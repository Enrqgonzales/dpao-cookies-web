package pe.com.dpaocookies.facade;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import pe.com.dpaocookies.dao.CategoriaDAO;
import pe.com.dpaocookies.dao.InsumoDAO;
import pe.com.dpaocookies.dao.KardexDAO;
import pe.com.dpaocookies.dao.PackDAO;
import pe.com.dpaocookies.dao.ProductoDAO;
import pe.com.dpaocookies.dto.CategoriaDTO;
import pe.com.dpaocookies.dto.InsumoDTO;
import pe.com.dpaocookies.dto.MovimientoDTO;
import pe.com.dpaocookies.dto.PackDTO;
import pe.com.dpaocookies.dto.ProductoDTO;
import pe.com.dpaocookies.dto.ResumenTableroDTO;
import pe.com.dpaocookies.dto.SolicitudMovimientoDTO;
import pe.com.dpaocookies.dto.VentaSaborDTO;
import pe.com.dpaocookies.model.PackPrecio;
import pe.com.dpaocookies.exception.NegocioException;
import pe.com.dpaocookies.exception.ServicioException;
import pe.com.dpaocookies.model.Categoria;
import pe.com.dpaocookies.model.ClaseItem;
import pe.com.dpaocookies.model.Insumo;
import pe.com.dpaocookies.model.MovimientoKardex;
import pe.com.dpaocookies.model.Producto;
import pe.com.dpaocookies.model.TipoMovimiento;
import pe.com.dpaocookies.util.Validacion;

/**
 * Fachada del inventario: único punto de entrada de los controladores a la
 * lógica de negocio. Valida los DTO recibidos, coordina los DAO, convierte las
 * entidades en DTO y traduce los errores SQL a mensajes para el usuario.
 */
public class InventarioFacade {

    private static final Logger LOGGER = Logger.getLogger(InventarioFacade.class.getName());
    private static final int MOVIMIENTOS_TABLERO = 5;
    private static final int MAS_VENDIDOS = 7;
    private static final int TABLA_NO_EXISTE = 1146;
    /** El negocio opera en Lima: "hoy" es el día de Lima, no el del servidor. */
    public static final ZoneId ZONA = ZoneId.of("America/Lima");
    static final String MOTIVO_AJUSTE_INVENTARIO = "Ajuste desde inventario";

    private final InsumoDAO insumoDAO;
    private final ProductoDAO productoDAO;
    private final CategoriaDAO categoriaDAO;
    private final KardexDAO kardexDAO;
    private final PackDAO packDAO;

    public InventarioFacade() {
        this(new InsumoDAO(), new ProductoDAO(), new CategoriaDAO(), new KardexDAO(), new PackDAO());
    }

    public InventarioFacade(InsumoDAO insumoDAO, ProductoDAO productoDAO,
            CategoriaDAO categoriaDAO, KardexDAO kardexDAO) {
        this(insumoDAO, productoDAO, categoriaDAO, kardexDAO, new PackDAO());
    }

    public InventarioFacade(InsumoDAO insumoDAO, ProductoDAO productoDAO,
            CategoriaDAO categoriaDAO, KardexDAO kardexDAO, PackDAO packDAO) {
        this.insumoDAO = insumoDAO;
        this.productoDAO = productoDAO;
        this.categoriaDAO = categoriaDAO;
        this.kardexDAO = kardexDAO;
        this.packDAO = packDAO;
    }

    // ---------------------------------------------------------------- Consultas

    public List<CategoriaDTO> listarCategorias() {
        return consultar("listar categorías", () -> MapeadorDTO.categorias(categoriaDAO.listar()));
    }

    public List<InsumoDTO> listarInsumos() {
        return consultar("listar insumos", () -> {
            Map<Integer, String> categorias = nombresCategoria();
            return insumoDAO.listar().stream().map(i -> MapeadorDTO.insumo(i, categorias)).toList();
        });
    }

    public List<ProductoDTO> listarProductos() {
        return consultar("listar productos", () -> {
            Map<Integer, String> categorias = nombresCategoria();
            return productoDAO.listar().stream().map(p -> MapeadorDTO.producto(p, categorias)).toList();
        });
    }

    /**
     * @param tipo nombre del tipo de movimiento; vacío o {@code null} incluye todos
     * @param desde fecha inicial inclusiva, opcional
     * @param hasta fecha final inclusiva, opcional
     */
    public List<MovimientoDTO> consultarKardex(String tipo, LocalDate desde, LocalDate hasta) {
        TipoMovimiento filtro = tipo == null || tipo.isBlank() ? null : TipoMovimiento.desde(tipo);
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new NegocioException("La fecha inicial no puede ser posterior a la fecha final.");
        }
        return consultarKardex(tipo, desde, hasta, 0);
    }

    /** Igual que {@link #consultarKardex(String, LocalDate, LocalDate)}, con un máximo de filas (0 = todas). */
    public List<MovimientoDTO> consultarKardex(String tipo, LocalDate desde, LocalDate hasta, int limite) {
        TipoMovimiento filtro = tipo == null || tipo.isBlank() ? null : TipoMovimiento.desde(tipo);
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new NegocioException("La fecha inicial no puede ser posterior a la fecha final.");
        }
        return consultar("consultar Kardex",
                () -> MapeadorDTO.movimientos(kardexDAO.listar(filtro, desde, hasta, limite)));
    }

    public ResumenTableroDTO obtenerResumen() {
        return obtenerResumen(LocalDate.now(ZONA));
    }

    /**
     * Resumen del tablero para un día: existencias, alertas y lo vendido ese día según los
     * movimientos VENTA del Kardex, valorizado al precio de venta actual de cada sabor.
     */
    public ResumenTableroDTO obtenerResumen(LocalDate dia) {
        return consultar("obtener resumen del tablero", () -> {
            List<InsumoDTO> insumos = insumoDAO.listar().stream()
                    .map(i -> MapeadorDTO.insumo(i, Map.of())).toList();
            List<InsumoDTO> alertas = insumos.stream().filter(InsumoDTO::isBajoStock).toList();
            List<Producto> productos = productoDAO.listar();
            Map<Integer, Producto> porId = productos.stream()
                    .collect(Collectors.toMap(Producto::getIdProducto, p -> p));

            Map<Integer, BigDecimal> unidadesPorSabor = new LinkedHashMap<>();
            for (MovimientoKardex venta : kardexDAO.listar(TipoMovimiento.VENTA, dia, dia, 0)) {
                if (venta.getClaseItem() == ClaseItem.PRODUCTO) {
                    unidadesPorSabor.merge(venta.getIdItem(), venta.getCantidad().negate(), BigDecimal::add);
                }
            }
            BigDecimal unidades = BigDecimal.ZERO;
            BigDecimal monto = BigDecimal.ZERO.setScale(2);
            List<VentaSaborDTO> ventas = new ArrayList<>();
            for (Map.Entry<Integer, BigDecimal> e : unidadesPorSabor.entrySet()) {
                Producto p = porId.get(e.getKey());
                if (p == null) {
                    continue;
                }
                BigDecimal importe = e.getValue().multiply(p.getPrecioVenta()).setScale(2, RoundingMode.HALF_UP);
                unidades = unidades.add(e.getValue());
                monto = monto.add(importe);
                ventas.add(new VentaSaborDTO(p.getCodigo(), p.getNombre(), e.getValue(), importe, 0));
            }
            ventas.sort(Comparator.comparing(VentaSaborDTO::getMonto).reversed()
                    .thenComparing(VentaSaborDTO::getNombre));
            BigDecimal mayor = ventas.isEmpty() ? BigDecimal.ONE : ventas.get(0).getMonto();
            List<VentaSaborDTO> masVendidos = ventas.stream().limit(MAS_VENDIDOS)
                    .map(v -> new VentaSaborDTO(v.getCodigo(), v.getNombre(), v.getUnidades(), v.getMonto(),
                            mayor.signum() == 0 ? 0 : v.getMonto().multiply(BigDecimal.valueOf(100))
                                    .divide(mayor, 0, RoundingMode.HALF_UP).intValue()))
                    .toList();
            List<ProductoDTO> agotados = productos.stream()
                    .filter(p -> p.getStockActual() != null && p.getStockActual().signum() == 0)
                    .map(p -> MapeadorDTO.producto(p, Map.of())).toList();
            List<MovimientoDTO> recientes = MapeadorDTO.movimientos(
                    kardexDAO.listar(null, null, null, MOVIMIENTOS_TABLERO));
            return new ResumenTableroDTO(insumos.size(), productos.size(), alertas, recientes,
                    unidades, monto, masVendidos, agotados);
        });
    }

    /**
     * Packs de delivery por categoría, con sus sabores. Si la base aún no tiene la tabla
     * {@code pack_precio} (no se cargó {@code sql/catalogo-dpao.sql}), no hay packs que mostrar.
     */
    public List<PackDTO> listarPacks() {
        return consultar("listar packs", () -> {
            List<PackPrecio> filas;
            try {
                filas = packDAO.listar();
            } catch (SQLException exception) {
                if (exception.getErrorCode() != TABLA_NO_EXISTE) {
                    throw exception;
                }
                LOGGER.info("La tabla pack_precio no existe; se muestra la lista de packs vacía.");
                return List.of();
            }
            List<Producto> productos = productoDAO.listar();
            Map<Integer, List<PackDTO.Precio>> precios = new LinkedHashMap<>();
            Map<Integer, String> nombres = new LinkedHashMap<>();
            for (PackPrecio fila : filas) {
                precios.computeIfAbsent(fila.getIdCategoria(), k -> new ArrayList<>())
                        .add(new PackDTO.Precio(fila.getUnidades(), fila.getPrecio()));
                nombres.put(fila.getIdCategoria(), fila.getNombreCategoria());
            }
            List<PackDTO> packs = new ArrayList<>();
            for (Map.Entry<Integer, List<PackDTO.Precio>> e : precios.entrySet()) {
                List<String> sabores = productos.stream().filter(p -> p.getIdCategoria() == e.getKey())
                        .map(Producto::getNombre).toList();
                packs.add(new PackDTO(e.getKey(), nombres.get(e.getKey()), sabores, e.getValue()));
            }
            return packs;
        });
    }

    // ---------------------------------------------------------- Mantenimientos

    /** Registra un insumo con su saldo de apertura y devuelve su identificador. */
    public int registrarInsumo(InsumoDTO datos) {
        if (datos == null) {
            throw new NegocioException("Los datos del insumo son obligatorios.");
        }
        Insumo insumo = new Insumo(
                Validacion.texto(datos.getCodigo(), 20, "Código"),
                Validacion.texto(datos.getNombre(), 100, "Nombre"),
                Validacion.texto(datos.getUnidadMedida(), 20, "Unidad de medida"),
                decimal(datos.getStockActual(), "Stock actual"),
                decimal(datos.getStockMinimo(), "Stock mínimo"),
                decimal(datos.getCostoUnitario(), "Costo unitario"),
                Validacion.identificador(datos.getIdCategoria(), "Seleccione una categoría válida."));
        return modificar("registrar insumo", () -> insumoDAO.registrar(insumo));
    }

    /**
     * Registra un producto nuevo ({@code idProducto == 0}, stock inicial cero) o
     * actualiza sus datos comerciales. El stock no se edita aquí: cambia con el Kardex.
     *
     * @return identificador del producto
     */
    public int guardarProducto(ProductoDTO datos) {
        if (datos == null || datos.getIdProducto() < 0) {
            throw new NegocioException("Los datos del producto no son válidos.");
        }
        Producto producto = new Producto(
                datos.getIdProducto(),
                Validacion.texto(datos.getCodigo(), 20, "Código"),
                Validacion.texto(datos.getNombre(), 100, "Nombre"),
                decimal(datos.getPrecioVenta(), "Precio de venta"),
                BigDecimal.ZERO,
                Validacion.identificador(datos.getIdCategoria(), "Seleccione una categoría válida."));
        if (producto.getIdProducto() == 0) {
            return modificar("registrar producto", () -> productoDAO.registrar(producto));
        }
        if (!modificar("actualizar producto", () -> productoDAO.actualizar(producto))) {
            throw new NegocioException("El producto no existe.");
        }
        return producto.getIdProducto();
    }

    // ------------------------------------------------------------------ Kardex

    /**
     * Registra un movimiento de Kardex en una transacción atómica.
     *
     * @return el movimiento con sus saldos, o vacío si un ajuste no cambia el stock
     * @throws pe.com.dpaocookies.exception.StockInsuficienteException si el saldo quedaría negativo
     */
    public Optional<MovimientoDTO> registrarMovimiento(SolicitudMovimientoDTO solicitud) {
        if (solicitud == null) {
            throw new NegocioException("Los datos del movimiento son obligatorios.");
        }
        ClaseItem clase = ClaseItem.desde(solicitud.getClaseItem());
        TipoMovimiento tipo = TipoMovimiento.desde(solicitud.getTipoMovimiento());
        int idItem = Validacion.identificador(solicitud.getIdItem(), "Seleccione un ítem válido.");
        BigDecimal valor = decimal(solicitud.getCantidad(), tipo.isAbsoluto() ? "Stock contado" : "Cantidad");
        if (!tipo.isAbsoluto() && valor.signum() == 0) {
            throw new NegocioException("La cantidad debe ser mayor que cero.");
        }
        String motivo = Validacion.texto(solicitud.getMotivo(), 255, "Motivo");

        MovimientoKardex movimiento = modificar("registrar movimiento de Kardex",
                () -> kardexDAO.registrar(clase, idItem, tipo, valor, motivo));
        return Optional.ofNullable(movimiento).map(MapeadorDTO::movimiento);
    }

    /** Fija el stock de un insumo según el conteo físico y lo registra como AJUSTE. */
    public Optional<MovimientoDTO> ajustarStockInsumo(int idInsumo, BigDecimal stockContado) {
        return registrarMovimiento(new SolicitudMovimientoDTO(ClaseItem.INSUMO.name(), idInsumo,
                TipoMovimiento.AJUSTE.name(), stockContado, MOTIVO_AJUSTE_INVENTARIO));
    }

    // --------------------------------------------------------------- Soporte

    private Map<Integer, String> nombresCategoria() throws SQLException {
        return categoriaDAO.listar().stream()
                .collect(Collectors.toMap(Categoria::getIdCategoria, Categoria::getNombre));
    }

    private static BigDecimal decimal(BigDecimal valor, String campo) {
        try {
            return Validacion.decimal(valor);
        } catch (NegocioException exception) {
            throw new NegocioException(campo + ": valor entre 0 y 99999999.99, con máximo dos decimales.");
        }
    }

    @FunctionalInterface
    private interface Operacion<T> {
        T ejecutar() throws SQLException;
    }

    private <T> T consultar(String operacion, Operacion<T> accion) {
        return ejecutar(operacion, accion, "No se pudo consultar la información. Intente nuevamente.");
    }

    private <T> T modificar(String operacion, Operacion<T> accion) {
        return ejecutar(operacion, accion, "No se pudo guardar la operación. Intente nuevamente.");
    }

    /** Traduce los códigos de error de MySQL conocidos; el resto se registra y se oculta al usuario. */
    private <T> T ejecutar(String operacion, Operacion<T> accion, String mensajeFallo) {
        try {
            return accion.ejecutar();
        } catch (SQLException exception) {
            switch (exception.getErrorCode()) {
                case 1062:
                    throw new NegocioException("El código ya existe.");
                case 1452:
                    throw new NegocioException("La categoría o el ítem seleccionado no existe.");
                case 3819:
                    throw new NegocioException("Los datos no cumplen las reglas del inventario.");
                default:
                    LOGGER.log(Level.SEVERE, "Error de persistencia al " + operacion, exception);
                    throw new ServicioException(mensajeFallo, exception);
            }
        }
    }
}
