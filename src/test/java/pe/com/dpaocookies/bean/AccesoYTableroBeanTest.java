package pe.com.dpaocookies.bean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import pe.com.dpaocookies.bean.Dobles.FachadaFalsa;
import pe.com.dpaocookies.bean.Dobles.VistaFalsa;
import pe.com.dpaocookies.dto.ProductoDTO;
import pe.com.dpaocookies.dto.ResumenTableroDTO;
import pe.com.dpaocookies.exception.ServicioException;
import pe.com.dpaocookies.util.Autenticacion;

class AccesoYTableroBeanTest {

    private final VistaFalsa vista = new VistaFalsa();
    private final Autenticacion cuenta = new Autenticacion(
            Map.of("DPAO_APP_USER", "demo", "DPAO_APP_PASSWORD", "clave-de-prueba")::get);

    @Test
    void ingresoValidoAbreSesionConElLocalYVaAlTablero() {
        AccesoBean bean = new AccesoBean(cuenta, vista);
        bean.setUsuario(" demo ");
        bean.setClave("clave-de-prueba");
        bean.setLocal("Delivery");

        assertEquals("tablero?faces-redirect=true", bean.ingresar());
        assertEquals("demo", vista.usuario);
        assertEquals("Delivery", vista.local);
        assertTrue(vista.erroresEn.isEmpty());
    }

    @Test
    void ingresoInvalidoMarcaLaClaveYNoAbreSesion() {
        AccesoBean bean = new AccesoBean(cuenta, vista);
        bean.setUsuario("demo");
        bean.setClave("otra");

        assertNull(bean.ingresar());
        assertNull(vista.usuario);
        assertNull(bean.getClave(), "la contraseña no se vuelve a pintar");
        assertEquals(List.of("acceso:clave|Usuario o contraseña incorrectos."), vista.erroresEn);
    }

    @Test
    void localDesconocidoSeReemplazaPorElPrimero() {
        AccesoBean bean = new AccesoBean(cuenta, vista);
        bean.setUsuario("demo");
        bean.setClave("clave-de-prueba");
        bean.setLocal("<script>");

        bean.ingresar();
        assertEquals("Plaza Mayor", vista.local);
    }

    @Test
    void salirCierraLaSesion() {
        AccesoBean bean = new AccesoBean(cuenta, vista);
        assertEquals("acceso?faces-redirect=true", bean.salir());
        assertTrue(vista.sesionCerrada);
    }

    @Test
    void tableroConsultaElResumenUnaSolaVezPorVista() {
        FachadaFalsa fachada = new FachadaFalsa();
        fachada.resumen = new ResumenTableroDTO(9, 16, List.of(), List.of());
        TableroBean bean = new TableroBean(fachada, vista);

        assertSame(fachada.resumen, bean.getResumen());
        bean.getResumen();
        assertEquals(1, fachada.consultasResumen);
    }

    @Test
    void tableroSinBaseMuestraElErrorYUnResumenVacio() {
        FachadaFalsa fachada = new FachadaFalsa();
        fachada.fallaConsulta = () -> new ServicioException("No se pudo consultar la información.", null);
        TableroBean bean = new TableroBean(fachada, vista);

        assertEquals(0, bean.getResumen().getTotalInsumos());
        assertEquals(List.of("No se pudo consultar la información."), vista.errores);
    }

    @Test
    void atencionMuestraPrimeroLosInsumosYResumeLoQueNoCabe() {
        FachadaFalsa fachada = new FachadaFalsa();
        List<ProductoDTO> agotados = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            agotados.add(Dobles.producto(i, "Sabor " + i, 1, "7.00", "0"));
        }
        fachada.resumen = new ResumenTableroDTO(9, 16,
                List.of(Dobles.insumo(9, "Queso crema", "0.80", "1.50", "kg")), List.of(),
                BigDecimal.ZERO, BigDecimal.ZERO, List.of(), agotados);
        TableroBean bean = new TableroBean(fachada, vista);

        List<TableroBean.Atencion> filas = bean.getAtencion();
        assertEquals(TableroBean.FILAS_ATENCION, filas.size());
        assertEquals("Queso crema", filas.get(0).getNombre());
        assertEquals("0.80 de 1.50 kg", filas.get(0).getDetalle());
        assertFalse(filas.get(0).isAgotado());
        assertTrue(filas.get(1).isAgotado());
        assertEquals(3, bean.getAtencionRestante());
        assertEquals(5, bean.getAtencionRestanteMovil());
    }

    @Test
    void pestanaDelTableroSoloAceptaSusDosValores() {
        TableroBean bean = new TableroBean(new FachadaFalsa(), vista);
        bean.setPestana("atencion");
        assertTrue(bean.isPestanaAtencion());
        bean.setPestana("javascript:alert(1)");
        assertFalse(bean.isPestanaAtencion());
        assertEquals("vendidos", bean.getPestana());
    }
}
