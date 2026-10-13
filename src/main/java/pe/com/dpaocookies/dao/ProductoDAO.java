package pe.com.dpaocookies.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import pe.com.dpaocookies.model.Producto;
import pe.com.dpaocookies.util.ConexionBD;
import pe.com.dpaocookies.util.ProveedorConexion;

/**
 * Acceso a la tabla {@code producto}. El stock inicial es cero y solo cambia
 * mediante {@link KardexDAO}.
 */
public class ProductoDAO {

    private static final String SQL_LISTAR = "SELECT id_producto, codigo, nombre, precio_venta, "
            + "stock_actual, id_categoria FROM producto ORDER BY nombre ASC";
    private static final String SQL_REGISTRAR =
            "INSERT INTO producto (codigo, nombre, precio_venta, id_categoria) VALUES (?, ?, ?, ?)";
    private static final String SQL_ACTUALIZAR =
            "UPDATE producto SET codigo = ?, nombre = ?, precio_venta = ?, id_categoria = ? WHERE id_producto = ?";

    private final ProveedorConexion conexiones;

    public ProductoDAO() {
        this(ConexionBD::getConnection);
    }

    public ProductoDAO(ProveedorConexion conexiones) {
        this.conexiones = conexiones;
    }

    public List<Producto> listar() throws SQLException {
        List<Producto> productos = new ArrayList<>();
        try (Connection connection = conexiones.obtener();
                PreparedStatement statement = connection.prepareStatement(SQL_LISTAR);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                productos.add(new Producto(
                        resultSet.getInt("id_producto"),
                        resultSet.getString("codigo"),
                        resultSet.getString("nombre"),
                        resultSet.getBigDecimal("precio_venta"),
                        resultSet.getBigDecimal("stock_actual"),
                        resultSet.getInt("id_categoria")));
            }
        }
        return productos;
    }

    /** Inserta un producto con stock cero y devuelve el identificador generado. */
    public int registrar(Producto producto) throws SQLException {
        try (Connection connection = conexiones.obtener();
                PreparedStatement statement = connection.prepareStatement(SQL_REGISTRAR,
                        Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, producto.getCodigo());
            statement.setString(2, producto.getNombre());
            statement.setBigDecimal(3, producto.getPrecioVenta());
            statement.setInt(4, producto.getIdCategoria());
            statement.executeUpdate();
            try (ResultSet claves = statement.getGeneratedKeys()) {
                return claves.next() ? claves.getInt(1) : 0;
            }
        }
    }

    /** Actualiza los datos comerciales; devuelve {@code false} si el producto no existe. */
    public boolean actualizar(Producto producto) throws SQLException {
        try (Connection connection = conexiones.obtener();
                PreparedStatement statement = connection.prepareStatement(SQL_ACTUALIZAR)) {
            statement.setString(1, producto.getCodigo());
            statement.setString(2, producto.getNombre());
            statement.setBigDecimal(3, producto.getPrecioVenta());
            statement.setInt(4, producto.getIdCategoria());
            statement.setInt(5, producto.getIdProducto());
            return statement.executeUpdate() == 1;
        }
    }
}
