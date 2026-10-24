package co.edu.sena.infomanager.dao;

import co.edu.sena.infomanager.conexion.ConexionBD;
import co.edu.sena.infomanager.excepcion.AccesoDatosException;
import co.edu.sena.infomanager.modelo.Rol;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Acceso a datos de la tabla {@code roles} (consulta).
 */
public class RolDAO {

    private static final String SQL_LISTAR =
            "SELECT id_rol, nombre_rol, descripcion FROM roles ORDER BY id_rol";
    private static final String SQL_BUSCAR_POR_ID =
            "SELECT id_rol, nombre_rol, descripcion FROM roles WHERE id_rol = ?";

    /**
     * Lista todos los roles disponibles.
     *
     * @return lista de roles (vacía si no hay ninguno)
     * @throws AccesoDatosException si ocurre un error de base de datos
     */
    public List<Rol> listarTodos() throws AccesoDatosException {
        List<Rol> roles = new ArrayList<>();
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_LISTAR);
             ResultSet resultado = sentencia.executeQuery()) {
            while (resultado.next()) {
                roles.add(mapearRol(resultado));
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("Error al consultar los roles: " + e.getMessage(), e);
        }
        return roles;
    }

    /**
     * Busca un rol por su identificador.
     *
     * @param idRol identificador del rol
     * @return el rol, o vacío si no existe
     * @throws AccesoDatosException si ocurre un error de base de datos
     */
    public Optional<Rol> buscarPorId(int idRol) throws AccesoDatosException {
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement sentencia = conexion.prepareStatement(SQL_BUSCAR_POR_ID)) {
            sentencia.setInt(1, idRol);
            try (ResultSet resultado = sentencia.executeQuery()) {
                if (resultado.next()) {
                    return Optional.of(mapearRol(resultado));
                }
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("Error al consultar el rol: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    private Rol mapearRol(ResultSet resultado) throws SQLException {
        return new Rol(
                resultado.getInt("id_rol"),
                resultado.getString("nombre_rol"),
                resultado.getString("descripcion"));
    }
}
