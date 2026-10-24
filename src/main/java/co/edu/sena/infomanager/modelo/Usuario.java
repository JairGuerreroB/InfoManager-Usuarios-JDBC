package co.edu.sena.infomanager.modelo;

import java.time.LocalDateTime;

/**
 * Usuario registrado en el sistema. Representa una fila de la tabla {@code usuarios}
 * junto con el {@link Rol} que tiene asignado.
 */
public class Usuario {

    private int idUsuario;
    private String nombre;
    private String apellido;
    private String correo;
    private String contrasena;
    private Rol rol;
    private EstadoUsuario estado;
    private LocalDateTime fechaRegistro;

    public Usuario() {
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    /** Hash de la contraseña (nunca el texto plano). */
    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public EstadoUsuario getEstado() {
        return estado;
    }

    public void setEstado(EstadoUsuario estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    @Override
    public String toString() {
        return "Usuario{id=" + idUsuario + ", nombre=" + getNombreCompleto()
                + ", correo=" + correo + ", rol=" + rol + ", estado=" + estado + "}";
    }
}
