package pe.com.dpaocookies.controller;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.logging.*;
import pe.com.dpaocookies.dao.*;

@WebServlet({"/tablero","/productos","/kardex"})
public class PanelServlet extends HttpServlet {
    private final ProductoDAO productos=new ProductoDAO();
    private final KardexDAO kardex=new KardexDAO();
    protected void doGet(HttpServletRequest r,HttpServletResponse p) throws ServletException,IOException {
        String vista=r.getServletPath().substring(1);
        try {
            var insumos=new InsumoDAO().listar();
            r.setAttribute("insumos",insumos); r.setAttribute("productos",productos.listar());
            r.setAttribute("categorias",productos.categorias());
            r.setAttribute("bajoStock",insumos.stream().filter(i->i.isBajoStock()).count());
            if(vista.equals("kardex")) {
                LocalDate desde=fecha(r.getParameter("desde")),hasta=fecha(r.getParameter("hasta"));
                if(desde!=null && hasta!=null && desde.isAfter(hasta)) throw new IllegalArgumentException();
                r.setAttribute("movimientos",kardex.listar(valor(r,"tipo"),desde,hasta));
            }
        } catch(IllegalArgumentException | java.time.format.DateTimeParseException e) {
            r.setAttribute("error","Revise los filtros y el rango de fechas.");
        } catch(SQLException | IllegalStateException e) { registrarError(r,e); }
        for(String key:new String[]{"mensaje","error"}) {
            Object mensaje=r.getSession().getAttribute(key);
            if(mensaje!=null) { r.setAttribute(key,mensaje); r.getSession().removeAttribute(key); }
        }
        r.getRequestDispatcher("/WEB-INF/views/"+vista+".jsp").forward(r,p);
    }
    protected void doPost(HttpServletRequest r,HttpServletResponse p) throws IOException {
        try {
            if(r.getServletPath().equals("/productos")) {
                productos.guardar(Integer.parseInt(valor(r,"id")),valor(r,"codigo"),valor(r,"nombre"),new BigDecimal(valor(r,"precio")),Integer.parseInt(valor(r,"categoria")));
            } else if(r.getServletPath().equals("/kardex")) {
                kardex.mover(valor(r,"item"),Integer.parseInt(valor(r,"id")),valor(r,"tipo"),new BigDecimal(valor(r,"cantidad")),valor(r,"motivo"));
            } else throw new IllegalArgumentException("Operación inválida.");
            r.getSession().setAttribute("mensaje","Operación registrada correctamente.");
        } catch(NumberFormatException e) { r.getSession().setAttribute("error","Revise los valores numéricos obligatorios.");
        } catch(IllegalArgumentException e) { r.getSession().setAttribute("error",e.getMessage());
        } catch(SQLException e) {
            String mensaje=e.getErrorCode()==1062 ? "El código ya existe." : e.getErrorCode()==1452 ? "La categoría o el ítem no existe." : "No se pudo guardar la operación. Intente nuevamente.";
            r.getSession().setAttribute("error",mensaje); Logger.getLogger(getClass().getName()).log(Level.SEVERE,"Error de persistencia",e);
        }
        p.sendRedirect(r.getContextPath()+r.getServletPath());
    }
    private void registrarError(HttpServletRequest r,Exception e) {
        Logger.getLogger(getClass().getName()).log(Level.SEVERE,"Error de consulta",e);
        r.setAttribute("error","No se pudo consultar la información. Intente nuevamente.");
    }
    private String valor(HttpServletRequest r,String key) { String s=r.getParameter(key); return s==null?"":s.trim(); }
    private LocalDate fecha(String s) { return s==null || s.isBlank()?null:LocalDate.parse(s); }
}
