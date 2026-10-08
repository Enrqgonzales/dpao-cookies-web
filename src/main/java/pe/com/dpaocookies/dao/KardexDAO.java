package pe.com.dpaocookies.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import pe.com.dpaocookies.exception.NegocioException;
import pe.com.dpaocookies.exception.StockInsuficienteException;
import pe.com.dpaocookies.model.ClaseItem;
import pe.com.dpaocookies.model.MovimientoKardex;
import pe.com.dpaocookies.model.TipoMovimiento;
import pe.com.dpaocookies.util.ConexionBD;
import pe.com.dpaocookies.util.ProveedorConexion;
import pe.com.dpaocookies.util.Validacion;

/**
 * Movimientos de Kardex. Cada registro bloquea la fila del ítem, actualiza su
 * stock e inserta el movimiento con los saldos anterior y posterior dentro de
 * una única transacción JDBC: o se guardan ambos cambios o ninguno.
 */
public class KardexDAO {

    private static final String SQL_INSERTAR = "INSERT INTO movimiento_kardex (%s, tipo_movimiento, cantidad, "
            + "stock_anterior, stock_posterior, observacion) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String SQL_CONSULTAR = """
            SELECT m.id_movimiento, m.id_insumo, m.id_producto, m.tipo_movimiento, m.cantidad,
                   m.stock_anterior, m.stock_posterior, m.observacion, m.fecha_registro,
                   COALESCE(i.codigo, p.codigo) AS codigo_item,
                   COALESCE(i.nombre, p.nombre) AS nombre_item
            FROM movimiento_kardex m
            LEFT JOIN insumo i ON m.id_insumo = i.id_insumo
            LEFT JOIN producto p ON m.id_producto = p.id_producto
            WHERE (? IS NULL OR m.tipo_movimiento = ?)
              AND (? IS NULL OR m.fecha_registro >= ?)
              AND (? IS NULL OR m.fecha_registro < DATE_ADD(?, INTERVAL 1 DAY))
            ORDER BY m.id_movimiento DESC
            """;

    private final ProveedorConexion conexiones;

    public KardexDAO() {
        this(ConexionBD::getConnection);
    }

    public KardexDAO(ProveedorConexion conexiones) {
        this.conexiones = conexiones;
    }

    /**
     * Registra un movimiento de forma atómica.
     *
     * @param valor cantidad positiva movida o, en un AJUSTE, stock físico contado
     * @return el movimiento guardado, o {@code null} si un AJUSTE no cambia el stock
     * @throws StockInsuficienteException si el saldo resultante sería negativo
     */
    public MovimientoKardex registrar(ClaseItem clase, int idItem, TipoMovimiento tipo,
            BigDecimal valor, String observacion) throws SQLException {
        try (Connection connection = conexiones.obtener()) {
            connection.setAutoCommit(false);
            try {
                MovimientoKardex movimiento = aplicar(connection, clase, idItem, tipo, valor, observacion);
                connection.commit();
                return movimiento;
            } catch (SQLException | RuntimeException exception) {
                deshacer(connection, exception);
                throw exception;
            }
        }
    }

    private MovimientoKardex aplicar(Connection connection, ClaseItem clase, int idItem,
            TipoMovimiento tipo, BigDecimal valor, String observacion) throws SQLException {
        MovimientoKardex movimiento = bloquearItem(connection, clase, idItem);
        BigDecimal anterior = movimiento.getStockAnterior();
        BigDecimal variacion = tipo.variacion(valor, anterior);
        BigDecimal posterior = anterior.add(variacion);

        if (posterior.signum() < 0) {
            throw new StockInsuficienteException(anterior, valor);
        }
        if (posterior.compareTo(Validacion.MAXIMO) > 0) {
            throw new NegocioException("El stock resultante supera el máximo permitido (99999999.99).");
        }
        if (variacion.signum() == 0) {
            return null;
        }

        String sqlActualizar = "UPDATE " + clase.getTabla() + " SET stock_actual = ? WHERE "
                + clase.getColumnaId() + " = ?";
        try (PreparedStatement statement = connection.prepareStatement(sqlActualizar)) {
            statement.setBigDecimal(1, posterior);
            statement.setInt(2, idItem);
            statement.executeUpdate();
        }

        String sqlInsertar = String.format(SQL_INSERTAR, clase.getColumnaId());
        try (PreparedStatement statement = connection.prepareStatement(sqlInsertar,
                Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, idItem);
            statement.setString(2, tipo.name());
            statement.setBigDecimal(3, variacion);
            statement.setBigDecimal(4, anterior);
            statement.setBigDecimal(5, posterior);
            statement.setString(6, observacion);
            statement.executeUpdate();
            try (ResultSet claves = statement.getGeneratedKeys()) {
                if (claves.next()) {
                    movimiento.setIdMovimiento(claves.getLong(1));
                }
            }
        }

        movimiento.setTipoMovimiento(tipo);
        movimiento.setCantidad(variacion);
        movimiento.setStockPosterior(posterior);
        movimiento.setObservacion(observacion);
        return movimiento;
    }

    /** SELECT ... FOR UPDATE: otro movimiento del mismo ítem espera a que esta transacción termine. */
    private MovimientoKardex bloquearItem(Connection connection, ClaseItem clase, int idItem) throws SQLException {
        String sql = "SELECT stock_actual, codigo, nombre FROM " + clase.getTabla()
                + " WHERE " + clase.getColumnaId() + " = ? FOR UPDATE";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idItem);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new NegocioException("El ítem seleccionado no existe.");
                }
                MovimientoKardex movimiento = new MovimientoKardex();
                movimiento.setClaseItem(clase);
                movimiento.setIdItem(idItem);
                movimiento.setStockAnterior(resultSet.getBigDecimal("stock_actual"));
                movimiento.setCodigoItem(resultSet.getString("codigo"));
                movimiento.setNombreItem(resultSet.getString("nombre"));
                return movimiento;
            }
        }
    }

    private static void deshacer(Connection connection, Exception causa) {
        try {
            connection.rollback();
        } catch (SQLException errorRollback) {
            causa.addSuppressed(errorRollback);
        }
    }

    /**
     * Consulta el historial, del más reciente al más antiguo.
     *
     * @param tipo filtro opcional; {@code null} incluye todos los tipos
     * @param desde fecha inicial inclusiva, opcional
     * @param hasta fecha final inclusiva, opcional
     * @param limite máximo de filas; cero o negativo devuelve todas
     */
    public List<MovimientoKardex> listar(TipoMovimiento tipo, LocalDate desde, LocalDate hasta, int limite)
            throws SQLException {
        String sql = limite > 0 ? SQL_CONSULTAR + "LIMIT ?" : SQL_CONSULTAR;
        String nombreTipo = tipo == null ? null : tipo.name();
        List<MovimientoKardex> movimientos = new ArrayList<>();
        try (Connection connection = conexiones.obtener();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nombreTipo);
            statement.setString(2, nombreTipo);
            statement.setObject(3, desde);
            statement.setObject(4, desde);
            statement.setObject(5, hasta);
            statement.setObject(6, hasta);
            if (limite > 0) {
                statement.setInt(7, limite);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    movimientos.add(mapear(resultSet));
                }
            }
        }
        return movimientos;
    }

    private MovimientoKardex mapear(ResultSet resultSet) throws SQLException {
        MovimientoKardex movimiento = new MovimientoKardex();
        movimiento.setIdMovimiento(resultSet.getLong("id_movimiento"));
        int idInsumo = resultSet.getInt("id_insumo");
        if (resultSet.wasNull()) {
            movimiento.setClaseItem(ClaseItem.PRODUCTO);
            movimiento.setIdItem(resultSet.getInt("id_producto"));
        } else {
            movimiento.setClaseItem(ClaseItem.INSUMO);
            movimiento.setIdItem(idInsumo);
        }
        movimiento.setCodigoItem(resultSet.getString("codigo_item"));
        movimiento.setNombreItem(resultSet.getString("nombre_item"));
        movimiento.setTipoMovimiento(TipoMovimiento.valueOf(resultSet.getString("tipo_movimiento")));
        movimiento.setCantidad(resultSet.getBigDecimal("cantidad"));
        movimiento.setStockAnterior(resultSet.getBigDecimal("stock_anterior"));
        movimiento.setStockPosterior(resultSet.getBigDecimal("stock_posterior"));
        movimiento.setObservacion(resultSet.getString("observacion"));
        movimiento.setFechaRegistro(resultSet.getTimestamp("fecha_registro"));
        return movimiento;
    }
}
