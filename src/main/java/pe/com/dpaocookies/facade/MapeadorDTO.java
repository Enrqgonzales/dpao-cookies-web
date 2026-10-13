package pe.com.dpaocookies.facade;

import java.util.List;
import java.util.Map;
import pe.com.dpaocookies.dto.CategoriaDTO;
import pe.com.dpaocookies.dto.InsumoDTO;
import pe.com.dpaocookies.dto.MovimientoDTO;
import pe.com.dpaocookies.dto.ProductoDTO;
import pe.com.dpaocookies.model.Categoria;
import pe.com.dpaocookies.model.Insumo;
import pe.com.dpaocookies.model.MovimientoKardex;
import pe.com.dpaocookies.model.Producto;

/** Conversión de entidades a DTO; las entidades no salen de la fachada. */
final class MapeadorDTO {

    private MapeadorDTO() {
    }

    static CategoriaDTO categoria(Categoria categoria) {
        return new CategoriaDTO(categoria.getIdCategoria(), categoria.getNombre());
    }

    static List<CategoriaDTO> categorias(List<Categoria> categorias) {
        return categorias.stream().map(MapeadorDTO::categoria).toList();
    }

    static InsumoDTO insumo(Insumo insumo, Map<Integer, String> nombresCategoria) {
        InsumoDTO dto = new InsumoDTO();
        dto.setIdInsumo(insumo.getIdInsumo());
        dto.setCodigo(insumo.getCodigo());
        dto.setNombre(insumo.getNombre());
        dto.setUnidadMedida(insumo.getUnidadMedida());
        dto.setStockActual(insumo.getStockActual());
        dto.setStockMinimo(insumo.getStockMinimo());
        dto.setCostoUnitario(insumo.getCostoUnitario());
        dto.setIdCategoria(insumo.getIdCategoria());
        dto.setNombreCategoria(nombresCategoria.get(insumo.getIdCategoria()));
        dto.setBajoStock(insumo.isBajoStock());
        return dto;
    }

    static ProductoDTO producto(Producto producto, Map<Integer, String> nombresCategoria) {
        ProductoDTO dto = new ProductoDTO();
        dto.setIdProducto(producto.getIdProducto());
        dto.setCodigo(producto.getCodigo());
        dto.setNombre(producto.getNombre());
        dto.setPrecioVenta(producto.getPrecioVenta());
        dto.setStockActual(producto.getStockActual());
        dto.setIdCategoria(producto.getIdCategoria());
        dto.setNombreCategoria(nombresCategoria.get(producto.getIdCategoria()));
        return dto;
    }

    static MovimientoDTO movimiento(MovimientoKardex movimiento) {
        MovimientoDTO dto = new MovimientoDTO();
        dto.setIdMovimiento(movimiento.getIdMovimiento());
        dto.setClaseItem(movimiento.getClaseItem().name());
        dto.setIdItem(movimiento.getIdItem());
        dto.setCodigoItem(movimiento.getCodigoItem());
        dto.setNombreItem(movimiento.getNombreItem());
        dto.setTipoMovimiento(movimiento.getTipoMovimiento().name());
        dto.setCantidad(movimiento.getCantidad());
        dto.setStockAnterior(movimiento.getStockAnterior());
        dto.setStockPosterior(movimiento.getStockPosterior());
        dto.setObservacion(movimiento.getObservacion());
        dto.setFechaRegistro(movimiento.getFechaRegistro());
        return dto;
    }

    static List<MovimientoDTO> movimientos(List<MovimientoKardex> movimientos) {
        return movimientos.stream().map(MapeadorDTO::movimiento).toList();
    }
}
