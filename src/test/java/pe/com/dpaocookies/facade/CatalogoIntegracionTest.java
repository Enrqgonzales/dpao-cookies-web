package pe.com.dpaocookies.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import pe.com.dpaocookies.dto.PackDTO;
import pe.com.dpaocookies.dto.ProductoDTO;
import pe.com.dpaocookies.support.BaseDatosPrueba;

/** sql/catalogo-dpao.sql contra MySQL real: 16 sabores, 3 categorías y 9 precios de pack. */
@EnabledIfEnvironmentVariable(named = "DPAO_JDBC_TEST", matches = "true")
class CatalogoIntegracionTest {

    private final InventarioFacade facade = new InventarioFacade();

    @BeforeAll
    static void preparar() throws Exception {
        BaseDatosPrueba.preparar();
        BaseDatosPrueba.ejecutarScript("sql/catalogo-dpao.sql");
        BaseDatosPrueba.ejecutarScript("sql/catalogo-dpao.sql");   // se puede ejecutar dos veces
    }

    @Test
    void cargaLosDieciseisSaboresConSuPrecioYCategoria() {
        List<ProductoDTO> sabores = facade.listarProductos().stream()
                .filter(p -> p.getCodigo().matches("G\\d{2}")).toList();
        assertEquals(16, sabores.size());
        ProductoDTO pistacho = sabores.stream().filter(p -> p.getCodigo().equals("G16")).findFirst().orElseThrow();
        assertEquals("Pistacho", pistacho.getNombre());
        assertEquals(new BigDecimal("15.00"), pistacho.getPrecioVenta());
        assertEquals("Premium", pistacho.getNombreCategoria());
        assertEquals(0, pistacho.getStockActual().signum(), "un sabor nuevo empieza sin stock");
        assertEquals(4, sabores.stream().filter(p -> "Clásico".equals(p.getNombreCategoria())).count());
        assertEquals(9, sabores.stream().filter(p -> "Edición".equals(p.getNombreCategoria())).count());
    }

    @Test
    void cargaLosPacksDeDeliveryPorCategoria() {
        List<PackDTO> packs = facade.listarPacks();
        assertEquals(List.of("Clásico", "Edición", "Premium"), packs.stream().map(PackDTO::getCategoria).toList());
        assertEquals("12.00 22.00 30.00", precios(packs.get(0)));
        assertEquals("16.00 30.00 42.00", precios(packs.get(1)));
        assertEquals("20.00 40.00 58.00", precios(packs.get(2)));
        assertTrue(packs.get(2).getSabores().containsAll(List.of("Nutella", "Snicker", "Pistacho")));
        assertEquals(9, packs.get(1).getSabores().size());
    }

    private static String precios(PackDTO pack) {
        return pack.getPrecios().stream().map(p -> p.getPrecio().toPlainString()).collect(Collectors.joining(" "));
    }
}
