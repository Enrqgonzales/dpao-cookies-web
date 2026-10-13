package pe.com.dpaocookies.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import pe.com.dpaocookies.model.PackPrecio;
import pe.com.dpaocookies.util.ConexionBD;
import pe.com.dpaocookies.util.ProveedorConexion;

/** Precios de los packs de delivery ({@code sql/catalogo-dpao.sql}). */
public class PackDAO {

    private static final String SQL_LISTAR = "SELECT p.id_categoria, c.nombre, p.unidades, p.precio "
            + "FROM pack_precio p JOIN categoria c ON c.id_categoria = p.id_categoria "
            + "ORDER BY c.id_categoria ASC, p.unidades ASC";

    private final ProveedorConexion conexiones;

    public PackDAO() {
        this(ConexionBD::getConnection);
    }

    public PackDAO(ProveedorConexion conexiones) {
        this.conexiones = conexiones;
    }

    public List<PackPrecio> listar() throws SQLException {
        List<PackPrecio> precios = new ArrayList<>();
        try (Connection connection = conexiones.obtener();
                PreparedStatement statement = connection.prepareStatement(SQL_LISTAR);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                precios.add(new PackPrecio(resultSet.getInt(1), resultSet.getString(2),
                        resultSet.getInt(3), resultSet.getBigDecimal(4)));
            }
        }
        return precios;
    }
}
