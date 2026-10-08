package pe.com.dpaocookies.controller;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import pe.com.dpaocookies.dto.ProductoDTO;
import pe.com.dpaocookies.exception.AplicacionException;

@WebServlet(name = "ProductoServlet", urlPatterns = {"/productos"})
public class ProductoServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("productos", List.of());
        request.setAttribute("categorias", List.of());
        try {
            request.setAttribute("productos", facade.listarProductos());
            request.setAttribute("categorias", facade.listarCategorias());
        } catch (AplicacionException exception) {
            falloEnVista(request, exception.getMessage());
        }
        mostrar(request, response, "productos");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            ProductoDTO producto = new ProductoDTO();
            producto.setIdProducto(entero(request, "id", "El producto no es válido."));
            producto.setCodigo(texto(request, "codigo"));
            producto.setNombre(texto(request, "nombre"));
            producto.setPrecioVenta(decimal(request, "precio", "El precio no es válido."));
            producto.setIdCategoria(entero(request, "categoria", "La categoría no es válida."));
            boolean nuevo = producto.getIdProducto() == 0;
            facade.guardarProducto(producto);
            exito(request, nuevo ? "Producto registrado correctamente." : "Producto actualizado correctamente.");
        } catch (AplicacionException exception) {
            fallo(request, exception.getMessage());
        }
        redirigir(request, response);
    }
}
