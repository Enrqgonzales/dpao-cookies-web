package pe.com.dpaocookies.controller;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.UUID;

/**
 * Seguridad común a las vistas JSP y JSF: UTF-8, cabeceras, token CSRF en todo POST
 * (los formularios JSF lo envían junto a su ViewState) y sesión obligatoria.
 */
@WebFilter("/*")
public class SeguridadFilter implements Filter {
    static final String ACCESO_JSF = "/app/acceso.xhtml";

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest r=(HttpServletRequest)req; HttpServletResponse p=(HttpServletResponse)res;
        r.setCharacterEncoding("UTF-8");
        String db=System.getProperty("DPAO_DB_URL",System.getenv("DPAO_DB_URL"));
        if(db!=null && db.startsWith("jdbc:mysql://127.0.0.1:3307/dpao_test?"))
            p.setHeader("X-DPAO-Test-Database","isolated");
        p.setHeader("X-Content-Type-Options","nosniff"); p.setHeader("X-Frame-Options","DENY");
        p.setHeader("Referrer-Policy","same-origin");
        String path=r.getServletPath();
        // Archivos estáticos: sin fijar el charset de la respuesta; si se fija, Tomcat los lee con la
        // codificación del sistema y los vuelve a codificar (las tildes de un .js salían dañadas).
        if(path.startsWith("/assets/") || path.startsWith("/javax.faces.resource/")) { chain.doFilter(req,res); return; }
        p.setCharacterEncoding("UTF-8");
        p.setHeader("Cache-Control","no-store");
        HttpSession s=r.getSession();
        if(s.getAttribute("csrf")==null) s.setAttribute("csrf",UUID.randomUUID().toString());
        String acceso=r.getContextPath()+(path.startsWith("/app/") ? ACCESO_JSF : "/acceso");
        if("POST".equals(r.getMethod()) && !s.getAttribute("csrf").equals(r.getParameter("csrf"))) {
            // Sin sesión previa (venció) no hay formulario válido que procesar: se vuelve a ingresar.
            if(s.isNew() && path.startsWith("/app/")) p.sendRedirect(acceso); else p.sendError(403);
            return;
        }
        if(!esPublica(path) && s.getAttribute("usuario")==null) { p.sendRedirect(acceso); return; }
        chain.doFilter(req,res);
    }

    static boolean esPublica(String path) {
        return path.equals("/acceso") || path.equals(ACCESO_JSF) || path.equals("/index.jsp") || path.equals("/");
    }
}
