package pe.com.dpaocookies.controller;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.UUID;

@WebFilter("/*")
public class SeguridadFilter implements Filter {
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest r=(HttpServletRequest)req; HttpServletResponse p=(HttpServletResponse)res;
        r.setCharacterEncoding("UTF-8"); p.setCharacterEncoding("UTF-8");
        String db=System.getProperty("DPAO_DB_URL",System.getenv("DPAO_DB_URL"));
        if(db!=null && db.startsWith("jdbc:mysql://127.0.0.1:3307/dpao_test?"))
            p.setHeader("X-DPAO-Test-Database","isolated");
        p.setHeader("X-Content-Type-Options","nosniff"); p.setHeader("X-Frame-Options","DENY");
        String path=r.getServletPath();
        if(path.startsWith("/assets/")) { chain.doFilter(req,res); return; }
        p.setHeader("Cache-Control","no-store");
        HttpSession s=r.getSession();
        if(s.getAttribute("csrf")==null) s.setAttribute("csrf",UUID.randomUUID().toString());
        if("POST".equals(r.getMethod()) && !s.getAttribute("csrf").equals(r.getParameter("csrf"))) { p.sendError(403); return; }
        if(!path.equals("/acceso") && !path.equals("/index.jsp") && !path.equals("/") && s.getAttribute("usuario")==null) {
            p.sendRedirect(r.getContextPath()+"/acceso"); return;
        }
        chain.doFilter(req,res);
    }
}
