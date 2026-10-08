package pe.com.dpaocookies.controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import pe.com.dpaocookies.exception.AplicacionException;

@WebServlet(name = "TableroServlet", urlPatterns = {"/tablero"})
public class TableroServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("resumen", facade.obtenerResumen());
        } catch (AplicacionException exception) {
            falloEnVista(request, exception.getMessage());
        }
        mostrar(request, response, "tablero");
    }
}
