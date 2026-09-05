package pe.com.dpaocookies.dao;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import pe.com.dpaocookies.util.ConexionBD;
import pe.com.dpaocookies.util.Validacion;

public class KardexDAO {
    // El bloqueo de fila serializa movimientos del mismo ítem y evita stock perdido.
    public void mover(String item, int id, String tipo, BigDecimal cantidad, String motivo) throws SQLException {
        if (!Set.of("ENTRADA", "SALIDA", "VENTA", "MERMA").contains(tipo))
            throw new IllegalArgumentException("Tipo de movimiento inválido.");
        Validacion.decimal(cantidad);
        if (cantidad.signum() == 0) throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
        cambiar(item, id, tipo, cantidad, motivo, false);
    }
    public void ajustarInsumo(int id, BigDecimal stock) throws SQLException {
        Validacion.decimal(stock);
        cambiar("insumo", id, "AJUSTE", stock, "Ajuste desde inventario", true);
    }
    private void cambiar(String item, int id, String tipo, BigDecimal valor, String motivo, boolean absoluto) throws SQLException {
        if (!Set.of("insumo", "producto").contains(item) || id <= 0)
            throw new IllegalArgumentException("Ítem inválido.");
        motivo = Validacion.texto(motivo, 255, "Motivo");
        try (Connection c = ConexionBD.getConnection()) {
            c.setAutoCommit(false);
            try {
                BigDecimal anterior;
                try (PreparedStatement s = c.prepareStatement("SELECT stock_actual FROM " + item + " WHERE id_" + item + "=? FOR UPDATE")) {
                    s.setInt(1, id);
                    try (ResultSet r = s.executeQuery()) {
                        if (!r.next()) throw new IllegalArgumentException("El ítem no existe.");
                        anterior = r.getBigDecimal(1);
                    }
                }
                BigDecimal delta = absoluto ? valor.subtract(anterior) : "ENTRADA".equals(tipo) ? valor : valor.negate();
                BigDecimal posterior = Validacion.decimal(anterior.add(delta));
                if (delta.signum() != 0) {
                    try (PreparedStatement s = c.prepareStatement("UPDATE " + item + " SET stock_actual=? WHERE id_" + item + "=?")) {
                        s.setBigDecimal(1, posterior); s.setInt(2, id); s.executeUpdate();
                    }
                    try (PreparedStatement s = c.prepareStatement("INSERT INTO movimiento_kardex(id_" + item + ",tipo_movimiento,cantidad,stock_anterior,stock_posterior,observacion) VALUES (?,?,?,?,?,?)")) {
                        s.setInt(1, id); s.setString(2, tipo); s.setBigDecimal(3, delta);
                        s.setBigDecimal(4, anterior); s.setBigDecimal(5, posterior); s.setString(6, motivo); s.executeUpdate();
                    }
                }
                c.commit();
            } catch (SQLException | RuntimeException e) {
                c.rollback(); throw e;
            }
        }
    }
    public List<Map<String,Object>> listar(String tipo, LocalDate desde, LocalDate hasta) throws SQLException {
        String sql = "SELECT m.*, COALESCE(i.nombre,p.nombre) nombre FROM movimiento_kardex m LEFT JOIN insumo i ON m.id_insumo=i.id_insumo LEFT JOIN producto p ON m.id_producto=p.id_producto WHERE (?='' OR tipo_movimiento=?) AND (? IS NULL OR fecha_registro>=?) AND (? IS NULL OR fecha_registro<DATE_ADD(?, INTERVAL 1 DAY)) ORDER BY id_movimiento DESC";
        try (Connection c = ConexionBD.getConnection(); PreparedStatement s = c.prepareStatement(sql)) {
            s.setString(1,tipo); s.setString(2,tipo);
            s.setObject(3,desde); s.setObject(4,desde); s.setObject(5,hasta); s.setObject(6,hasta);
            try (ResultSet r=s.executeQuery()) { return filas(r); }
        }
    }
    static List<Map<String,Object>> filas(ResultSet r) throws SQLException {
        List<Map<String,Object>> lista=new ArrayList<>();
        while(r.next()) {
            Map<String,Object> fila=new LinkedHashMap<>();
            for(int i=1;i<=r.getMetaData().getColumnCount();i++) fila.put(r.getMetaData().getColumnLabel(i),r.getObject(i));
            lista.add(fila);
        }
        return lista;
    }
}
