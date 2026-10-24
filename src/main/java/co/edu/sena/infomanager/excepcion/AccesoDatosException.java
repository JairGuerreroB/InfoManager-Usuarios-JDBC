package co.edu.sena.infomanager.excepcion;

/**
 * Error ocurrido al acceder a la base de datos (conexión, consulta o restricción de integridad).
 * Envuelve la {@link java.sql.SQLException} original para no exponer detalles de JDBC a las capas superiores.
 */
public class AccesoDatosException extends Exception {

    private static final long serialVersionUID = 1L;

    public AccesoDatosException(String mensaje) {
        super(mensaje);
    }

    public AccesoDatosException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
