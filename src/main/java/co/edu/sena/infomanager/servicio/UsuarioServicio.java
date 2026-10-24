package co.edu.sena.infomanager.servicio;

import co.edu.sena.infomanager.dao.RolDAO;
import co.edu.sena.infomanager.dao.UsuarioDAO;
import co.edu.sena.infomanager.excepcion.AccesoDatosException;
import co.edu.sena.infomanager.excepcion.ValidacionException;
import co.edu.sena.infomanager.modelo.EstadoUsuario;
import co.edu.sena.infomanager.modelo.Rol;
import co.edu.sena.infomanager.modelo.Usuario;
import co.edu.sena.infomanager.util.ContrasenaUtil;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Lógica del módulo de usuarios: valida los datos, aplica las reglas del negocio
 * (contraseñas cifradas, solo usuarios activos pueden iniciar sesión) y delega el acceso
 * a la base de datos en {@link UsuarioDAO} y {@link RolDAO}.
 */
public class UsuarioServicio {

    private static final int LONGITUD_MAXIMA_NOMBRE = 50;
    private static final int LONGITUD_MAXIMA_APELLIDO = 50;
    private static final int LONGITUD_MAXIMA_CORREO = 100;
    private static final int LONGITUD_MINIMA_CONTRASENA = 8;
    private static final Pattern PATRON_CORREO = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final RolDAO rolDAO = new RolDAO();

    // ------------------------------------------------------------------ INSERCIÓN

    /**
     * Registra un nuevo usuario con estado activo y la contraseña cifrada.
     *
     * @return el usuario creado, con su identificador asignado
     */
    public Usuario registrarUsuario(String nombre, String apellido, String correo,
                                    String contrasena, int idRol)
            throws ValidacionException, AccesoDatosException {
        validarDatosBasicos(nombre, apellido, correo);
        validarContrasena(contrasena);
        Rol rol = obtenerRolExistente(idRol);

        Usuario usuario = new Usuario();
        usuario.setNombre(nombre.trim());
        usuario.setApellido(apellido.trim());
        usuario.setCorreo(correo.trim().toLowerCase());
        usuario.setContrasena(ContrasenaUtil.generarHash(contrasena));
        usuario.setRol(rol);
        usuario.setEstado(EstadoUsuario.ACTIVO);

        usuario.setIdUsuario(usuarioDAO.insertar(usuario));
        return usuario;
    }

    // ------------------------------------------------------------------ CONSULTA

    public List<Usuario> listarUsuarios() throws AccesoDatosException {
        return usuarioDAO.listarTodos();
    }

    public Optional<Usuario> buscarPorId(int idUsuario) throws AccesoDatosException {
        return usuarioDAO.buscarPorId(idUsuario);
    }

    public List<Usuario> buscarPorNombre(String texto) throws ValidacionException, AccesoDatosException {
        if (texto == null || texto.trim().isEmpty()) {
            throw new ValidacionException("Escriba el texto que desea buscar.");
        }
        return usuarioDAO.buscarPorNombre(texto.trim());
    }

    public List<Rol> listarRoles() throws AccesoDatosException {
        return rolDAO.listarTodos();
    }

    // ------------------------------------------------------------------ ACTUALIZACIÓN

    /**
     * Actualiza los datos de un usuario existente.
     *
     * @return {@code true} si se actualizó, {@code false} si el usuario no existe
     */
    public boolean actualizarUsuario(int idUsuario, String nombre, String apellido, String correo,
                                     int idRol, EstadoUsuario estado)
            throws ValidacionException, AccesoDatosException {
        validarDatosBasicos(nombre, apellido, correo);
        if (estado == null) {
            throw new ValidacionException("Debe indicar el estado del usuario.");
        }
        Rol rol = obtenerRolExistente(idRol);

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(idUsuario);
        usuario.setNombre(nombre.trim());
        usuario.setApellido(apellido.trim());
        usuario.setCorreo(correo.trim().toLowerCase());
        usuario.setRol(rol);
        usuario.setEstado(estado);

        return usuarioDAO.actualizar(usuario);
    }

    /**
     * Cambia la contraseña de un usuario (se guarda cifrada).
     *
     * @return {@code true} si se actualizó, {@code false} si el usuario no existe
     */
    public boolean cambiarContrasena(int idUsuario, String nuevaContrasena)
            throws ValidacionException, AccesoDatosException {
        validarContrasena(nuevaContrasena);
        return usuarioDAO.actualizarContrasena(idUsuario, ContrasenaUtil.generarHash(nuevaContrasena));
    }

    // ------------------------------------------------------------------ ELIMINACIÓN

    /**
     * Elimina un usuario.
     *
     * @return {@code true} si se eliminó, {@code false} si el usuario no existe
     */
    public boolean eliminarUsuario(int idUsuario) throws AccesoDatosException {
        return usuarioDAO.eliminar(idUsuario);
    }

    // ------------------------------------------------------------------ INICIO DE SESIÓN

    /**
     * Verifica las credenciales de un usuario. Solo los usuarios con estado activo pueden ingresar.
     *
     * @return el usuario autenticado, o vacío si las credenciales son incorrectas o está inactivo
     */
    public Optional<Usuario> autenticar(String correo, String contrasena) throws AccesoDatosException {
        if (correo == null || contrasena == null) {
            return Optional.empty();
        }
        Optional<Usuario> encontrado = usuarioDAO.buscarPorCorreo(correo.trim().toLowerCase());
        if (encontrado.isPresent()) {
            Usuario usuario = encontrado.get();
            boolean credencialesValidas = ContrasenaUtil.verificar(contrasena, usuario.getContrasena());
            if (credencialesValidas && usuario.getEstado() == EstadoUsuario.ACTIVO) {
                return encontrado;
            }
        }
        return Optional.empty();
    }

    // ------------------------------------------------------------------ VALIDACIONES

    private void validarDatosBasicos(String nombre, String apellido, String correo) throws ValidacionException {
        validarTextoObligatorio(nombre, "nombre", LONGITUD_MAXIMA_NOMBRE);
        validarTextoObligatorio(apellido, "apellido", LONGITUD_MAXIMA_APELLIDO);
        validarTextoObligatorio(correo, "correo electrónico", LONGITUD_MAXIMA_CORREO);
        if (!PATRON_CORREO.matcher(correo.trim()).matches()) {
            throw new ValidacionException("Ingrese un correo electrónico válido.");
        }
    }

    private void validarContrasena(String contrasena) throws ValidacionException {
        if (contrasena == null || contrasena.length() < LONGITUD_MINIMA_CONTRASENA) {
            throw new ValidacionException(
                    "La contraseña debe tener al menos " + LONGITUD_MINIMA_CONTRASENA + " caracteres.");
        }
    }

    private void validarTextoObligatorio(String valor, String campo, int longitudMaxima)
            throws ValidacionException {
        if (valor == null || valor.trim().isEmpty()) {
            throw new ValidacionException("El campo " + campo + " es obligatorio.");
        }
        if (valor.trim().length() > longitudMaxima) {
            throw new ValidacionException(
                    "El campo " + campo + " no puede superar los " + longitudMaxima + " caracteres.");
        }
    }

    private Rol obtenerRolExistente(int idRol) throws ValidacionException, AccesoDatosException {
        Optional<Rol> rol = rolDAO.buscarPorId(idRol);
        if (!rol.isPresent()) {
            throw new ValidacionException("El rol seleccionado no existe.");
        }
        return rol.get();
    }
}
