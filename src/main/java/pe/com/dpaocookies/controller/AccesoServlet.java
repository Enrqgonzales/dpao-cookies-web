package pe.com.dpaocookies.controller;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import pe.com.dpaocookies.util.Autenticacion;

@WebServlet({"/acceso","/salir"})
public class AccesoServlet extends HttpServlet {
    private final Autenticacion autenticacion = new Autenticacion();
    protected void doGet(HttpServletRequest r,HttpServletResponse p) throws ServletException,IOException {
        r.getRequestDispatcher("/WEB-INF/views/acceso.jsp").forward(r,p);
    }
    protected void doPost(HttpServletRequest r,HttpServletResponse p) throws IOException {
        if(r.getServletPath().equals("/salir")) { r.getSession().invalidate(); p.sendRedirect(r.getContextPath()+"/acceso"); return; }
        String usuario=r.getParameter("usuario");
        if(autenticacion.valida(usuario,r.getParameter("clave"))) {
            r.changeSessionId(); r.getSession().setAttribute("usuario",usuario); r.getSession().setMaxInactiveInterval(1800);
            p.sendRedirect(r.getContextPath()+"/tablero");
        } else { r.getSession().setAttribute("error","Credenciales inválidas o acceso sin configurar."); p.sendRedirect(r.getContextPath()+"/acceso"); }
    }
}
