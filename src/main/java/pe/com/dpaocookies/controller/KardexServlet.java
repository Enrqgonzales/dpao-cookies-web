package pe.com.dpaocookies.controller;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import pe.com.dpaocookies.dto.MovimientoDTO;
import pe.com.dpaocookies.dto.SolicitudMovimientoDTO;
import pe.com.dpaocookies.exception.AplicacionException;
import pe.com.dpaocookies.exception.NegocioException;
import pe.com.dpaocookies.model.TipoMovimiento;

@WebServlet(name = "KardexServlet", urlPatterns = {"/kardex"})
public class KardexServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("tiposMovimiento", List.of(TipoMovimiento.values()));
        request.setAttribute("insumos", List.of());
        request.setAttribute("productos", List.of());
        request.setAttribute("movimientos", List.of());
        try {
            request.setAttribute("insumos", facade.listarInsumos());
            request.setAttribute("productos", facade.listarProductos());
            request.setAttribute("movimientos", facade.consultarKardex(
                    texto(request, "tipo"), fecha(request, "desde"), fecha(request, "hasta")));
        } catch (AplicacionException exception) {
            falloEnVista(request, exception.getMessage());
        }
        mostrar(request, response, "kardex");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            // El selector envía "CLASE:id", por ejemplo "PRODUCTO:3".
            String[] item = texto(request, "item").split(":", 2);
            if (item.length != 2) {
                throw new NegocioException("Seleccione un ítem válido.");
            }
            SolicitudMovimientoDTO solicitud = new SolicitudMovimientoDTO(
                    item[0],
                    parsearId(item[1]),
                    texto(request, "tipo"),
                    decimal(request, "cantidad", "La cantidad no es válida."),
                    texto(request, "motivo"));
            Optional<MovimientoDTO> movimiento = facade.registrarMovimiento(solicitud);
            exito(request, movimiento
                    .map(m -> "Movimiento registrado: " + m.getNombreItem() + " (" + m.getCodigoItem() + "), stock "
                            + m.getStockAnterior().toPlainString() + " → " + m.getStockPosterior().toPlainString() + ".")
                    .orElse("El stock contado coincide con el actual; no se registró ningún movimiento."));
        } catch (AplicacionException exception) {
            fallo(request, exception.getMessage());
        }
        redirigir(request, response);
    }

    private int parsearId(String valor) {
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException exception) {
            throw new NegocioException("Seleccione un ítem válido.");
        }
    }
}
