package pe.com.dpaocookies.bean;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import pe.com.dpaocookies.dto.CategoriaDTO;
import pe.com.dpaocookies.dto.InsumoDTO;
import pe.com.dpaocookies.dto.MovimientoDTO;
import pe.com.dpaocookies.dto.PackDTO;
import pe.com.dpaocookies.dto.ProductoDTO;
import pe.com.dpaocookies.dto.ResumenTableroDTO;
import pe.com.dpaocookies.dto.SolicitudMovimientoDTO;
import pe.com.dpaocookies.facade.InventarioFacade;

/** Dobles de prueba de los beans: una fachada con respuestas fijas y una vista que anota lo pedido. */
final class Dobles {

    private Dobles() {
    }

    /** Vista que guarda los mensajes y la sesión en memoria. */
    static class VistaFalsa implements Vista {
        final List<String> exitos = new ArrayList<>();
        final List<String> errores = new ArrayList<>();
        final List<String> erroresEn = new ArrayList<>();
        String usuario;
        String local;
        boolean sesionCerrada;

        @Override
        public void exito(String mensaje) {
            exitos.add(mensaje);
        }

        @Override
        public void error(String mensaje) {
            errores.add(mensaje);
        }

        @Override
        public void errorEn(String idCliente, String mensaje) {
            erroresEn.add(idCliente + "|" + mensaje);
        }

        @Override
        public void iniciarSesion(String usuario, String local) {
            this.usuario = usuario;
            this.local = local;
        }

        @Override
        public void cerrarSesion() {
            sesionCerrada = true;
            usuario = null;
        }

        @Override
        public String usuarioActual() {
            return usuario;
        }
    }

    /**
     * Fachada sin base de datos: cada consulta devuelve la lista configurada y cada operación
     * anota su argumento. Si {@code falla} tiene una excepción, las operaciones la lanzan.
     */
    static class FachadaFalsa extends InventarioFacade {
        List<CategoriaDTO> categorias = List.of();
        List<InsumoDTO> insumos = List.of();
        List<ProductoDTO> productos = List.of();
        List<MovimientoDTO> movimientos = List.of();
        List<PackDTO> packs = List.of();
        ResumenTableroDTO resumen = new ResumenTableroDTO(0, 0, List.of(), List.of());
        Optional<MovimientoDTO> respuestaMovimiento = Optional.empty();
        RuntimeException falla;
        Supplier<RuntimeException> fallaConsulta;

        int consultasResumen;
        InsumoDTO insumoRegistrado;
        ProductoDTO productoGuardado;
        SolicitudMovimientoDTO solicitud;
        int idAjustado;
        BigDecimal stockAjustado;
        String filtroTipo;
        LocalDate filtroDesde;
        LocalDate filtroHasta;
        int filtroLimite;

        FachadaFalsa() {
            super(null, null, null, null, null);
        }

        private void consultar() {
            if (fallaConsulta != null) {
                throw fallaConsulta.get();
            }
        }

        private void operar() {
            if (falla != null) {
                throw falla;
            }
        }

        @Override
        public List<CategoriaDTO> listarCategorias() {
            consultar();
            return categorias;
        }

        @Override
        public List<InsumoDTO> listarInsumos() {
            consultar();
            return insumos;
        }

        @Override
        public List<ProductoDTO> listarProductos() {
            consultar();
            return productos;
        }

        @Override
        public List<PackDTO> listarPacks() {
            consultar();
            return packs;
        }

        @Override
        public ResumenTableroDTO obtenerResumen() {
            consultasResumen++;
            consultar();
            return resumen;
        }

        @Override
        public List<MovimientoDTO> consultarKardex(String tipo, LocalDate desde, LocalDate hasta, int limite) {
            filtroTipo = tipo;
            filtroDesde = desde;
            filtroHasta = hasta;
            filtroLimite = limite;
            consultar();
            return movimientos;
        }

        @Override
        public int registrarInsumo(InsumoDTO datos) {
            insumoRegistrado = datos;
            operar();
            return 10;
        }

        @Override
        public int guardarProducto(ProductoDTO datos) {
            productoGuardado = datos;
            operar();
            return datos.getIdProducto() == 0 ? 17 : datos.getIdProducto();
        }

        @Override
        public Optional<MovimientoDTO> registrarMovimiento(SolicitudMovimientoDTO solicitud) {
            this.solicitud = solicitud;
            operar();
            return respuestaMovimiento;
        }

        @Override
        public Optional<MovimientoDTO> ajustarStockInsumo(int idInsumo, BigDecimal stockContado) {
            idAjustado = idInsumo;
            stockAjustado = stockContado;
            operar();
            return respuestaMovimiento;
        }
    }

    // ------------------------------------------------------------- Datos

    static CategoriaDTO categoria(int id, String nombre) {
        return new CategoriaDTO(id, nombre);
    }

    static InsumoDTO insumo(int id, String nombre, String stock, String minimo, String unidad) {
        InsumoDTO dto = new InsumoDTO();
        dto.setIdInsumo(id);
        dto.setCodigo("INS-00" + id);
        dto.setNombre(nombre);
        dto.setUnidadMedida(unidad);
        dto.setStockActual(new BigDecimal(stock));
        dto.setStockMinimo(new BigDecimal(minimo));
        dto.setCostoUnitario(new BigDecimal("1.00"));
        dto.setIdCategoria(4);
        dto.setBajoStock(new BigDecimal(stock).compareTo(new BigDecimal(minimo)) <= 0);
        return dto;
    }

    static ProductoDTO producto(int id, String nombre, int idCategoria, String precio, String stock) {
        ProductoDTO dto = new ProductoDTO();
        dto.setIdProducto(id);
        dto.setCodigo(String.format("G%02d", id));
        dto.setNombre(nombre);
        dto.setIdCategoria(idCategoria);
        dto.setPrecioVenta(new BigDecimal(precio));
        dto.setStockActual(new BigDecimal(stock));
        return dto;
    }

    static MovimientoDTO movimiento(String item, String tipo, String cantidad, String antes, String despues, Date fecha) {
        MovimientoDTO dto = new MovimientoDTO();
        dto.setNombreItem(item);
        dto.setTipoMovimiento(tipo);
        dto.setCantidad(new BigDecimal(cantidad));
        dto.setStockAnterior(new BigDecimal(antes));
        dto.setStockPosterior(new BigDecimal(despues));
        dto.setFechaRegistro(fecha);
        return dto;
    }
}
