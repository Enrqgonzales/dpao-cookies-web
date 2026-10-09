package pe.com.dpaocookies.bean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pe.com.dpaocookies.bean.Dobles.FachadaFalsa;
import pe.com.dpaocookies.bean.Dobles.VistaFalsa;
import pe.com.dpaocookies.dto.MovimientoDTO;
import pe.com.dpaocookies.exception.NegocioException;

class InsumosBeanTest {

    private final VistaFalsa vista = new VistaFalsa();
    private final FachadaFalsa fachada = new FachadaFalsa();
    private InsumosBean bean;

    @BeforeEach
    void preparar() {
        fachada.insumos = List.of(
                Dobles.insumo(1, "Harina de trigo", "18.00", "10.00", "kg"),
                Dobles.insumo(2, "Mantequilla", "2.75", "3.00", "kg"));
        bean = new InsumosBean(fachada, vista);
    }

    @Test
    void listaYCuentaLoQueHayQueReponer() {
        assertEquals(2, bean.getLista().size());
        assertEquals(1, bean.getPorReponer());
    }

    @Test
    void registrarEntregaElDtoALaFachadaYRedirige() {
        bean.setHoja("nuevo");
        bean.getNuevo().setCodigo("INS-010");
        bean.getNuevo().setNombre(" Vainilla ");

        assertEquals("insumos?faces-redirect=true", bean.registrar());
        assertSame(bean.getNuevo(), fachada.insumoRegistrado);
        assertEquals(List.of("Insumo registrado · Vainilla"), vista.exitos);
    }

    @Test
    void errorDeNegocioSeQuedaEnLaHojaConElMensajeDeLaFachada() {
        fachada.falla = new NegocioException("El código ya existe.");
        bean.setHoja("nuevo");

        assertNull(bean.registrar());
        assertEquals(List.of("formulario|El código ya existe."), vista.erroresEn);
        assertTrue(bean.isHojaNuevo(), "la hoja sigue abierta para corregir");
        assertTrue(vista.exitos.isEmpty());
    }

    @Test
    void hojaNuevaProponeUnidadYCategoria() {
        bean.setHoja("nuevo");
        bean.preparar();
        assertEquals("kg", bean.getNuevo().getUnidadMedida());
        assertEquals(4, bean.getNuevo().getIdCategoria());
    }

    @Test
    void hojaDeAjusteParteDelStockActual() {
        bean.setHoja("ajuste");
        bean.setId(2);
        bean.preparar();
        assertTrue(bean.isHojaAjuste());
        assertEquals(new BigDecimal("2.75"), bean.getStockContado());
    }

    @Test
    void hojaDeAjusteDeUnInsumoInexistenteNoSeAbre() {
        bean.setHoja("ajuste");
        bean.setId(99);
        bean.preparar();
        assertFalse(bean.isHojaAjuste());
        assertNull(bean.getHoja());
    }

    @Test
    void ajustarPideElAjusteALaFachadaYMuestraLosSaldos() {
        MovimientoDTO hecho = Dobles.movimiento("Mantequilla", "AJUSTE", "-0.25", "2.75", "2.50", null);
        fachada.respuestaMovimiento = Optional.of(hecho);
        bean.setHoja("ajuste");
        bean.setId(2);
        bean.setStockContado(new BigDecimal("2.50"));

        assertEquals("insumos?faces-redirect=true", bean.ajustar());
        assertEquals(2, fachada.idAjustado);
        assertEquals(new BigDecimal("2.50"), fachada.stockAjustado);
        assertEquals(List.of("Ajuste registrado · Mantequilla 2.75 → 2.50"), vista.exitos);
    }

    @Test
    void ajusteSinDiferenciaLoDiceSinInventarUnMovimiento() {
        bean.setId(1);
        bean.setStockContado(new BigDecimal("18.00"));
        bean.ajustar();
        assertEquals(List.of("Sin cambios · Harina de trigo ya tenía 18.00"), vista.exitos);
    }

    @Test
    void errorDelAjusteVaASuPropiaHoja() {
        fachada.falla = new NegocioException("Stock contado: valor entre 0 y 99999999.99, con máximo dos decimales.");
        bean.setHoja("ajuste");
        bean.setId(2);
        assertNull(bean.ajustar());
        assertEquals(1, vista.erroresEn.size());
        assertTrue(vista.erroresEn.get(0).startsWith("ajuste|Stock contado"));
    }
}
