package pe.com.dpaocookies.dao;

import pe.com.dpaocookies.model.Insumo;
import pe.com.dpaocookies.util.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InsumoDAO {

    private static final Logger LOGGER = Logger.getLogger(InsumoDAO.class.getName());

    private static final String SQL_LISTAR = "SELECT id_insumo, codigo, nombre, unidad_medida, "
            + "stock_actual, stock_minimo, costo_unitario, id_categoria "
            + "FROM insumo ORDER BY nombre ASC";
    private static final String SQL_REGISTRAR = """
            INSERT INTO insumo (codigo, nombre, unidad_medida, stock_actual,
                                stock_minimo, costo_unitario, id_categoria)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
    private static final String SQL_ACTUALIZAR_STOCK =
            "UPDATE insumo SET stock_actual = ? WHERE id_insumo = ?";

    public List<Insumo> listar() {
        List<Insumo> insumos = new ArrayList<>();

        try (Connection connection = ConexionBD.getConnection();
                PreparedStatement statement = connection.prepareStatement(SQL_LISTAR);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                insumos.add(mapear(resultSet));
            }
            return insumos;
        } catch (SQLException exception) {
            exception.printStackTrace();
            LOGGER.log(Level.SEVERE, "No se pudo listar los insumos", exception);
            throw new IllegalStateException("No se pudo consultar el inventario.", exception);
        }
    }

    public boolean registrar(Insumo insumo) {
        if (insumo == null) {
            return false;
        }

        try (Connection connection = ConexionBD.getConnection();
                PreparedStatement statement = connection.prepareStatement(SQL_REGISTRAR)) {
            statement.setString(1, insumo.getCodigo());
            statement.setString(2, insumo.getNombre());
            statement.setString(3, insumo.getUnidadMedida());
            statement.setBigDecimal(4, insumo.getStockActual());
            statement.setBigDecimal(5, insumo.getStockMinimo());
            statement.setBigDecimal(6, insumo.getCostoUnitario());
            statement.setInt(7, insumo.getIdCategoria());
            return statement.executeUpdate() == 1;
        } catch (SQLException exception) {
            exception.printStackTrace();
            LOGGER.log(Level.SEVERE, "No se pudo registrar el insumo", exception);
            return false;
        }
    }

    public boolean actualizarStock(int idInsumo, double nuevoStock) {
        if (idInsumo <= 0 || Double.isNaN(nuevoStock) || Double.isInfinite(nuevoStock)
                || nuevoStock < 0) {
            return false;
        }

        try (Connection connection = ConexionBD.getConnection();
                PreparedStatement statement = connection.prepareStatement(SQL_ACTUALIZAR_STOCK)) {
            statement.setDouble(1, nuevoStock);
            statement.setInt(2, idInsumo);
            return statement.executeUpdate() == 1;
        } catch (SQLException exception) {
            exception.printStackTrace();
            LOGGER.log(Level.SEVERE, "No se pudo actualizar el stock del insumo " + idInsumo, exception);
            return false;
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
