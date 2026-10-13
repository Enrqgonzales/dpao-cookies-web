package pe.com.dpaocookies.model;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.sql.*;
import pe.com.dpaocookies.dao.*;
import pe.com.dpaocookies.exception.NegocioException;
import pe.com.dpaocookies.exception.StockInsuficienteException;
import pe.com.dpaocookies.support.BaseDatosPrueba;
import pe.com.dpaocookies.util.ConexionBD;

/** DAO contra MySQL real (esquema aislado creado por BaseDatosPrueba). */
@EnabledIfEnvironmentVariable(named="DPAO_JDBC_TEST", matches="true")
class JdbcTest {
    static String schema;
    @BeforeAll static void preparar() throws Exception { schema=BaseDatosPrueba.preparar(); }

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
        int id=new ProductoDAO().registrar(new Producto(0,"CONC","Concurrencia",BigDecimal.ONE,BigDecimal.ZERO,2));
        var executor=java.util.concurrent.Executors.newFixedThreadPool(2);
        try {
            var tareas=new java.util.ArrayList<java.util.concurrent.Callable<Void>>();
            for(int n=0;n<10;n++) tareas.add(()->{ new KardexDAO().registrar(ClaseItem.PRODUCTO,id,TipoMovimiento.ENTRADA,new BigDecimal("0.10"),"Concurrente"); return null; });
            for(var f:executor.invokeAll(tareas)) f.get();
            assertEquals(new BigDecimal("1.00"),BaseDatosPrueba.decimal("SELECT stock_actual FROM producto WHERE id_producto="+id));
        } finally { executor.shutdown(); }
    }
    @Test void registroConsultaActualizacionYErrores() throws Exception {
        InsumoDAO dao=new InsumoDAO();
        Insumo i=new Insumo("TEST-UTF8","Azúcar ñ","kg",new BigDecimal("0.10"),BigDecimal.ONE,BigDecimal.ONE,1);
        int id=dao.registrar(i);
        assertTrue(id>0);
        Insumo leido=dao.listar().stream().filter(x->x.getCodigo().equals("TEST-UTF8")).findFirst().orElseThrow();
        assertEquals("Azúcar ñ",leido.getNombre());
        assertEquals(new BigDecimal("0.10"),leido.getStockActual());
        new KardexDAO().registrar(ClaseItem.INSUMO,id,TipoMovimiento.AJUSTE,new BigDecimal("0.30"),"Ajuste");
        assertEquals(1062,assertThrows(SQLException.class,()->dao.registrar(i)).getErrorCode());
        i.setCodigo("NO-CAT"); i.setIdCategoria(999999);
        assertEquals(1452,assertThrows(SQLException.class,()->dao.registrar(i)).getErrorCode());
        assertThrows(NegocioException.class,()->new KardexDAO().registrar(ClaseItem.INSUMO,999999,TipoMovimiento.AJUSTE,BigDecimal.ONE,"x"));
        assertEquals(new BigDecimal("0.30"),BaseDatosPrueba.decimal("SELECT stock_actual FROM insumo WHERE codigo='TEST-UTF8'"));
    }
    @Test void rollbackTrasFalloInsertKardex() throws Exception {
        BigDecimal antes=BaseDatosPrueba.decimal("SELECT stock_actual FROM insumo WHERE id_insumo=1");
        BaseDatosPrueba.ejecutar("CREATE TRIGGER fallo_prueba BEFORE INSERT ON movimiento_kardex FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='fallo controlado de prueba'");
        try {
            assertThrows(SQLException.class,()->new KardexDAO().registrar(ClaseItem.INSUMO,1,TipoMovimiento.ENTRADA,BigDecimal.ONE,"Prueba rollback"));
            assertEquals(antes,BaseDatosPrueba.decimal("SELECT stock_actual FROM insumo WHERE id_insumo=1"));
        } finally { BaseDatosPrueba.ejecutar("DROP TRIGGER fallo_prueba"); }
    }
    @Test void productosMovimientosYRestricciones() throws Exception {
        ProductoDAO dao=new ProductoDAO();
        int id=dao.registrar(new Producto(0,"TEST-P","Galleta ñ",new BigDecimal("6.50"),BigDecimal.ZERO,2));
        assertTrue(dao.actualizar(new Producto(id,"TEST-P","Galleta cacao",new BigDecimal("7.50"),BigDecimal.ZERO,2)));
        KardexDAO k=new KardexDAO();
        k.registrar(ClaseItem.PRODUCTO,id,TipoMovimiento.ENTRADA,new BigDecimal("3.00"),"Horneado");
        k.registrar(ClaseItem.PRODUCTO,id,TipoMovimiento.VENTA,BigDecimal.ONE,"Venta de prueba");
        assertThrows(StockInsuficienteException.class,()->k.registrar(ClaseItem.PRODUCTO,id,TipoMovimiento.SALIDA,new BigDecimal("9.00"),"Stock insuficiente"));
        assertTrue(k.listar(TipoMovimiento.VENTA,null,null,0).stream().anyMatch(m->m.getClaseItem()==ClaseItem.PRODUCTO && m.getIdItem()==id));
        try(Connection c=ConexionBD.getConnection(); Statement s=c.createStatement()) {
            assertThrows(SQLException.class,()->s.execute("DELETE FROM producto WHERE id_producto="+id));
            assertThrows(SQLException.class,()->s.execute("INSERT INTO movimiento_kardex(tipo_movimiento,cantidad,stock_anterior,stock_posterior,observacion) VALUES('ENTRADA',1,0,1,'sin FK')"));
            assertThrows(SQLException.class,()->s.execute("UPDATE producto SET stock_actual=-1 WHERE id_producto="+id));
        }
        assertEquals(new BigDecimal("2.00"),BaseDatosPrueba.decimal("SELECT stock_actual FROM producto WHERE id_producto="+id));
    }
}
