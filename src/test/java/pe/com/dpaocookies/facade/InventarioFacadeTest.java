package pe.com.dpaocookies.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pe.com.dpaocookies.dao.CategoriaDAO;
import pe.com.dpaocookies.dao.InsumoDAO;
import pe.com.dpaocookies.dao.KardexDAO;
import pe.com.dpaocookies.dao.PackDAO;
import pe.com.dpaocookies.dto.PackDTO;
import pe.com.dpaocookies.model.PackPrecio;
import pe.com.dpaocookies.dao.ProductoDAO;
import pe.com.dpaocookies.dto.InsumoDTO;
import pe.com.dpaocookies.dto.MovimientoDTO;
import pe.com.dpaocookies.dto.ProductoDTO;
import pe.com.dpaocookies.dto.ResumenTableroDTO;
import pe.com.dpaocookies.dto.SolicitudMovimientoDTO;
import pe.com.dpaocookies.exception.NegocioException;
import pe.com.dpaocookies.exception.ServicioException;
import pe.com.dpaocookies.exception.StockInsuficienteException;
import pe.com.dpaocookies.model.Categoria;
import pe.com.dpaocookies.model.ClaseItem;
import pe.com.dpaocookies.model.Insumo;
import pe.com.dpaocookies.model.MovimientoKardex;
import pe.com.dpaocookies.model.Producto;
import pe.com.dpaocookies.model.TipoMovimiento;
import pe.com.dpaocookies.support.JdbcFalso;
import pe.com.dpaocookies.util.ProveedorConexion;

/** Orquestación de la fachada con DAO simulados: validación, mapeo a DTO y traducción de errores. */
class InventarioFacadeTest {

    private static final ProveedorConexion SIN_CONEXION = () -> {
        throw new SQLException("Las pruebas unitarias no abren conexiones");
    };

    private InsumoFalso insumos;
    private ProductoFalso productos;
    private KardexFalso kardex;
    private PackFalso packs;
    private InventarioFacade facade;

    @BeforeEach
    void preparar() {
        insumos = new InsumoFalso();
        productos = new ProductoFalso();
        kardex = new KardexFalso();
        packs = new PackFalso();
        facade = new InventarioFacade(insumos, productos, new CategoriaFalsa(), kardex, packs);
    }

    // ------------------------------------------------------------ Consultas

    @Test
    void listarInsumosDevuelveDTOConCategoriaYAlerta() {
        insumos.lista.add(insumo(1, "HAR-001", "4.00", "5.00"));
        insumos.lista.add(insumo(2, "MAN-001", "9.00", "2.00"));

        List<InsumoDTO> resultado = facade.listarInsumos();

        assertEquals(2, resultado.size());
        assertEquals("Materias primas", resultado.get(0).getNombreCategoria());
        assertTrue(resultado.get(0).isBajoStock());
        assertFalse(resultado.get(1).isBajoStock());
    }

    @Test
    void resumenReuneContadoresAlertasYUltimosMovimientos() {
        insumos.lista.add(insumo(1, "HAR-001", "5.00", "5.00"));
        insumos.lista.add(insumo(2, "MAN-001", "9.00", "2.00"));
        productos.lista.add(new Producto(1, "GAL-001", "Galleta", BigDecimal.ONE, BigDecimal.TEN, 2));
        kardex.historial = List.of(movimiento(TipoMovimiento.ENTRADA, "3.00"));

        ResumenTableroDTO resumen = facade.obtenerResumen();

        assertEquals(2, resumen.getTotalInsumos());
        assertEquals(1, resumen.getTotalProductos());
        assertEquals(1, resumen.getCantidadAlertas());
        assertEquals("HAR-001", resumen.getAlertas().get(0).getCodigo());
        assertEquals(1, resumen.getUltimosMovimientos().size());
        assertEquals(5, kardex.limitePedido);
    }

    @Test
    void resumenDelDiaValorizaLasVentasYOrdenaLosMasVendidos() {
        productos.lista.add(new Producto(1, "G01", "Chispas", new BigDecimal("7.00"), new BigDecimal("6.00"), 2));
        productos.lista.add(new Producto(16, "G16", "Pistacho", new BigDecimal("15.00"), new BigDecimal("2.00"), 2));
        productos.lista.add(new Producto(15, "G15", "Snicker", new BigDecimal("11.00"), new BigDecimal("0.00"), 2));
        kardex.historial = List.of(venta(1, "-2.00"), venta(16, "-1.00"), venta(1, "-1.00"));

        ResumenTableroDTO resumen = facade.obtenerResumen(LocalDate.of(2026, 8, 15));

        assertEquals(TipoMovimiento.VENTA, kardex.primerFiltroTipo, "lee solo las ventas del día");
        assertEquals(LocalDate.of(2026, 8, 15), kardex.primerDesde);
        assertEquals(0, new BigDecimal("4").compareTo(resumen.getUnidadesVendidasHoy()));
        assertEquals(new BigDecimal("36.00"), resumen.getMontoVendidoHoy());
        assertEquals("Chispas", resumen.getMasVendidos().get(0).getNombre(), "3 × 7.00 = 21.00 supera a 15.00");
        assertEquals(100, resumen.getMasVendidos().get(0).getPorcentaje());
        assertEquals(71, resumen.getMasVendidos().get(1).getPorcentaje());
        assertEquals(1, resumen.getAgotados().size());
        assertEquals("Snicker", resumen.getAgotados().get(0).getNombre());
        assertEquals(1, resumen.getCantidadAtencion());
    }

    @Test
    void listarPacksAgrupaPreciosYSaboresPorCategoria() {
        productos.lista.add(new Producto(1, "G01", "Chispas", BigDecimal.ONE, BigDecimal.ONE, 3));
        productos.lista.add(new Producto(14, "G14", "Nutella", BigDecimal.ONE, BigDecimal.ONE, 5));
        packs.filas = List.of(
                new PackPrecio(3, "Clásico", 2, new BigDecimal("12.00")),
                new PackPrecio(3, "Clásico", 4, new BigDecimal("22.00")),
                new PackPrecio(5, "Premium", 2, new BigDecimal("20.00")));

        List<PackDTO> resultado = facade.listarPacks();

        assertEquals(2, resultado.size());
        assertEquals("Clásico", resultado.get(0).getCategoria());
        assertEquals(List.of("Chispas"), resultado.get(0).getSabores());
        assertEquals(2, resultado.get(0).getPrecios().size());
        assertEquals(new BigDecimal("22.00"), resultado.get(0).getPrecios().get(1).getPrecio());
        assertEquals(List.of("Nutella"), resultado.get(1).getSabores());
    }

    @Test
    void sinTablaDePacksNoHayPacksQueMostrar() {
        packs.error = new SQLException("Table 'pack_precio' doesn't exist", "42S02", 1146);
        assertTrue(facade.listarPacks().isEmpty());
        packs.error = new SQLException("Communications link failure", "08S01", 0);
        assertThrows(ServicioException.class, facade::listarPacks);
    }

    @Test
    void consultarKardexConLimiteLoPasaAlDAO() {
        facade.consultarKardex("", null, null, 120);
        assertEquals(120, kardex.limitePedido);
    }

    @Test
    void consultarKardexConvierteElFiltroDeTipo() {
        kardex.historial = List.of(movimiento(TipoMovimiento.VENTA, "-1.00"));
        List<MovimientoDTO> resultado = facade.consultarKardex("venta", null, null);

        assertEquals(TipoMovimiento.VENTA, kardex.filtroTipo);
        assertEquals("VENTA", resultado.get(0).getTipoMovimiento());
        assertFalse(resultado.get(0).isEntrada());

        facade.consultarKardex("", null, null);
        assertNull(kardex.filtroTipo, "vacío incluye todos los tipos");
    }

    @Test
    void consultarKardexRechazaRangoInvertidoYTipoDesconocido() {
        assertThrows(NegocioException.class,
                () -> facade.consultarKardex(null, LocalDate.of(2026, 10, 9), LocalDate.of(2026, 10, 8)));
        assertThrows(NegocioException.class, () -> facade.consultarKardex("REGALO", null, null));
    }

    // ------------------------------------------------------- Mantenimientos

    @Test
    void registrarInsumoRecortaTextosYDevuelveId() {
        InsumoDTO dto = insumoDTO("  AZU-001 ", " Azúcar rubia ");
        assertEquals(7, facade.registrarInsumo(dto));
        assertEquals("AZU-001", insumos.registrado.getCodigo());
        assertEquals("Azúcar rubia", insumos.registrado.getNombre());
    }

    @Test
    void registrarInsumoValidaAntesDeLlegarAlDAO() {
        assertThrows(NegocioException.class, () -> facade.registrarInsumo(insumoDTO(" ", "Azúcar")));
        InsumoDTO sinCategoria = insumoDTO("AZU-001", "Azúcar");
        sinCategoria.setIdCategoria(0);
        assertThrows(NegocioException.class, () -> facade.registrarInsumo(sinCategoria));
        InsumoDTO negativo = insumoDTO("AZU-001", "Azúcar");
        negativo.setStockActual(new BigDecimal("-1"));
        NegocioException error = assertThrows(NegocioException.class, () -> facade.registrarInsumo(negativo));
        assertTrue(error.getMessage().startsWith("Stock actual"));
        assertNull(insumos.registrado, "ningún dato inválido llega al DAO");
    }

    @Test
    void codigoDuplicadoSeTraduceAMensajeDeNegocio() {
        insumos.error = new SQLException("Duplicate entry", "23000", 1062);
        NegocioException error = assertThrows(NegocioException.class,
                () -> facade.registrarInsumo(insumoDTO("HAR-001", "Harina")));
        assertEquals("El código ya existe.", error.getMessage());
    }

    @Test
    void falloTecnicoSeOcultaAlUsuarioPeroConservaLaCausa() {
        SQLException causa = new SQLException("Communications link failure", "08S01", 0);
        insumos.error = causa;
        ServicioException error = assertThrows(ServicioException.class, facade::listarInsumos);
        assertEquals("No se pudo consultar la información. Intente nuevamente.", error.getMessage());
        assertSame(causa, error.getCause());
    }

    @Test
    void guardarProductoNuevoOEditado() {
        ProductoDTO nuevo = productoDTO(0);
        assertEquals(11, facade.guardarProducto(nuevo));
        assertEquals(0, productos.registrado.getStockActual().signum(), "stock inicial cero");

        assertEquals(3, facade.guardarProducto(productoDTO(3)));
        assertEquals(3, productos.actualizado.getIdProducto());

        productos.existe = false;
        assertThrows(NegocioException.class, () -> facade.guardarProducto(productoDTO(404)));
    }

    // ----------------------------------------------------------------- Kardex

    @Test
    void registrarMovimientoValidaYDelegaEnElKardex() {
        kardex.respuesta = movimiento(TipoMovimiento.VENTA, "-2.00");

        Optional<MovimientoDTO> resultado = facade.registrarMovimiento(
                new SolicitudMovimientoDTO("producto", 3, "venta", new BigDecimal("2.00"), "  Venta stand "));

        assertEquals(ClaseItem.PRODUCTO, kardex.clase);
        assertEquals(3, kardex.id);
        assertEquals(TipoMovimiento.VENTA, kardex.tipo);
        assertEquals(new BigDecimal("2.00"), kardex.valor);
        assertEquals("Venta stand", kardex.motivo);
        assertTrue(resultado.isPresent());
        assertEquals(new BigDecimal("10.00"), resultado.get().getStockAnterior());
        assertEquals(new BigDecimal("8.00"), resultado.get().getStockPosterior());
    }

    @Test
    void cantidadCeroSoloSeAdmiteComoConteoDeAjuste() {
        assertThrows(NegocioException.class, () -> facade.registrarMovimiento(
                new SolicitudMovimientoDTO("INSUMO", 1, "SALIDA", BigDecimal.ZERO, "Producción")));
        assertNull(kardex.tipo);

        facade.registrarMovimiento(new SolicitudMovimientoDTO("INSUMO", 1, "AJUSTE", BigDecimal.ZERO, "Conteo"));
        assertEquals(TipoMovimiento.AJUSTE, kardex.tipo);
    }

    @Test
    void rechazaSolicitudesMalFormadas() {
        BigDecimal uno = BigDecimal.ONE;
        assertThrows(NegocioException.class, () -> facade.registrarMovimiento(null));
        assertThrows(NegocioException.class, () -> facade.registrarMovimiento(
                new SolicitudMovimientoDTO("CLIENTE", 1, "ENTRADA", uno, "x")));
        assertThrows(NegocioException.class, () -> facade.registrarMovimiento(
                new SolicitudMovimientoDTO("INSUMO", 1, "DEVOLUCION", uno, "x")));
        assertThrows(NegocioException.class, () -> facade.registrarMovimiento(
                new SolicitudMovimientoDTO("INSUMO", 0, "ENTRADA", uno, "x")));
        assertThrows(NegocioException.class, () -> facade.registrarMovimiento(
                new SolicitudMovimientoDTO("INSUMO", 1, "ENTRADA", new BigDecimal("1.001"), "x")));
        assertThrows(NegocioException.class, () -> facade.registrarMovimiento(
                new SolicitudMovimientoDTO("INSUMO", 1, "ENTRADA", uno, " ")));
        assertNull(kardex.tipo, "nada inválido llega al DAO");
    }

    @Test
    void ajustarStockInsumoSeRegistraComoAjuste() {
        assertTrue(facade.ajustarStockInsumo(1, new BigDecimal("4.00")).isEmpty());
        assertEquals(ClaseItem.INSUMO, kardex.clase);
        assertEquals(TipoMovimiento.AJUSTE, kardex.tipo);
        assertEquals(InventarioFacade.MOTIVO_AJUSTE_INVENTARIO, kardex.motivo);
    }

    @Test
    void stockInsuficienteLlegaAlControladorComoErrorDeNegocio() {
        kardex.errorNegocio = new StockInsuficienteException(new BigDecimal("1.00"), new BigDecimal("3.00"));
        NegocioException error = assertThrows(NegocioException.class, () -> facade.registrarMovimiento(
                new SolicitudMovimientoDTO("PRODUCTO", 1, "VENTA", new BigDecimal("3.00"), "Venta")));
        assertEquals("Stock insuficiente: disponible 1.00, solicitado 3.00.", error.getMessage());
    }

    @Test
    void fachadaConKardexRealPropagaElRollbackComoErrorTecnico() {
        JdbcFalso jdbc = new JdbcFalso().conStock("5.00").fallandoEn("INSERT");
        InventarioFacade conJdbc = new InventarioFacade(insumos, productos, new CategoriaFalsa(),
                new KardexDAO(jdbc.proveedor()));

        ServicioException error = assertThrows(ServicioException.class, () -> conJdbc.registrarMovimiento(
                new SolicitudMovimientoDTO("INSUMO", 1, "ENTRADA", BigDecimal.ONE, "Compra")));
        assertEquals("No se pudo guardar la operación. Intente nuevamente.", error.getMessage());
        assertTrue(jdbc.eventos.contains("rollback"));
        assertFalse(jdbc.eventos.contains("commit"));
    }

    // ----------------------------------------------------------- Datos de prueba

    private static Insumo insumo(int id, String codigo, String stock, String minimo) {
        return new Insumo(id, codigo, "Insumo " + codigo, "kg", new BigDecimal(stock),
                new BigDecimal(minimo), BigDecimal.ONE, 1);
    }

    private static InsumoDTO insumoDTO(String codigo, String nombre) {
        InsumoDTO dto = new InsumoDTO();
        dto.setCodigo(codigo);
        dto.setNombre(nombre);
        dto.setUnidadMedida("kg");
        dto.setStockActual(new BigDecimal("2.00"));
        dto.setStockMinimo(new BigDecimal("3.00"));
        dto.setCostoUnitario(new BigDecimal("4.00"));
        dto.setIdCategoria(1);
        return dto;
    }

    private static ProductoDTO productoDTO(int id) {
        ProductoDTO dto = new ProductoDTO();
        dto.setIdProducto(id);
        dto.setCodigo("GAL-00" + id);
        dto.setNombre("Galleta");
        dto.setPrecioVenta(new BigDecimal("6.50"));
        dto.setIdCategoria(2);
        return dto;
    }

    private static MovimientoKardex movimiento(TipoMovimiento tipo, String cantidad) {
        MovimientoKardex m = new MovimientoKardex();
        m.setClaseItem(ClaseItem.PRODUCTO);
        m.setIdItem(3);
        m.setCodigoItem("GAL-003");
        m.setNombreItem("Galleta de vainilla");
        m.setTipoMovimiento(tipo);
        m.setCantidad(new BigDecimal(cantidad));
        m.setStockAnterior(new BigDecimal("10.00"));
        m.setStockPosterior(new BigDecimal("10.00").add(new BigDecimal(cantidad)));
        m.setObservacion("Prueba");
        return m;
    }

    private static MovimientoKardex venta(int idProducto, String cantidad) {
        MovimientoKardex m = movimiento(TipoMovimiento.VENTA, cantidad);
        m.setIdItem(idProducto);
        return m;
    }

    private static final class PackFalso extends PackDAO {
        List<PackPrecio> filas = List.of();
        SQLException error;

        PackFalso() {
            super(SIN_CONEXION);
        }

        @Override
        public List<PackPrecio> listar() throws SQLException {
            if (error != null) {
                throw error;
            }
            return filas;
        }
    }

    private static final class CategoriaFalsa extends CategoriaDAO {
        CategoriaFalsa() {
            super(SIN_CONEXION);
        }

        @Override
        public List<Categoria> listar() {
            return List.of(new Categoria(1, "Materias primas"), new Categoria(2, "Galletas"));
        }
    }

    private static final class InsumoFalso extends InsumoDAO {
        final List<Insumo> lista = new ArrayList<>();
        Insumo registrado;
        SQLException error;

        InsumoFalso() {
            super(SIN_CONEXION);
        }

        @Override
        public List<Insumo> listar() throws SQLException {
            if (error != null) {
                throw error;
            }
            return lista;
        }

        @Override
        public int registrar(Insumo insumo) throws SQLException {
            if (error != null) {
                throw error;
            }
            registrado = insumo;
            return 7;
        }
    }

    private static final class ProductoFalso extends ProductoDAO {
        final List<Producto> lista = new ArrayList<>();
        Producto registrado;
        Producto actualizado;
        boolean existe = true;

        ProductoFalso() {
            super(SIN_CONEXION);
        }

        @Override
        public List<Producto> listar() {
            return lista;
        }

        @Override
        public int registrar(Producto producto) {
            registrado = producto;
            return 11;
        }

        @Override
        public boolean actualizar(Producto producto) {
            actualizado = producto;
            return existe;
        }
    }

    private static final class KardexFalso extends KardexDAO {
        ClaseItem clase;
        int id;
        TipoMovimiento tipo;
        BigDecimal valor;
        String motivo;
        MovimientoKardex respuesta;
        RuntimeException errorNegocio;
        List<MovimientoKardex> historial = List.of();
        TipoMovimiento filtroTipo;
        TipoMovimiento primerFiltroTipo;
        LocalDate primerDesde;
        boolean consultado;
        int limitePedido = -1;

        KardexFalso() {
            super(SIN_CONEXION);
        }

        @Override
        public MovimientoKardex registrar(ClaseItem clase, int idItem, TipoMovimiento tipo,
                BigDecimal valor, String observacion) {
            this.clase = clase;
            this.id = idItem;
            this.tipo = tipo;
            this.valor = valor;
            this.motivo = observacion;
            if (errorNegocio != null) {
                throw errorNegocio;
            }
            return respuesta;
        }

        @Override
        public List<MovimientoKardex> listar(TipoMovimiento tipo, LocalDate desde, LocalDate hasta, int limite) {
            if (!consultado) {
                consultado = true;
                primerFiltroTipo = tipo;
                primerDesde = desde;
            }
            filtroTipo = tipo;
            limitePedido = limite;
            return historial;
        }
    }
}
