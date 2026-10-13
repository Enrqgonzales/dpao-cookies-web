package pe.com.dpaocookies.bean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pe.com.dpaocookies.bean.Dobles.FachadaFalsa;
import pe.com.dpaocookies.bean.Dobles.VistaFalsa;
import pe.com.dpaocookies.dto.ProductoDTO;
import pe.com.dpaocookies.exception.NegocioException;

class ProductosBeanTest {

    private final VistaFalsa vista = new VistaFalsa();
    private final FachadaFalsa fachada = new FachadaFalsa();
    private ProductosBean bean;

    @BeforeEach
    void preparar() {
        fachada.categorias = List.of(Dobles.categoria(1, "Clásico"), Dobles.categoria(2, "Edición"),
                Dobles.categoria(3, "Premium"), Dobles.categoria(4, "Insumos de cocina"));
        fachada.productos = List.of(
                Dobles.producto(1, "Chispas", 1, "7.00", "6"),
                Dobles.producto(2, "Chunky", 1, "7.00", "1"),
                Dobles.producto(5, "Red Velvet", 2, "8.50", "5"),
                Dobles.producto(16, "Pistacho", 3, "15.00", "0"));
        bean = new ProductosBean(fachada, vista);
    }

    @Test
    void chipsCuentanSaboresSoloDeCategoriasConSabores() {
        List<ProductosBean.Cuenta> cuentas = bean.getCategoriasConSabores();
        assertEquals(List.of("Clásico", "Edición", "Premium"),
                cuentas.stream().map(c -> c.getCategoria().getNombre()).toList());
        assertEquals(List.of(2, 1, 1), cuentas.stream().map(ProductosBean.Cuenta::getSabores).toList());
        assertEquals(3, bean.getCategoriasDeSabor().size(), "la categoría de insumos no se ofrece para un sabor");
    }

    @Test
    void filtroDeCategoriaDejaSoloSusSabores() {
        bean.setCategoria(1);
        assertEquals(List.of("Chispas", "Chunky"), bean.getSabores().stream().map(ProductoDTO::getNombre).toList());
        assertEquals("Clásico", bean.getNombreCategoria());
        bean.setCategoria(-3);
        assertEquals(4, bean.getSabores().size());
    }

    @Test
    void editarCargaUnaCopiaDelSabor() {
        bean.setHoja("editar");
        bean.setId(5);
        bean.preparar();
        assertTrue(bean.isHojaAbierta());
        assertTrue(bean.isEditando());
        assertEquals("Red Velvet", bean.getSabor().getNombre());
        assertNotSame(fachada.productos.get(2), bean.getSabor(), "editar no toca la lista mostrada");
    }

    @Test
    void editarUnSaborInexistenteNoAbreLaHoja() {
        bean.setHoja("editar");
        bean.setId(99);
        bean.preparar();
        assertFalse(bean.isHojaAbierta());
    }

    @Test
    void nuevoSaborTomaLaCategoriaDelFiltro() {
        bean.setHoja("nuevo");
        bean.setCategoria(3);
        bean.preparar();
        assertEquals(3, bean.getSabor().getIdCategoria());
        assertFalse(bean.isEditando());
    }

    @Test
    void guardarNuevoYEditadoDicenLoQuePaso() {
        bean.setHoja("nuevo");
        bean.getSabor().setNombre("Coco");
        bean.getSabor().setPrecioVenta(new BigDecimal("9.00"));
        assertEquals("productos?faces-redirect=true", bean.guardar());

        ProductosBean edicion = new ProductosBean(fachada, vista);
        edicion.setHoja("editar");
        edicion.setId(1);
        edicion.preparar();
        edicion.getSabor().setPrecioVenta(new BigDecimal("7.50"));
        edicion.guardar();

        assertEquals(List.of("Sabor registrado · Coco", "Sabor guardado · Chispas"), vista.exitos);
        assertEquals(new BigDecimal("7.50"), fachada.productoGuardado.getPrecioVenta());
        assertEquals(1, fachada.productoGuardado.getIdProducto());
    }

    @Test
    void errorAlGuardarSeMuestraEnLaHoja() {
        fachada.falla = new NegocioException("Nombre: es obligatorio.");
        bean.setHoja("nuevo");
        assertNull(bean.guardar());
        assertEquals(List.of("formulario|Nombre: es obligatorio."), vista.erroresEn);
    }

    @Test
    void pestanaPacksNoAbreLaHojaDeSabor() {
        bean.setPestana("packs");
        bean.setHoja("nuevo");
        assertTrue(bean.isPestanaPacks());
        assertFalse(bean.isHojaAbierta());
        bean.setPestana("otra");
        assertEquals("sabores", bean.getPestana());
    }
}
