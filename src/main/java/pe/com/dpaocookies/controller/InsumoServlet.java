package pe.com.dpaocookies.controller;

import pe.com.dpaocookies.dao.InsumoDAO;
import pe.com.dpaocookies.model.Insumo;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@WebServlet(name = "InsumoServlet", urlPatterns = {"/insumos"})
public class InsumoServlet extends HttpServlet {

    private static final String VISTA_INVENTARIO = "/inventario.jsp";
    private static final String MENSAJE = "mensaje";
    private static final String ERROR = "error";

    private final InsumoDAO insumoDAO = new InsumoDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            List<Insumo> lista = insumoDAO.listar();
            request.setAttribute("listaInsumos", lista);
        } catch (IllegalStateException exception) {
            request.setAttribute(ERROR, exception.getMessage());
        }

        copiarMensajeDeSesion(request);
        request.getRequestDispatcher(VISTA_INVENTARIO).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        String accion = valor(request.getParameter("accion"));

        try {
            if ("registrar".equals(accion)) {
                registrar(request);
            } else if ("actualizarStock".equals(accion)) {
                actualizarStock(request);
            } else {
                throw new IllegalArgumentException("La operación solicitada no es válida.");
            }
        } catch (IllegalArgumentException exception) {
            request.getSession().setAttribute(ERROR, exception.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/insumos");
    }

    private void registrar(HttpServletRequest request) {
        Insumo insumo = new Insumo(
                requerido(request, "codigo", "El código es obligatorio."),
                requerido(request, "nombre", "El nombre es obligatorio."),
                requerido(request, "unidadMedida", "La unidad de medida es obligatoria."),
                decimalNoNegativo(request, "stockActual", "El stock actual no es válido."),
                decimalNoNegativo(request, "stockMinimo", "El stock mínimo no es válido."),
                decimalNoNegativo(request, "costoUnitario", "El costo unitario no es válido."),
                enteroPositivo(request, "idCategoria", "La categoría no es válida."));

        if (!insumoDAO.registrar(insumo)) {
            throw new IllegalArgumentException("No se pudo registrar el insumo. Verifique que el código sea único.");
        }
        request.getSession().setAttribute(MENSAJE, "Insumo registrado correctamente.");
    }

    private void actualizarStock(HttpServletRequest request) {
        int idInsumo = enteroPositivo(request, "idInsumo", "El insumo no es válido.");
        BigDecimal stock = decimalNoNegativo(request, "nuevoStock", "El nuevo stock no es válido.");

        if (!insumoDAO.actualizarStock(idInsumo, stock.doubleValue())) {
            throw new IllegalArgumentException("No se pudo actualizar el stock del insumo.");
        }
        request.getSession().setAttribute(MENSAJE, "Stock actualizado correctamente.");
    }

    private String requerido(HttpServletRequest request, String parametro, String mensaje) {
        String valor = valor(request.getParameter(parametro));
        if (valor.isBlank()) {
            throw new IllegalArgumentException(mensaje);
        }
        return valor;
    }

    private BigDecimal decimalNoNegativo(HttpServletRequest request, String parametro, String mensaje) {
        try {
            BigDecimal valor = new BigDecimal(requerido(request, parametro, mensaje));
            if (valor.signum() < 0) {
                throw new NumberFormatException();
            }
            return valor;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(mensaje);
        }
    }

    private int enteroPositivo(HttpServletRequest request, String parametro, String mensaje) {
        try {
            int valor = Integer.parseInt(requerido(request, parametro, mensaje));
            if (valor <= 0) {
                throw new NumberFormatException();
            }
            return valor;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(mensaje);
        }
    }

    private void copiarMensajeDeSesion(HttpServletRequest request) {
        var session = request.getSession(false);
        if (session == null) {
            return;
        }
        for (String atributo : new String[]{MENSAJE, ERROR}) {
            Object valor = session.getAttribute(atributo);
            if (valor != null) {
                request.setAttribute(atributo, valor);
                session.removeAttribute(atributo);
            }
        }
    }

    private String valor(String valor) {
        return valor == null ? "" : valor.trim();
    }
}
