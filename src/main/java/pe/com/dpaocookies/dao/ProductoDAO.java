package pe.com.dpaocookies.dao;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import pe.com.dpaocookies.util.*;

public class ProductoDAO {
    public List<Map<String,Object>> listar() throws SQLException {
        try(Connection c=ConexionBD.getConnection(); PreparedStatement s=c.prepareStatement("SELECT * FROM producto ORDER BY nombre"); ResultSet r=s.executeQuery()) {
            return KardexDAO.filas(r);
        }
    }
    public List<Map<String,Object>> categorias() throws SQLException {
        try(Connection c=ConexionBD.getConnection(); PreparedStatement s=c.prepareStatement("SELECT * FROM categoria ORDER BY nombre"); ResultSet r=s.executeQuery()) {
            return KardexDAO.filas(r);
        }
    }
    public void guardar(int id, String codigo, String nombre, BigDecimal precio, int categoria) throws SQLException {
        codigo=Validacion.texto(codigo,20,"Código"); nombre=Validacion.texto(nombre,100,"Nombre"); Validacion.decimal(precio);
        if(categoria<=0 || id<0) throw new IllegalArgumentException("Identificador inválido.");
        String sql=id==0 ? "INSERT INTO producto(codigo,nombre,precio_venta,id_categoria) VALUES(?,?,?,?)" : "UPDATE producto SET codigo=?,nombre=?,precio_venta=?,id_categoria=? WHERE id_producto=?";
        try(Connection c=ConexionBD.getConnection(); PreparedStatement s=c.prepareStatement(sql)) {
            s.setString(1,codigo); s.setString(2,nombre); s.setBigDecimal(3,precio); s.setInt(4,categoria);
            if(id!=0) s.setInt(5,id);
            if(s.executeUpdate()!=1) throw new IllegalArgumentException("El producto no existe.");
        }
    }
}
