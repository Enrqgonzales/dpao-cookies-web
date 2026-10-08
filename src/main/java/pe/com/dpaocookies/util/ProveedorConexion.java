package pe.com.dpaocookies.util;

import java.sql.Connection;
import java.sql.SQLException;

/** Origen de conexiones de los DAO; permite inyectar una conexión de prueba. */
@FunctionalInterface
public interface ProveedorConexion {

    Connection obtener() throws SQLException;
}
