package pe.com.dpaocookies.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import pe.com.dpaocookies.dto.MovimientoDTO;
import pe.com.dpaocookies.dto.ProductoDTO;
import pe.com.dpaocookies.dto.SolicitudMovimientoDTO;
import pe.com.dpaocookies.exception.NegocioException;
import pe.com.dpaocookies.exception.ServicioException;
import pe.com.dpaocookies.exception.StockInsuficienteException;
import pe.com.dpaocookies.support.BaseDatosPrueba;

/** Flujo completo a través de la fachada contra MySQL real, sin simulaciones. */
@EnabledIfEnvironmentVariable(named = "DPAO_JDBC_TEST", matches = "true")
class InventarioFacadeIntegracionTest {

    private final InventarioFacade facade = new InventarioFacade();

    @BeforeAll
    static void preparar() throws Exception {
        BaseDatosPrueba.preparar();
    }

    @Test
    void saldosAnteriorYPosteriorQuedanEncadenados() throws Exception {
        int id = nuevoProducto("FAC-SALDOS");
        mover(id, "ENTRADA", "10.00");
        mover(id, "VENTA", "3.00");
        mover(id, "MERMA", "1.00");
        MovimientoDTO ajuste = mover(id, "AJUSTE", "8.00");

        assertEquals(new BigDecimal("6.00"), ajuste.getStockAnterior());
        assertEquals(new BigDecimal("2.00"), ajuste.getCantidad());
        List<MovimientoDTO> historial = historial(id);
        assertEquals(4, historial.size());
        BigDecimal saldo = BigDecimal.ZERO.setScale(2);
        for (MovimientoDTO m : historial) {
            assertEquals(saldo, m.getStockAnterior(), "cada movimiento parte del saldo anterior");
            assertEquals(m.getStockAnterior().add(m.getCantidad()), m.getStockPosterior());
            saldo = m.getStockPosterior();
        }
        assertEquals(new BigDecimal("8.00"), stockProducto(id));
        assertEquals(saldo, stockProducto(id), "el stock coincide con el último saldo del Kardex");
    }

    @Test
    void ventaSinStockNoModificaStockNiHistorial() throws Exception {
        int id = nuevoProducto("FAC-SINSTOCK");
        mover(id, "ENTRADA", "2.00");

        assertThrows(StockInsuficienteException.class, () -> mover(id, "VENTA", "2.01"));
        assertEquals(new BigDecimal("2.00"), stockProducto(id));
        assertEquals(1, historial(id).size());
    }

    @Test
    void rollbackAtravesDeLaFachadaCuandoFallaElInsert() throws Exception {
        int id = nuevoProducto("FAC-ROLLBACK");
        mover(id, "ENTRADA", "4.00");
        BaseDatosPrueba.ejecutar("CREATE TRIGGER fallo_fachada BEFORE INSERT ON movimiento_kardex FOR EACH ROW "
                + "SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='fallo controlado de prueba'");
        try {
            assertThrows(ServicioException.class, () -> mover(id, "ENTRADA", "1.00"));
        } finally {
            BaseDatosPrueba.ejecutar("DROP TRIGGER fallo_fachada");
        }
        assertEquals(new BigDecimal("4.00"), stockProducto(id), "el UPDATE del stock se deshizo");
        assertEquals(1, historial(id).size());
    }

    @Test
    void ajusteSinDiferenciaNoRegistraMovimiento() throws Exception {
        int id = nuevoProducto("FAC-AJUSTE");
        mover(id, "ENTRADA", "3.00");
        assertTrue(facade.registrarMovimiento(
                new SolicitudMovimientoDTO("PRODUCTO", id, "AJUSTE", new BigDecimal("3.00"), "Conteo")).isEmpty());
        assertEquals(1, historial(id).size());
    }

    @Test
    void codigoDuplicadoSeInformaComoErrorDeNegocio() {
        nuevoProducto("FAC-DUP");
        NegocioException error = assertThrows(NegocioException.class, () -> nuevoProducto("FAC-DUP"));
        assertEquals("El código ya existe.", error.getMessage());
    }

    @Test
    void ventasConcurrentesNuncaDejanStockNegativo() throws Exception {
        int id = nuevoProducto("FAC-CONC");
        mover(id, "ENTRADA", "5.00");
        ExecutorService executor = Executors.newFixedThreadPool(4);
        int exitosas = 0;
        int rechazadas = 0;
        try {
            List<Callable<MovimientoDTO>> ventas = new ArrayList<>();
            for (int n = 0; n < 10; n++) {
                ventas.add(() -> mover(id, "VENTA", "1.00"));
            }
            for (Future<MovimientoDTO> venta : executor.invokeAll(ventas)) {
                try {
                    venta.get();
                    exitosas++;
                } catch (ExecutionException e) {
                    assertTrue(e.getCause() instanceof StockInsuficienteException, e.getCause().toString());
                    rechazadas++;
                }
            }
        } finally {
            executor.shutdown();
        }
        assertEquals(5, exitosas);
        assertEquals(5, rechazadas);
        assertEquals(0, stockProducto(id).signum());
        assertEquals(6, historial(id).size());
    }

    private int nuevoProducto(String codigo) {
        ProductoDTO dto = new ProductoDTO();
        dto.setCodigo(codigo);
        dto.setNombre("Galleta " + codigo);
        dto.setPrecioVenta(new BigDecimal("6.50"));
        dto.setIdCategoria(2);
        return facade.guardarProducto(dto);
    }

    private MovimientoDTO mover(int idProducto, String tipo, String cantidad) {
        return facade.registrarMovimiento(new SolicitudMovimientoDTO("PRODUCTO", idProducto, tipo,
                new BigDecimal(cantidad), "Prueba de integración " + tipo)).orElseThrow();
    }

    private List<MovimientoDTO> historial(int idProducto) {
        return facade.consultarKardex(null, null, null).stream()
                .filter(m -> m.getClaseItem().equals("PRODUCTO") && m.getIdItem() == idProducto)
                .sorted(Comparator.comparingLong(MovimientoDTO::getIdMovimiento))
                .toList();
    }

    private BigDecimal stockProducto(int idProducto) {
        return facade.listarProductos().stream()
                .filter(p -> p.getIdProducto() == idProducto)
                .findFirst().orElseThrow().getStockActual();
    }
}
