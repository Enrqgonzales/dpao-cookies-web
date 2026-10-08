package pe.com.dpaocookies.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import pe.com.dpaocookies.model.Insumo;
import pe.com.dpaocookies.util.ConexionBD;
import pe.com.dpaocookies.util.ProveedorConexion;

/**
 * Acceso a la tabla {@code insumo}. El stock solo cambia mediante
 * {@link KardexDAO}, para que cada variación quede registrada.
 */
public class InsumoDAO {

    private static final String SQL_LISTAR = "SELECT id_insumo, codigo, nombre, unidad_medida, "
            + "stock_actual, stock_minimo, costo_unitario, id_categoria "
            + "FROM insumo ORDER BY nombre ASC";
    private static final String SQL_REGISTRAR = """
            INSERT INTO insumo (codigo, nombre, unidad_medida, stock_actual,
                                stock_minimo, costo_unitario, id_categoria)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

    private final ProveedorConexion conexiones;

    public InsumoDAO() {
        this(ConexionBD::getConnection);
    }

    public InsumoDAO(ProveedorConexion conexiones) {
        this.conexiones = conexiones;
    }

    public List<Insumo> listar() throws SQLException {
        List<Insumo> insumos = new ArrayList<>();
        try (Connection connection = conexiones.obtener();
                PreparedStatement statement = connection.prepareStatement(SQL_LISTAR);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                insumos.add(mapear(resultSet));
            }
        }
        return insumos;
    }

    /** Inserta el insumo con su saldo de apertura y devuelve el identificador generado. */
    public int registrar(Insumo insumo) throws SQLException {
        try (Connection connection = conexiones.obtener();
                PreparedStatement statement = connection.prepareStatement(SQL_REGISTRAR,
                        Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, insumo.getCodigo());
            statement.setString(2, insumo.getNombre());
            statement.setString(3, insumo.getUnidadMedida());
            statement.setBigDecimal(4, insumo.getStockActual());
            statement.setBigDecimal(5, insumo.getStockMinimo());
            statement.setBigDecimal(6, insumo.getCostoUnitario());
            statement.setInt(7, insumo.getIdCategoria());
            statement.executeUpdate();
            try (ResultSet claves = statement.getGeneratedKeys()) {
                return claves.next() ? claves.getInt(1) : 0;
            }
        }
    }

    private Insumo mapear(ResultSet resultSet) throws SQLException {
        return new Insumo(
                resultSet.getInt("id_insumo"),
                resultSet.getString("codigo"),
                resultSet.getString("nombre"),
                resultSet.getString("unidad_medida"),
                resultSet.getBigDecimal("stock_actual"),
                resultSet.getBigDecimal("stock_minimo"),
                resultSet.getBigDecimal("costo_unitario"),
                resultSet.getInt("id_categoria"));
    }
}
