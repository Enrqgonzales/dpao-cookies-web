package pe.com.dpaocookies.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class ConexionBD {

    private static final String URL = "jdbc:mysql://localhost:3306/db_dpao_cookies?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private ConexionBD() {
        // Clase utilitaria.
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException exception) {
            throw new SQLException("No se encontró el driver JDBC de MySQL.", exception);
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Alias de compatibilidad para código existente que use el nombre en español.
     */
    @Deprecated
    public static Connection getConexion() throws SQLException {
        return getConnection();
    }
}
