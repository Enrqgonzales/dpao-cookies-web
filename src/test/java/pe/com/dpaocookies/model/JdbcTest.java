package pe.com.dpaocookies.model;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.sql.*;
import java.nio.file.*;
import pe.com.dpaocookies.dao.*;
import pe.com.dpaocookies.util.ConexionBD;

@EnabledIfEnvironmentVariable(named="DPAO_JDBC_TEST", matches="true")
class JdbcTest {
    static String schema;
    @BeforeAll static void preparar() throws Exception {
        String url=System.getenv("DPAO_DB_URL");
        if(url==null || !url.startsWith("jdbc:mysql://127.0.0.1:3307/dpao_test?"))
            throw new IllegalStateException("Pruebas permitidas solamente en MySQL aislado 127.0.0.1:3307/dpao_test.");
        schema="dpao_test_"+System.currentTimeMillis();
        try(Connection c=ConexionBD.getConnection(); Statement s=c.createStatement()) { s.execute("CREATE DATABASE "+schema+" CHARACTER SET utf8mb4"); }
        System.setProperty("DPAO_DB_URL",url.replace("/dpao_test?","/"+schema+"?"));
        try(Connection c=ConexionBD.getConnection(); Statement s=c.createStatement()) {
            for(String file:new String[]{"sql/schema.sql","sql/seed.sql"}) {
                String sql=Files.readString(Path.of(file)).replaceAll("(?m)^--.*$","");
                for(String cmd:sql.split(";")) if(!cmd.isBlank()) s.execute(cmd);
            }
            System.out.println("Motor real: "+c.getMetaData().getDatabaseProductName()+" "+c.getMetaData().getDatabaseProductVersion()+"; esquema aislado: "+schema);
        }
    }
    // Se conserva el esquema de esta ejecución como evidencia; no se toca información real.
    @AfterAll static void terminar() { System.clearProperty("DPAO_DB_URL"); }
    @Test void conexionJdbc() throws Exception {
        try(Connection c=ConexionBD.getConnection(); Statement s=c.createStatement(); ResultSet r=s.executeQuery("SELECT 1")) {
            assertTrue(r.next()); assertEquals(1,r.getInt(1));
        }
    }
    @Test void errorDeConexionReal() {
        String previo=System.getProperty("DPAO_DB_URL");
        try {
            System.setProperty("DPAO_DB_URL","jdbc:mysql://127.0.0.1:1/"+schema+"?connectTimeout=1000");
            assertThrows(SQLException.class,ConexionBD::getConnection);
        } finally { System.setProperty("DPAO_DB_URL",previo); }
    }
    @Test void movimientosConcurrentesNoPierdenStock() throws Exception {
        ProductoDAO dao=new ProductoDAO(); dao.guardar(0,"CONC","Concurrencia",BigDecimal.ONE,2);
        int id=((Number)dao.listar().stream().filter(p->p.get("codigo").equals("CONC")).findFirst().orElseThrow().get("id_producto")).intValue();
        var executor=java.util.concurrent.Executors.newFixedThreadPool(2);
        try {
            var tareas=new java.util.ArrayList<java.util.concurrent.Callable<Void>>();
            for(int n=0;n<10;n++) tareas.add(()->{ new KardexDAO().mover("producto",id,"ENTRADA",new BigDecimal("0.10"),"Concurrente"); return null; });
            for(var f:executor.invokeAll(tareas)) f.get();
            try(Connection c=ConexionBD.getConnection(); Statement s=c.createStatement(); ResultSet r=s.executeQuery("SELECT stock_actual FROM producto WHERE id_producto="+id)) {
                assertTrue(r.next()); assertEquals(new BigDecimal("1.00"),r.getBigDecimal(1));
            }
        } finally { executor.shutdown(); }
    }
    @Test void registroConsultaActualizacionYErrores() throws Exception {
        InsumoDAO dao=new InsumoDAO();
        Insumo i=new Insumo("TEST-UTF8","Azúcar ñ","kg",new BigDecimal("0.10"),BigDecimal.ONE,BigDecimal.ONE,1);
        assertTrue(dao.registrar(i));
        Insumo leido=dao.listar().stream().filter(x->x.getCodigo().equals("TEST-UTF8")).findFirst().orElseThrow();
        assertEquals("Azúcar ñ",leido.getNombre());
        assertEquals(new BigDecimal("0.10"),leido.getStockActual());
        assertTrue(dao.actualizarStock(leido.getIdInsumo(),new BigDecimal("0.30")));
        assertThrows(IllegalArgumentException.class,()->dao.registrar(i));
        i.setCodigo("NO-CAT"); i.setIdCategoria(999999);
        assertThrows(IllegalArgumentException.class,()->dao.registrar(i));
        assertThrows(IllegalArgumentException.class,()->dao.actualizarStock(999999,BigDecimal.ONE));
        try(Connection c=ConexionBD.getConnection(); Statement s=c.createStatement(); ResultSet r=s.executeQuery("SELECT stock_actual FROM insumo WHERE codigo='TEST-UTF8'")) {
            assertTrue(r.next()); assertEquals(new BigDecimal("0.30"),r.getBigDecimal(1));
        }
    }
    @Test void rollbackTrasFalloInsertKardex() throws Exception {
        try(Connection c=ConexionBD.getConnection(); Statement s=c.createStatement()) {
            s.execute("CREATE TRIGGER fallo_prueba BEFORE INSERT ON movimiento_kardex FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='fallo controlado de prueba'");
            try {
                assertThrows(SQLException.class,()->new KardexDAO().mover("insumo",1,"ENTRADA",BigDecimal.ONE,"Prueba rollback"));
                try(ResultSet r=s.executeQuery("SELECT stock_actual FROM insumo WHERE id_insumo=1")) {
                    assertTrue(r.next()); assertEquals(new BigDecimal("5.00"),r.getBigDecimal(1));
                }
            } finally { s.execute("DROP TRIGGER fallo_prueba"); }
        }
    }
    @Test void productosMovimientosYRestricciones() throws Exception {
        ProductoDAO dao=new ProductoDAO();
        dao.guardar(0,"TEST-P","Galleta ñ",new BigDecimal("6.50"),2);
        int id=((Number)dao.listar().stream().filter(p->p.get("codigo").equals("TEST-P")).findFirst().orElseThrow().get("id_producto")).intValue();
        dao.guardar(id,"TEST-P","Galleta cacao",new BigDecimal("7.50"),2);
        KardexDAO k=new KardexDAO();
        k.mover("producto",id,"ENTRADA",new BigDecimal("3.00"),"Horneado");
        k.mover("producto",id,"VENTA",BigDecimal.ONE,"Venta de prueba");
        assertThrows(IllegalArgumentException.class,()->k.mover("producto",id,"SALIDA",new BigDecimal("9.00"),"Stock insuficiente"));
        assertTrue(k.listar("VENTA",null,null).stream().anyMatch(m->((Number)m.get("id_producto")).intValue()==id));
        try(Connection c=ConexionBD.getConnection(); Statement s=c.createStatement()) {
            assertThrows(SQLException.class,()->s.execute("DELETE FROM producto WHERE id_producto="+id));
            assertThrows(SQLException.class,()->s.execute("INSERT INTO movimiento_kardex(tipo_movimiento,cantidad,stock_anterior,stock_posterior,observacion) VALUES('ENTRADA',1,0,1,'sin FK')"));
            try(ResultSet r=s.executeQuery("SELECT stock_actual FROM producto WHERE id_producto="+id)) { assertTrue(r.next()); assertEquals(new BigDecimal("2.00"),r.getBigDecimal(1)); }
        }
    }
}
