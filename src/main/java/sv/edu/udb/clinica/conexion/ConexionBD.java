package sv.edu.udb.clinica.conexion;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Establece la conexión de la aplicación con la base de datos MySQL mediante JDBC.
 * Los datos de conexión (url, usuario, contraseña) se leen de db.properties.
 */
public final class ConexionBD {

    private static final String ARCHIVO_CONFIG = "/db.properties";

    private static Properties config;

    private ConexionBD() {
    }

    //Abre una nueva conexión a la base de datos.
    public static Connection obtenerConexion() throws SQLException {
        Properties cfg = cargarConfiguracion();
        return DriverManager.getConnection(
                cfg.getProperty("db.url"),
                cfg.getProperty("db.usuario"),
                cfg.getProperty("db.password"));
    }

    private static synchronized Properties cargarConfiguracion() throws SQLException {
        if (config == null) {
            try (InputStream in = ConexionBD.class.getResourceAsStream(ARCHIVO_CONFIG)) {
                if (in == null) {
                    throw new SQLException("No se encontró el archivo " + ARCHIVO_CONFIG);
                }
                Properties props = new Properties();
                props.load(in);
                config = props;
            } catch (IOException e) {
                throw new SQLException("No se pudo leer el archivo " + ARCHIVO_CONFIG, e);
            }
        }
        return config;
    }
}
