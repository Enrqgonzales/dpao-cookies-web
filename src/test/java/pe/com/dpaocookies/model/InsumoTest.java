package pe.com.dpaocookies.model;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import pe.com.dpaocookies.exception.NegocioException;
import pe.com.dpaocookies.util.Validacion;

class InsumoTest {
    @Test void inferiorAlMinimo() { assertTrue(insumo("4.99").isBajoStock()); }
    @Test void igualAlMinimo() { assertTrue(insumo("5.00").isBajoStock()); }
    @Test void superiorAlMinimo() { assertFalse(insumo("5.01").isBajoStock()); }
    @Test void rechazaDecimalesInvalidos() {
        for (String valor : new String[]{"-0.01", "100000000", "1.001"})
            assertThrows(NegocioException.class, () -> Validacion.decimal(new BigDecimal(valor)));
        assertThrows(NegocioException.class, () -> Validacion.decimal(null));
        assertEquals(new BigDecimal("99999999.99"), Validacion.decimal(new BigDecimal("99999999.99")));
    }
    @Test void rechazaTextosInvalidos() {
        assertThrows(NegocioException.class, () -> Validacion.texto(" ", 20, "Código"));
        assertThrows(NegocioException.class, () -> Validacion.texto("x".repeat(21), 20, "Código"));
    }
    @Test void tiposDeMovimientoCalculanLaVariacion() {
        BigDecimal anterior = new BigDecimal("5.00");
        assertEquals(new BigDecimal("2.00"), TipoMovimiento.ENTRADA.variacion(new BigDecimal("2.00"), anterior));
        for (TipoMovimiento salida : new TipoMovimiento[]{TipoMovimiento.SALIDA, TipoMovimiento.VENTA, TipoMovimiento.MERMA})
            assertEquals(new BigDecimal("-2.00"), salida.variacion(new BigDecimal("2.00"), anterior));
        assertEquals(new BigDecimal("-3.00"), TipoMovimiento.AJUSTE.variacion(new BigDecimal("2.00"), anterior));
        assertThrows(NegocioException.class, () -> TipoMovimiento.desde("DEVOLUCION"));
        assertThrows(NegocioException.class, () -> ClaseItem.desde("insumo; DROP TABLE insumo"));
    }
    private Insumo insumo(String stock) {
        return new Insumo("TEST", "Harina", "kg", new BigDecimal(stock), new BigDecimal("5.00"), BigDecimal.ONE, 1);
    }
}
