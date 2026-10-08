package pe.com.dpaocookies.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import pe.com.dpaocookies.exception.NegocioException;
import pe.com.dpaocookies.facade.InventarioFacade;

/**
 * Base de los controladores: convierte parámetros HTTP en tipos Java, delega
 * en {@link InventarioFacade} y aplica Post/Redirect/Get con mensajes de un solo uso.
 */
public abstract class BaseServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final String VISTAS = "/WEB-INF/views/";
    private static final String MENSAJE = "mensaje";
    private static final String ERROR = "error";

    protected InventarioFacade facade;

    @Override
    public void init() throws ServletException {
        facade = new InventarioFacade();
    }

    protected void mostrar(HttpServletRequest request, HttpServletResponse response, String vista)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            for (String atributo : new String[]{MENSAJE, ERROR}) {
                Object valor = session.getAttribute(atributo);
                if (valor != null && request.getAttribute(atributo) == null) {
                    request.setAttribute(atributo, valor);
                }
                session.removeAttribute(atributo);
            }
        }
        request.setAttribute("seccion", vista);
        request.getRequestDispatcher(VISTAS + vista + ".jsp").forward(request, response);
    }

    protected void redirigir(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + request.getServletPath());
    }

    protected void exito(HttpServletRequest request, String mensaje) {
        request.getSession().setAttribute(MENSAJE, mensaje);
    }

    protected void fallo(HttpServletRequest request, String mensaje) {
        request.getSession().setAttribute(ERROR, mensaje);
    }

    protected void falloEnVista(HttpServletRequest request, String mensaje) {
        request.setAttribute(ERROR, mensaje);
    }

    protected String texto(HttpServletRequest request, String parametro) {
        String valor = request.getParameter(parametro);
        return valor == null ? "" : valor.trim();
    }

    protected BigDecimal decimal(HttpServletRequest request, String parametro, String mensaje) {
        try {
            return new BigDecimal(texto(request, parametro));
        } catch (NumberFormatException exception) {
            throw new NegocioException(mensaje);
        }
    }

    protected int entero(HttpServletRequest request, String parametro, String mensaje) {
        try {
            return Integer.parseInt(texto(request, parametro));
        } catch (NumberFormatException exception) {
            throw new NegocioException(mensaje);
        }
    }

    protected LocalDate fecha(HttpServletRequest request, String parametro) {
        String valor = texto(request, parametro);
        try {
            return valor.isEmpty() ? null : LocalDate.parse(valor);
        } catch (DateTimeParseException exception) {
            throw new NegocioException("La fecha no tiene un formato válido.");
        }
    }
}
