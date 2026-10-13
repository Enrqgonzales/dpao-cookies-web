package pe.com.dpaocookies.support;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import pe.com.dpaocookies.util.ProveedorConexion;

/**
 * JDBC simulado con {@link Proxy} para probar el control transaccional del
 * Kardex sin MySQL. Registra los eventos de la conexión (autoCommit, commit,
 * rollback, close) y las sentencias ejecutadas, y puede fallar a propósito.
 */
public final class JdbcFalso {

    public final List<String> eventos = new ArrayList<>();
    public final List<String> sentencias = new ArrayList<>();

    private BigDecimal stock;
    private String fallarEn;
    private boolean autoCommit = true;

    /** Stock que devuelve el SELECT ... FOR UPDATE; {@code null} simula un ítem inexistente. */
    public JdbcFalso conStock(String valor) {
        stock = valor == null ? null : new BigDecimal(valor);
        return this;
    }

    /** Hace fallar el executeUpdate cuyo SQL empiece con el prefijo indicado. */
    public JdbcFalso fallandoEn(String prefijoSql) {
        fallarEn = prefijoSql;
        return this;
    }

    public ProveedorConexion proveedor() {
        return this::conexion;
    }

    public boolean ejecuto(String prefijoSql) {
        return sentencias.stream().anyMatch(s -> s.startsWith(prefijoSql));
    }

    public Connection conexion() {
        return proxy(Connection.class, (metodo, args) -> {
            switch (metodo) {
                case "setAutoCommit":
                    autoCommit = (Boolean) args[0];
                    eventos.add("autoCommit=" + autoCommit);
                    return null;
                case "getAutoCommit":
                    return autoCommit;
                case "commit":
                case "rollback":
                case "close":
                    eventos.add(metodo);
                    return null;
                case "prepareStatement":
                    return sentencia((String) args[0]);
                default:
                    throw new UnsupportedOperationException(metodo);
            }
        });
    }

    private PreparedStatement sentencia(String sql) {
        return proxy(PreparedStatement.class, (metodo, args) -> {
            if (metodo.startsWith("set") || metodo.equals("close")) {
                return null;
            }
            switch (metodo) {
                case "executeQuery":
                    sentencias.add(sql);
                    return filaBloqueada();
                case "executeUpdate":
                    sentencias.add(sql);
                    if (fallarEn != null && sql.startsWith(fallarEn)) {
                        throw new SQLException("Fallo simulado de " + fallarEn, "45000", 1644);
                    }
                    return 1;
                case "getGeneratedKeys":
                    return claveGenerada();
                default:
                    throw new UnsupportedOperationException(metodo);
            }
        });
    }

    private ResultSet filaBloqueada() {
        boolean[] leida = {false};
        return proxy(ResultSet.class, (metodo, args) -> {
            switch (metodo) {
                case "next":
                    boolean hay = stock != null && !leida[0];
                    leida[0] = true;
                    return hay;
                case "getBigDecimal":
                    return stock;
                case "getString":
                    return "codigo".equals(args[0]) ? "HAR-001" : "Harina de trigo";
                case "close":
                    return null;
                default:
                    throw new UnsupportedOperationException(metodo);
            }
        });
    }

    private ResultSet claveGenerada() {
        boolean[] leida = {false};
        return proxy(ResultSet.class, (metodo, args) -> {
            switch (metodo) {
                case "next":
                    boolean hay = !leida[0];
                    leida[0] = true;
                    return hay;
                case "getLong":
                    return 99L;
                case "getInt":
                    return 99;
                case "close":
                    return null;
                default:
                    throw new UnsupportedOperationException(metodo);
            }
        });
    }

    @FunctionalInterface
    private interface Manejador {
        Object manejar(String metodo, Object[] args) throws Throwable;
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> tipo, Manejador manejador) {
        return (T) Proxy.newProxyInstance(JdbcFalso.class.getClassLoader(), new Class<?>[]{tipo},
                (instancia, metodo, args) -> {
                    switch (metodo.getName()) {
                        case "toString":
                            return tipo.getSimpleName() + " falso";
                        case "hashCode":
                            return System.identityHashCode(instancia);
                        case "equals":
                            return instancia == args[0];
                        default:
                            return manejador.manejar(metodo.getName(), args == null ? new Object[0] : args);
                    }
                });
    }
}
