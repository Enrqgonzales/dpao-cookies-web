package pe.com.dpaocookies.controller;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import pe.com.dpaocookies.dto.InsumoDTO;
import pe.com.dpaocookies.dto.MovimientoDTO;
import pe.com.dpaocookies.exception.AplicacionException;
import pe.com.dpaocookies.exception.NegocioException;

@WebServlet(name = "InsumoServlet", urlPatterns = {"/insumos"})
public class InsumoServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("insumos", List.of());
        request.setAttribute("categorias", List.of());
        try {
            request.setAttribute("insumos", facade.listarInsumos());
            request.setAttribute("categorias", facade.listarCategorias());
        } catch (AplicacionException exception) {
            falloEnVista(request, exception.getMessage());
        }
        mostrar(request, response, "inventario");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String accion = texto(request, "accion");
            if ("registrar".equals(accion)) {
                facade.registrarInsumo(leerInsumo(request));
                exito(request, "Insumo registrado correctamente.");
            } else if ("actualizarStock".equals(accion)) {
                Optional<MovimientoDTO> ajuste = facade.ajustarStockInsumo(
                        entero(request, "idInsumo", "El insumo no es válido."),
                        decimal(request, "nuevoStock", "El nuevo stock no es válido."));
                exito(request, ajuste
                        .map(m -> "Stock actualizado de " + m.getStockAnterior().toPlainString()
                                + " a " + m.getStockPosterior().toPlainString() + "; ajuste registrado en Kardex.")
                        .orElse("El stock no cambió; no se registró ningún movimiento."));
            } else {
                throw new NegocioException("La operación solicitada no es válida.");
            }
        } catch (AplicacionException exception) {
            fallo(request, exception.getMessage());
        }
        redirigir(request, response);
    }

    private InsumoDTO leerInsumo(HttpServletRequest request) {
        InsumoDTO insumo = new InsumoDTO();
        insumo.setCodigo(texto(request, "codigo"));
        insumo.setNombre(texto(request, "nombre"));
        insumo.setUnidadMedida(texto(request, "unidadMedida"));
        insumo.setStockActual(decimal(request, "stockActual", "El stock actual no es válido."));
        insumo.setStockMinimo(decimal(request, "stockMinimo", "El stock mínimo no es válido."));
        insumo.setCostoUnitario(decimal(request, "costoUnitario", "El costo unitario no es válido."));
        insumo.setIdCategoria(entero(request, "idCategoria", "La categoría no es válida."));
        return insumo;
    }
}
