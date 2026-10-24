package co.edu.sena.infomanager.dao;

import co.edu.sena.infomanager.conexion.ConexionBD;
import co.edu.sena.infomanager.excepcion.AccesoDatosException;
import co.edu.sena.infomanager.modelo.EstadoUsuario;
import co.edu.sena.infomanager.modelo.Rol;
import co.edu.sena.infomanager.modelo.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos de la tabla {@code usuarios}: inserción, consulta, actualización y eliminación.
 *
 * Todas las sentencias usan {@link PreparedStatement} con parámetros (?) para evitar inyección SQL,
 * y todos los recursos JDBC se cierran con try-with-resources.
 */
public class UsuarioDAO {

    // Códigos de error de MySQL que se traducen a mensajes claros para el usuario.
    private static final int ERROR_VALOR_DUPLICADO = 1062;
    private static final int ERROR_LLAVE_FORANEA_INEXISTENTE = 1452;
    private static final int ERROR_REGISTRO_REFERENCIADO = 1451;

    private static final String SQL_SELECCION_BASE =
            "SELECT u.id_usuario, u.nombre, u.apellido, u.correo, u.contrasena, u.estado, u.fecha_registro, "
            + "r.id_rol, r.nombre_rol, r.descripcion "
            + "FROM usuarios u INNER JOIN roles r ON u.id_rol = r.id_rol ";

    private static final String SQL_INSERTAR =
            "INSERT INTO usuarios (nombre, apellido, correo, contrasena, id_rol, estado) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String SQL_LISTAR = SQL_SELECCION_BASE + "ORDER BY u.id_usuario";
    private static final String SQL_BUSCAR_POR_ID = SQL_SELECCION_BASE + "WHERE u.id_usuario = ?";
    private static final String SQL_BUSCAR_POR_CORREO = SQL_SELECCION_BASE + "WHERE u.correo = ?";
    private static final String SQL_BUSCAR_POR_NOMBRE =
            SQL_SELECCION_BASE + "WHERE u.nombre LIKE ? OR u.apellido LIKE ? ORDER BY u.nombre, u.apellido";
    private static final String SQL_ACTUALIZAR =
            "UPDATE usuarios SET nombre = ?, apellido = ?, correo = ?, id_rol = ?, estado = ? WHERE id_usuario = ?";
    private static final String SQL_ACTUALIZAR_CONTRASENA =
            "UPDATE usuarios SET contrasena = ? WHERE id_usuario = ?";
    private static final String SQL_ELIMINAR = "DELETE FROM usuarios WHERE id_usuario = ?";

    // ------------------------------------------------------------------ INSERCIÓN

    /**
     * Inserta un nuevo usuario.
     *
     * @param usuario usuario a guardar (con la contraseña ya convertida en hash)
     * @return identificador generado por la base de datos
     * @throws AccesoDatosException si el correo ya existe, el rol no existe o falla la base de datos
     */
    public int insertar(Usuario usuario) throws AccesoDatosException {
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {
            sentencia.setString(1, usuario.getNombre());
            sentencia.setString(2, usuario.getApellido());
            sentencia.setString(3, usuario.getCorreo());
            sentencia.setString(4, usuario.getContrasena());
            sentencia.setInt(5, usuario.getRol().getIdRol());
            sentencia.setString(6, usuario.getEstado().getValorBD());
            sentencia.executeUpdate();

            try (ResultSet clavesGeneradas = sentencia.getGeneratedKeys()) {
                if (clavesGeneradas.next()) {
                    return clavesGeneradas.getInt(1);
                }
            }
            throw new AccesoDatosException("No se obtuvo el identificador del usuario creado.");
        } catch (SQLException e) {
            throw traducirError(e);
        }
    }

    // ------------------------------------------------------------------ CONSULTA

    /**
     * Lista todos los usuarios con su rol.
     *
     * @return lista de usuarios (vacía si no hay ninguno)
     * @throws AccesoDatosException si falla la base de datos
     */
    public List<Usuario> listarTodos() throws AccesoDatosException {
        List<Usuario> usuarios = new ArrayList<>();
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_LISTAR);
             ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                usuarios.add(mapearUsuario(resultado));
            }
        } catch (SQLException e) {
            throw traducirError(e);
        }
        return usuarios;
    }

    /**
     * Busca un usuario por su identificador.
     *
     * @param idUsuario identificador del usuario
     * @return el usuario, o vacío si no existe
     * @throws AccesoDatosException si falla la base de datos
     */
    public Optional<Usuario> buscarPorId(int idUsuario) throws AccesoDatosException {
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_BUSCAR_POR_ID)) {
            sentencia.setInt(1, idUsuario);
            try (ResultSet resultado = sentencia.executeQuery()) {
                if (resultado.next()) {
                    return Optional.of(mapearUsuario(resultado));
                }
            }
        } catch (SQLException e) {
            throw traducirError(e);
        }
        return Optional.empty();
    }

    /**
     * Busca un usuario por su correo electrónico (único en la tabla).
     *
     * @param correo correo del usuario
     * @return el usuario, o vacío si no existe
     * @throws AccesoDatosException si falla la base de datos
     */
    public Optional<Usuario> buscarPorCorreo(String correo) throws AccesoDatosException {
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_BUSCAR_POR_CORREO)) {
            sentencia.setString(1, correo);
            try (ResultSet resultado = sentencia.executeQuery()) {
                if (resultado.next()) {
                    return Optional.of(mapearUsuario(resultado));
                }
            }
        } catch (SQLException e) {
            throw traducirError(e);
        }
        return Optional.empty();
    }

    /**
     * Busca usuarios cuyo nombre o apellido contenga el texto indicado.
     *
     * @param texto texto a buscar
     * @return lista de coincidencias (vacía si no hay)
     * @throws AccesoDatosException si falla la base de datos
     */
    public List<Usuario> buscarPorNombre(String texto) throws AccesoDatosException {
        List<Usuario> usuarios = new ArrayList<>();
        String patron = "%" + texto + "%";
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_BUSCAR_POR_NOMBRE)) {
            sentencia.setString(1, patron);
            sentencia.setString(2, patron);
            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    usuarios.add(mapearUsuario(resultado));
                }
            }
        } catch (SQLException e) {
            throw traducirError(e);
        }
        return usuarios;
    }

    // ------------------------------------------------------------------ ACTUALIZACIÓN

    /**
     * Actualiza los datos de un usuario (no modifica la contraseña).
     *
     * @param usuario usuario con los nuevos datos; se usa su idUsuario para ubicarlo
     * @return {@code true} si se actualizó un registro, {@code false} si el usuario no existe
     * @throws AccesoDatosException si el correo ya existe, el rol no existe o falla la base de datos
     */
    public boolean actualizar(Usuario usuario) throws AccesoDatosException {
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_ACTUALIZAR)) {
            sentencia.setString(1, usuario.getNombre());
            sentencia.setString(2, usuario.getApellido());
            sentencia.setString(3, usuario.getCorreo());
            sentencia.setInt(4, usuario.getRol().getIdRol());
            sentencia.setString(5, usuario.getEstado().getValorBD());
            sentencia.setInt(6, usuario.getIdUsuario());
            return sentencia.executeUpdate() > 0;
        } catch (SQLException e) {
            throw traducirError(e);
        }
    }

    /**
     * Cambia la contraseña de un usuario.
     *
     * @param idUsuario      identificador del usuario
     * @param nuevoHash      nuevo hash de la contraseña
     * @return {@code true} si se actualizó un registro, {@code false} si el usuario no existe
     * @throws AccesoDatosException si falla la base de datos
     */
    public boolean actualizarContrasena(int idUsuario, String nuevoHash) throws AccesoDatosException {
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_ACTUALIZAR_CONTRASENA)) {
            sentencia.setString(1, nuevoHash);
            sentencia.setInt(2, idUsuario);
            return sentencia.executeUpdate() > 0;
        } catch (SQLException e) {
            throw traducirError(e);
        }
    }

    // ------------------------------------------------------------------ ELIMINACIÓN

    /**
     * Elimina un usuario.
     *
     * @param idUsuario identificador del usuario
     * @return {@code true} si se eliminó, {@code false} si el usuario no existe
     * @throws AccesoDatosException si el usuario tiene documentos o reportes asociados o falla la base de datos
     */
    public boolean eliminar(int idUsuario) throws AccesoDatosException {
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_ELIMINAR)) {
            sentencia.setInt(1, idUsuario);
            return sentencia.executeUpdate() > 0;
        } catch (SQLException e) {
            throw traducirError(e);
        }
    }

    // ------------------------------------------------------------------ APOYO

    private Usuario mapearUsuario(ResultSet resultado) throws SQLException {
        Rol rol = new Rol(
                resultado.getInt("id_rol"),
                resultado.getString("nombre_rol"),
                resultado.getString("descripcion"));

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(resultado.getInt("id_usuario"));
        usuario.setNombre(resultado.getString("nombre"));
        usuario.setApellido(resultado.getString("apellido"));
        usuario.setCorreo(resultado.getString("correo"));
        usuario.setContrasena(resultado.getString("contrasena"));
        usuario.setRol(rol);
        usuario.setEstado(EstadoUsuario.desdeValorBD(resultado.getString("estado")));

        Timestamp fechaRegistro = resultado.getTimestamp("fecha_registro");
        if (fechaRegistro != null) {
            usuario.setFechaRegistro(fechaRegistro.toLocalDateTime());
        }
        return usuario;
    }

    /** Convierte los errores de integridad de MySQL en mensajes comprensibles. */
    private AccesoDatosException traducirError(SQLException error) {
        switch (error.getErrorCode()) {
            case ERROR_VALOR_DUPLICADO:
                return new AccesoDatosException("Ya existe un usuario registrado con ese correo electrónico.", error);
            case ERROR_LLAVE_FORANEA_INEXISTENTE:
                return new AccesoDatosException("El rol indicado no existe.", error);
            case ERROR_REGISTRO_REFERENCIADO:
                return new AccesoDatosException(
                        "No se puede eliminar el usuario porque tiene documentos o reportes asociados.", error);
            default:
                return new AccesoDatosException("Error al acceder a la base de datos: " + error.getMessage(), error);
        }
    }
}
