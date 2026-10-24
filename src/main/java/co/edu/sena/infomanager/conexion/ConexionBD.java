package co.edu.sena.infomanager.conexion;

import co.edu.sena.infomanager.excepcion.AccesoDatosException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Fábrica de conexiones JDBC hacia la base de datos MySQL {@code infomanager_db}.
 *
 * Los datos de conexión (URL, usuario y clave) se leen del archivo {@code db.properties},
 * ubicado en {@code src/main/resources}, para no dejar credenciales escritas en el código.
 */
public final class ConexionBD {

    private static final String ARCHIVO_CONFIGURACION = "/db.properties";
    private static final String PROPIEDAD_URL = "db.url";
    private static final String PROPIEDAD_USUARIO = "db.usuario";
    private static final String PROPIEDAD_CLAVE = "db.clave";

    private static Properties configuracion;

    private ConexionBD() {
        // Clase utilitaria: no se instancia.
    }

    /**
     * Abre una nueva conexión con la base de datos. Quien la reciba debe cerrarla
     * (se recomienda usar try-with-resources).
     *
     * @return conexión abierta
     * @throws AccesoDatosException si falta la configuración o no se logra conectar
     */
    public static Connection obtenerConexion() throws AccesoDatosException {
        Properties datos = cargarConfiguracion();
        try {
            return DriverManager.getConnection(
                    datos.getProperty(PROPIEDAD_URL),
                    datos.getProperty(PROPIEDAD_USUARIO),
                    datos.getProperty(PROPIEDAD_CLAVE));
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo conectar con la base de datos. "
                    + "Verifique que MySQL esté activo y que los datos de db.properties sean correctos.", e);
        }
    }

    private static synchronized Properties cargarConfiguracion() throws AccesoDatosException {
        if (configuracion == null) {
            try (InputStream entrada = ConexionBD.class.getResourceAsStream(ARCHIVO_CONFIGURACION)) {
                if (entrada == null) {
                    throw new AccesoDatosException("No se encontró el archivo db.properties. "
                            + "Copie db.properties.example como db.properties en src/main/resources "
                            + "y complete sus datos de conexión.");
                }
                Properties datos = new Properties();
                datos.load(entrada);
                configuracion = datos;
            } catch (IOException e) {
                throw new AccesoDatosException("No se pudo leer el archivo db.properties.", e);
            }
        }
        return configuracion;
    }
}
