package co.edu.sena.infomanager.excepcion;

/**
 * Error de validación de los datos ingresados (campos vacíos, formato de correo inválido, etc.).
 */
public class ValidacionException extends Exception {

    private static final long serialVersionUID = 1L;

    public ValidacionException(String mensaje) {
        super(mensaje);
    }
}
