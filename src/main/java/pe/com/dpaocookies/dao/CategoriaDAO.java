package pe.com.dpaocookies.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import pe.com.dpaocookies.model.Categoria;
import pe.com.dpaocookies.util.ConexionBD;
import pe.com.dpaocookies.util.ProveedorConexion;

public class CategoriaDAO {

    private static final String SQL_LISTAR = "SELECT id_categoria, nombre FROM categoria ORDER BY nombre ASC";

    private final ProveedorConexion conexiones;

    public CategoriaDAO() {
        this(ConexionBD::getConnection);
    }

    public CategoriaDAO(ProveedorConexion conexiones) {
        this.conexiones = conexiones;
    }

    public List<Categoria> listar() throws SQLException {
        List<Categoria> categorias = new ArrayList<>();
        try (Connection connection = conexiones.obtener();
                PreparedStatement statement = connection.prepareStatement(SQL_LISTAR);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                categorias.add(new Categoria(resultSet.getInt("id_categoria"), resultSet.getString("nombre")));
            }
        }
        return categorias;
    }
}
