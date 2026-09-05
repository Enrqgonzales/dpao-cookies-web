package pe.com.dpaocookies.controller;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@WebServlet({"/acceso","/salir"})
public class AccesoServlet extends HttpServlet {
    protected void doGet(HttpServletRequest r,HttpServletResponse p) throws ServletException,IOException {
        r.getRequestDispatcher("/WEB-INF/views/acceso.jsp").forward(r,p);
    }
    protected void doPost(HttpServletRequest r,HttpServletResponse p) throws IOException {
        if(r.getServletPath().equals("/salir")) { r.getSession().invalidate(); p.sendRedirect(r.getContextPath()+"/acceso"); return; }
        String usuario=System.getenv("DPAO_APP_USER"), clave=System.getenv("DPAO_APP_PASSWORD");
        String entrada=r.getParameter("clave");
        if(usuario!=null && clave!=null && !clave.isBlank() && usuario.equals(r.getParameter("usuario")) && entrada!=null
                && MessageDigest.isEqual(clave.getBytes(StandardCharsets.UTF_8),entrada.getBytes(StandardCharsets.UTF_8))) {
            r.changeSessionId(); r.getSession().setAttribute("usuario",usuario); r.getSession().setMaxInactiveInterval(1800);
            p.sendRedirect(r.getContextPath()+"/tablero");
        } else { r.getSession().setAttribute("error","Credenciales inválidas o acceso sin configurar."); p.sendRedirect(r.getContextPath()+"/acceso"); }
    }
}
