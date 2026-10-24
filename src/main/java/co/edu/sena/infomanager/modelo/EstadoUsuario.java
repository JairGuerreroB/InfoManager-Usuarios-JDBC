package co.edu.sena.infomanager.modelo;

/**
 * Estados posibles de un usuario. Cada valor se corresponde con el ENUM('activo','inactivo')
 * de la columna {@code usuarios.estado} en MySQL.
 */
public enum EstadoUsuario {

    ACTIVO("activo"),
    INACTIVO("inactivo");

    private final String valorBD;

    EstadoUsuario(String valorBD) {
        this.valorBD = valorBD;
    }

    /** Texto tal como se almacena en la base de datos. */
    public String getValorBD() {
        return valorBD;
    }

    /**
     * Convierte el texto almacenado en la base de datos al valor del enum.
     *
     * @param valor texto leído de la columna estado
     * @return el estado correspondiente
     * @throws IllegalArgumentException si el texto no corresponde a ningún estado
     */
    public static EstadoUsuario desdeValorBD(String valor) {
        for (EstadoUsuario estado : values()) {
            if (estado.valorBD.equalsIgnoreCase(valor)) {
                return estado;
            }
        }
        throw new IllegalArgumentException("Estado de usuario desconocido: " + valor);
    }
}
