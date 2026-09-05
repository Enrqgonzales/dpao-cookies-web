package pe.com.dpaocookies.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class ConexionBD {

    private static String configuracion(String nombre) throws SQLException {
        String valor = System.getProperty(nombre, System.getenv(nombre));
        if (valor == null) throw new SQLException("Falta configuración de base de datos: " + nombre);
        return valor;
    }

    private ConexionBD() {
        // Clase utilitaria.
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException exception) {
            throw new SQLException("No se encontró el driver JDBC de MySQL.", exception);
        }
        return DriverManager.getConnection(configuracion("DPAO_DB_URL"),
                configuracion("DPAO_DB_USER"), configuracion("DPAO_DB_PASSWORD"));
    }

    /**
     * Alias de compatibilidad para código existente que use el nombre en español.
     */
    @Deprecated
    public static Connection getConexion() throws SQLException {
        return getConnection();
    }
}
