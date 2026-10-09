package pe.com.dpaocookies.support;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import pe.com.dpaocookies.util.ConexionBD;

/**
 * Prepara, una sola vez por ejecución, un esquema {@code dpao_test_<marca>} en
 * el MySQL aislado de pruebas y apunta {@code DPAO_DB_URL} hacia él. El esquema
 * se conserva como evidencia; nunca se toca una base real.
 */
public final class BaseDatosPrueba {

    private static final String URL_PERMITIDA = "jdbc:mysql://127.0.0.1:3307/dpao_test?";
    private static String esquema;

    private BaseDatosPrueba() {
    }

    public static synchronized String preparar() throws Exception {
        if (esquema != null) {
            return esquema;
        }
        String url = System.getenv("DPAO_DB_URL");
        if (url == null || !url.startsWith(URL_PERMITIDA)) {
            throw new IllegalStateException("Pruebas permitidas solamente en MySQL aislado 127.0.0.1:3307/dpao_test.");
        }
        String nuevo = "dpao_test_" + System.currentTimeMillis();
        try (Connection c = ConexionBD.getConnection(); Statement s = c.createStatement()) {
            s.execute("CREATE DATABASE " + nuevo + " CHARACTER SET utf8mb4");
        }
        System.setProperty("DPAO_DB_URL", url.replace("/dpao_test?", "/" + nuevo + "?"));
        ejecutarScript("sql/schema.sql");
        ejecutarScript("sql/seed.sql");
        try (Connection c = ConexionBD.getConnection()) {
            System.out.println("Motor real: " + c.getMetaData().getDatabaseProductName() + " "
                    + c.getMetaData().getDatabaseProductVersion() + "; esquema aislado: " + nuevo);
        }
        esquema = nuevo;
        return esquema;
    }

    /** Ejecuta un script del repositorio, sentencia por sentencia, sobre el esquema de pruebas. */
    public static void ejecutarScript(String archivo) throws Exception {
        String sql = Files.readString(Path.of(archivo)).replaceAll("(?m)^--.*$", "");
        try (Connection c = ConexionBD.getConnection(); Statement s = c.createStatement()) {
            for (String comando : sql.split(";")) {
                if (!comando.isBlank()) {
                    s.execute(comando);
                }
            }
        }
    }

    /** Ejecuta una consulta escalar sobre el esquema de pruebas. */
    public static java.math.BigDecimal decimal(String sql) throws SQLException {
        try (Connection c = ConexionBD.getConnection(); Statement s = c.createStatement();
                java.sql.ResultSet r = s.executeQuery(sql)) {
            if (!r.next()) {
                throw new SQLException("Sin resultado: " + sql);
            }
            return r.getBigDecimal(1);
        }
    }

    public static void ejecutar(String sql) throws SQLException {
        try (Connection c = ConexionBD.getConnection(); Statement s = c.createStatement()) {
            s.execute(sql);
        }
    }
}
