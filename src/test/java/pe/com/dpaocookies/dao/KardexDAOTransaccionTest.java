package pe.com.dpaocookies.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.Test;
import pe.com.dpaocookies.exception.NegocioException;
import pe.com.dpaocookies.exception.StockInsuficienteException;
import pe.com.dpaocookies.model.ClaseItem;
import pe.com.dpaocookies.model.MovimientoKardex;
import pe.com.dpaocookies.model.TipoMovimiento;
import pe.com.dpaocookies.support.JdbcFalso;

/** Control transaccional de KardexDAO: autoCommit(false), commit y rollback. */
class KardexDAOTransaccionTest {

    @Test
    void entradaConfirmaLaTransaccionConSaldos() throws SQLException {
        JdbcFalso jdbc = new JdbcFalso().conStock("5.00");
        MovimientoKardex m = new KardexDAO(jdbc.proveedor())
                .registrar(ClaseItem.INSUMO, 1, TipoMovimiento.ENTRADA, new BigDecimal("3.00"), "Compra");

        assertEquals(new BigDecimal("5.00"), m.getStockAnterior());
        assertEquals(new BigDecimal("3.00"), m.getCantidad());
        assertEquals(new BigDecimal("8.00"), m.getStockPosterior());
        assertEquals(99L, m.getIdMovimiento());
        assertEquals("HAR-001", m.getCodigoItem());
        assertEquals(List.of("autoCommit=false", "commit", "close"), jdbc.eventos);
        assertTrue(jdbc.sentencias.get(0).endsWith("FOR UPDATE"), "primero bloquea la fila");
        assertTrue(jdbc.sentencias.get(1).startsWith("UPDATE insumo SET stock_actual"));
        assertTrue(jdbc.sentencias.get(2).startsWith("INSERT INTO movimiento_kardex (id_insumo"));
    }

    @Test
    void salidaSeGuardaConSignoNegativo() throws SQLException {
        JdbcFalso jdbc = new JdbcFalso().conStock("5.00");
        MovimientoKardex m = new KardexDAO(jdbc.proveedor())
                .registrar(ClaseItem.PRODUCTO, 2, TipoMovimiento.VENTA, new BigDecimal("2.00"), "Venta");

        assertEquals(new BigDecimal("-2.00"), m.getCantidad());
        assertEquals(new BigDecimal("3.00"), m.getStockPosterior());
        assertTrue(jdbc.ejecuto("UPDATE producto SET stock_actual"));
    }

    @Test
    void falloDelInsertHaceRollbackDelUpdateYaEjecutado() {
        JdbcFalso jdbc = new JdbcFalso().conStock("5.00").fallandoEn("INSERT");
        KardexDAO dao = new KardexDAO(jdbc.proveedor());

        assertThrows(SQLException.class, () -> dao.registrar(ClaseItem.INSUMO, 1,
                TipoMovimiento.ENTRADA, BigDecimal.ONE, "Prueba rollback"));
        assertTrue(jdbc.ejecuto("UPDATE"), "el UPDATE se ejecutó antes del fallo");
        assertTrue(jdbc.eventos.contains("rollback"));
        assertFalse(jdbc.eventos.contains("commit"));
        assertEquals("close", jdbc.eventos.get(jdbc.eventos.size() - 1));
    }

    @Test
    void stockInsuficienteHaceRollbackSinTocarElStock() {
        JdbcFalso jdbc = new JdbcFalso().conStock("5.00");
        KardexDAO dao = new KardexDAO(jdbc.proveedor());

        StockInsuficienteException error = assertThrows(StockInsuficienteException.class,
                () -> dao.registrar(ClaseItem.INSUMO, 1, TipoMovimiento.MERMA, new BigDecimal("5.01"), "Merma"));
        assertEquals(new BigDecimal("5.00"), error.getDisponible());
        assertFalse(jdbc.ejecuto("UPDATE"));
        assertFalse(jdbc.ejecuto("INSERT"));
        assertTrue(jdbc.eventos.contains("rollback"));
        assertFalse(jdbc.eventos.contains("commit"));
    }

    @Test
    void salidaQueDejaStockEnCeroEsValida() throws SQLException {
        JdbcFalso jdbc = new JdbcFalso().conStock("5.00");
        MovimientoKardex m = new KardexDAO(jdbc.proveedor())
                .registrar(ClaseItem.INSUMO, 1, TipoMovimiento.SALIDA, new BigDecimal("5.00"), "Producción");

        assertEquals(0, m.getStockPosterior().signum());
        assertTrue(jdbc.eventos.contains("commit"));
    }

    @Test
    void ajusteRegistraLaDiferenciaConElConteo() throws SQLException {
        JdbcFalso jdbc = new JdbcFalso().conStock("5.00");
        MovimientoKardex m = new KardexDAO(jdbc.proveedor())
                .registrar(ClaseItem.INSUMO, 1, TipoMovimiento.AJUSTE, new BigDecimal("2.00"), "Conteo");

        assertEquals(new BigDecimal("-3.00"), m.getCantidad());
        assertEquals(new BigDecimal("2.00"), m.getStockPosterior());
    }

    @Test
    void ajusteSinDiferenciaNoCreaMovimientoVacio() throws SQLException {
        JdbcFalso jdbc = new JdbcFalso().conStock("5.00");
        assertNull(new KardexDAO(jdbc.proveedor())
                .registrar(ClaseItem.INSUMO, 1, TipoMovimiento.AJUSTE, new BigDecimal("5.00"), "Conteo"));
        assertFalse(jdbc.ejecuto("UPDATE"));
        assertFalse(jdbc.ejecuto("INSERT"));
        assertTrue(jdbc.eventos.contains("commit"));
    }

    @Test
    void itemInexistenteHaceRollback() {
        JdbcFalso jdbc = new JdbcFalso().conStock(null);
        assertThrows(NegocioException.class, () -> new KardexDAO(jdbc.proveedor())
                .registrar(ClaseItem.PRODUCTO, 404, TipoMovimiento.ENTRADA, BigDecimal.ONE, "Entrada"));
        assertTrue(jdbc.eventos.contains("rollback"));
        assertFalse(jdbc.eventos.contains("commit"));
    }
}
