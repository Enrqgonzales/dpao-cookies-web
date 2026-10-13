package pe.com.dpaocookies.bean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;
import org.junit.jupiter.api.Test;
import pe.com.dpaocookies.bean.Dobles.FachadaFalsa;
import pe.com.dpaocookies.bean.Dobles.VistaFalsa;
import pe.com.dpaocookies.dto.MovimientoDTO;
import pe.com.dpaocookies.exception.NegocioException;
import pe.com.dpaocookies.exception.StockInsuficienteException;
import pe.com.dpaocookies.facade.InventarioFacade;

class KardexBeanTest {

    private static final LocalDate HOY = LocalDate.of(2026, 10, 9);
    private final VistaFalsa vista = new VistaFalsa();
    private final FachadaFalsa fachada = new FachadaFalsa();
    private final KardexBean bean = new KardexBean(fachada, vista, () -> HOY);

    private static Date enLima(LocalDateTime momento) {
        return Date.from(momento.atZone(InventarioFacade.ZONA).toInstant());
    }

    @Test
    void agrupaPorDiaDeLimaConHoyAyerYFecha() {
        fachada.movimientos = List.of(
                Dobles.movimiento("Pistacho", "VENTA", "-3", "5", "2", enLima(HOY.atTime(20, 10))),
                Dobles.movimiento("Chunky", "ENTRADA", "6", "4", "10", enLima(HOY.atTime(0, 5))),
                Dobles.movimiento("Harina de trigo", "SALIDA", "-4.5", "22.5", "18", enLima(HOY.minusDays(1).atTime(23, 55))),
                Dobles.movimiento("Mantequilla", "AJUSTE", "-0.25", "2.75", "2.5", enLima(LocalDateTime.of(2026, 10, 5, 9, 0))));

        List<KardexBean.Dia> dias = bean.getDias();
        assertEquals(List.of("Hoy", "Ayer", "Lun 5 oct"), dias.stream().map(KardexBean.Dia::getNombre).toList());
        assertEquals(2, dias.get(0).getMovimientos().size());
        assertEquals(4, bean.getTotalFilas());
        assertEquals(KardexBean.MAXIMO_FILAS, fachada.filtroLimite);
    }

    @Test
    void elFiltroLlegaALaFachadaYSoloAceptaTiposConocidos() {
        bean.setTipo("VENTA");
        bean.setDesde(HOY.minusDays(7));
        bean.setHasta(HOY);
        bean.getDias();
        assertEquals("VENTA", fachada.filtroTipo);
        assertEquals(HOY.minusDays(7), fachada.filtroDesde);
        assertTrue(bean.isFiltrado());

        KardexBean otro = new KardexBean(fachada, vista, () -> HOY);
        otro.setTipo("VENTA' OR 1=1");
        assertNull(otro.getTipo());
        assertFalse(otro.isFiltrado());
    }

    @Test
    void registrarArmaLaSolicitudDesdeElItemElegido() {
        fachada.respuestaMovimiento = Optional.of(Dobles.movimiento("Pistacho", "VENTA", "-3", "5.00", "2.00", null));
        bean.setHoja("nuevo");
        bean.setItem("PRODUCTO:16");
        bean.setTipoNuevo("VENTA");
        bean.setCantidad(new BigDecimal("3"));
        bean.setMotivo("Venta en vitrina");

        assertEquals("kardex?faces-redirect=true", bean.registrar());
        assertEquals("PRODUCTO", fachada.solicitud.getClaseItem());
        assertEquals(16, fachada.solicitud.getIdItem());
        assertEquals("VENTA", fachada.solicitud.getTipoMovimiento());
        assertEquals(new BigDecimal("3"), fachada.solicitud.getCantidad());
        assertEquals("Venta en vitrina", fachada.solicitud.getMotivo());
        assertEquals(List.of("Venta registrada · Pistacho 5.00 → 2.00"), vista.exitos);
    }

    @Test
    void ajusteSeNombraEnMasculino() {
        fachada.respuestaMovimiento = Optional.of(Dobles.movimiento("Mantequilla", "AJUSTE", "-0.25", "2.75", "2.50", null));
        bean.setItem("INSUMO:2");
        bean.registrar();
        assertEquals(List.of("Ajuste registrado · Mantequilla 2.75 → 2.50"), vista.exitos);
    }

    @Test
    void stockInsuficienteSeMarcaEnLaCantidad() {
        fachada.falla = new StockInsuficienteException(new BigDecimal("1.00"), new BigDecimal("2.00"));
        bean.setItem("PRODUCTO:11");
        assertNull(bean.registrar());
        assertEquals(List.of("formulario:cantidad|Stock insuficiente: disponible 1.00, solicitado 2.00."), vista.erroresEn);
    }

    @Test
    void itemMalFormadoLoRechazaLaFachadaNoElBean() {
        fachada.falla = new NegocioException("Seleccione un ítem válido.");
        bean.setItem("PRODUCTO:x");
        assertNull(bean.registrar());
        assertEquals(0, fachada.solicitud.getIdItem());
        assertEquals(List.of("formulario|Seleccione un ítem válido."), vista.erroresEn);
    }

    @Test
    void filtroInvalidoLoDecideLaFachadaYSeQuedaEnSuHoja() {
        KardexBean conFallo = new KardexBean(new FachadaFalsa() {
            @Override
            public List<MovimientoDTO> consultarKardex(String tipo, LocalDate desde, LocalDate hasta, int limite) {
                throw new NegocioException("La fecha inicial no puede ser posterior a la fecha final.");
            }
        }, vista, () -> HOY);
        conFallo.setHoja("filtro");
        conFallo.setDesde(HOY);
        conFallo.setHasta(HOY.minusDays(1));

        assertNull(conFallo.filtrar());
        assertEquals(List.of("filtro|La fecha inicial no puede ser posterior a la fecha final."), vista.erroresEn);
        assertTrue(conFallo.isHojaFiltro());
    }

    @Test
    void filtroValidoRedirigeConLosParametrosDeLaVista() {
        bean.setHoja("filtro");
        bean.setTipo("MERMA");
        assertEquals("kardex?faces-redirect=true&includeViewParams=true", bean.filtrar());
        assertNull(bean.getHoja(), "la hoja de filtro se cierra al aplicar");
    }

    @Test
    void itemsSeparanSaboresEInsumos() {
        fachada.productos = List.of(Dobles.producto(1, "Chispas", 1, "7.00", "6"));
        fachada.insumos = List.of(Dobles.insumo(2, "Mantequilla", "2.50", "3.00", "kg"));

        List<SelectItem> grupos = bean.getItems();
        assertEquals(List.of("Sabores", "Insumos"), grupos.stream().map(SelectItem::getLabel).toList());
        SelectItem sabor = ((SelectItemGroup) grupos.get(0)).getSelectItems()[0];
        assertEquals("PRODUCTO:1", sabor.getValue());
        assertEquals("Chispas · 6 en vitrina", sabor.getLabel());
        SelectItem insumo = ((SelectItemGroup) grupos.get(1)).getSelectItems()[0];
        assertEquals("INSUMO:2", insumo.getValue());
        assertEquals("Mantequilla · 2.50 kg", insumo.getLabel());
    }
}
