package co.edu.sena.infomanager.app;

import co.edu.sena.infomanager.conexion.ConexionBD;
import co.edu.sena.infomanager.excepcion.AccesoDatosException;
import co.edu.sena.infomanager.excepcion.ValidacionException;
import co.edu.sena.infomanager.modelo.EstadoUsuario;
import co.edu.sena.infomanager.modelo.Rol;
import co.edu.sena.infomanager.modelo.Usuario;
import co.edu.sena.infomanager.servicio.UsuarioServicio;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Programa de consola para probar el módulo de usuarios de InfoManager System.
 * Permite insertar, consultar, actualizar y eliminar usuarios, además de probar el inicio de sesión.
 */
public class Main {

    private static final int OPCION_REGISTRAR = 1;
    private static final int OPCION_LISTAR = 2;
    private static final int OPCION_BUSCAR_POR_ID = 3;
    private static final int OPCION_BUSCAR_POR_NOMBRE = 4;
    private static final int OPCION_ACTUALIZAR = 5;
    private static final int OPCION_CAMBIAR_CONTRASENA = 6;
    private static final int OPCION_ELIMINAR = 7;
    private static final int OPCION_INICIAR_SESION = 8;
    private static final int OPCION_SALIR = 0;
    private static final int SEGUNDOS_ESPERA_CONEXION = 3;

    private static final Scanner TECLADO = new Scanner(System.in);
    private static final UsuarioServicio SERVICIO = new UsuarioServicio();

    public static void main(String[] args) {
        System.out.println("=== InfoManager System · Módulo de Usuarios (JDBC) ===");
        if (!probarConexion()) {
            return;
        }

        int opcion;
        do {
            mostrarMenu();
            opcion = leerEntero("Seleccione una opción: ");
            ejecutarOpcion(opcion);
        } while (opcion != OPCION_SALIR);

        System.out.println("Hasta pronto.");
    }

    // ------------------------------------------------------------------ MENÚ

    private static boolean probarConexion() {
        try (Connection conexion = ConexionBD.obtenerConexion()) {
            if (conexion.isValid(SEGUNDOS_ESPERA_CONEXION)) {
                System.out.println("Conexión exitosa con la base de datos.");
                return true;
            }
            System.out.println("ERROR: la conexión con la base de datos no es válida.");
            return false;
        } catch (AccesoDatosException | SQLException e) {
            System.out.println("ERROR: " + e.getMessage());
            return false;
        }
    }

    private static void mostrarMenu() {
        System.out.println();
        System.out.println("--------- MENÚ ---------");
        System.out.println("1. Registrar usuario");
        System.out.println("2. Listar usuarios");
        System.out.println("3. Buscar usuario por id");
        System.out.println("4. Buscar usuarios por nombre");
        System.out.println("5. Actualizar usuario");
        System.out.println("6. Cambiar contraseña");
        System.out.println("7. Eliminar usuario");
        System.out.println("8. Probar inicio de sesión");
        System.out.println("0. Salir");
    }

    private static void ejecutarOpcion(int opcion) {
        try {
            switch (opcion) {
                case OPCION_REGISTRAR:
                    registrarUsuario();
                    break;
                case OPCION_LISTAR:
                    mostrarTabla(SERVICIO.listarUsuarios());
                    break;
                case OPCION_BUSCAR_POR_ID:
                    buscarPorId();
                    break;
                case OPCION_BUSCAR_POR_NOMBRE:
                    buscarPorNombre();
                    break;
                case OPCION_ACTUALIZAR:
                    actualizarUsuario();
                    break;
                case OPCION_CAMBIAR_CONTRASENA:
                    cambiarContrasena();
                    break;
                case OPCION_ELIMINAR:
                    eliminarUsuario();
                    break;
                case OPCION_INICIAR_SESION:
                    iniciarSesion();
                    break;
                case OPCION_SALIR:
                    break;
                default:
                    System.out.println("Opción no válida.");
            }
        } catch (ValidacionException | AccesoDatosException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------ OPERACIONES

    private static void registrarUsuario() throws ValidacionException, AccesoDatosException {
        System.out.println("--- Registrar usuario ---");
        String nombre = leerTexto("Nombre: ");
        String apellido = leerTexto("Apellido: ");
        String correo = leerTexto("Correo electrónico: ");
        String contrasena = leerTexto("Contraseña (mínimo 8 caracteres): ");
        mostrarRoles();
        int idRol = leerEntero("Id del rol: ");

        Usuario creado = SERVICIO.registrarUsuario(nombre, apellido, correo, contrasena, idRol);
        System.out.println("Usuario registrado con id " + creado.getIdUsuario() + ".");
    }

    private static void buscarPorId() throws AccesoDatosException {
        int id = leerEntero("Id del usuario: ");
        Optional<Usuario> usuario = SERVICIO.buscarPorId(id);
        if (usuario.isPresent()) {
            mostrarDetalle(usuario.get());
        } else {
            System.out.println("No existe un usuario con ese id.");
        }
    }

    private static void buscarPorNombre() throws ValidacionException, AccesoDatosException {
        String texto = leerTexto("Nombre o apellido a buscar: ");
        mostrarTabla(SERVICIO.buscarPorNombre(texto));
    }

    private static void actualizarUsuario() throws ValidacionException, AccesoDatosException {
        int id = leerEntero("Id del usuario a actualizar: ");
        Optional<Usuario> existente = SERVICIO.buscarPorId(id);
        if (!existente.isPresent()) {
            System.out.println("No existe un usuario con ese id.");
            return;
        }
        Usuario actual = existente.get();
        System.out.println("Presione Enter para conservar el valor actual.");

        String nombre = leerTextoOpcional("Nombre [" + actual.getNombre() + "]: ", actual.getNombre());
        String apellido = leerTextoOpcional("Apellido [" + actual.getApellido() + "]: ", actual.getApellido());
        String correo = leerTextoOpcional("Correo [" + actual.getCorreo() + "]: ", actual.getCorreo());
        mostrarRoles();
        int idRol = leerEnteroOpcional("Id del rol [" + actual.getRol().getIdRol() + "]: ",
                actual.getRol().getIdRol());
        String textoEstado = leerTextoOpcional("Estado (activo/inactivo) [" + actual.getEstado().getValorBD() + "]: ",
                actual.getEstado().getValorBD());

        EstadoUsuario estado;
        try {
            estado = EstadoUsuario.desdeValorBD(textoEstado);
        } catch (IllegalArgumentException e) {
            throw new ValidacionException("El estado debe ser 'activo' o 'inactivo'.");
        }

        boolean actualizado = SERVICIO.actualizarUsuario(id, nombre, apellido, correo, idRol, estado);
        System.out.println(actualizado ? "Usuario actualizado correctamente." : "No se pudo actualizar el usuario.");
    }

    private static void cambiarContrasena() throws ValidacionException, AccesoDatosException {
        int id = leerEntero("Id del usuario: ");
        String nueva = leerTexto("Nueva contraseña (mínimo 8 caracteres): ");
        boolean cambiada = SERVICIO.cambiarContrasena(id, nueva);
        System.out.println(cambiada ? "Contraseña actualizada." : "No existe un usuario con ese id.");
    }

    private static void eliminarUsuario() throws AccesoDatosException {
        int id = leerEntero("Id del usuario a eliminar: ");
        String confirmacion = leerTexto("¿Está seguro? Esta acción no se puede deshacer (s/n): ");
        if (!confirmacion.equalsIgnoreCase("s")) {
            System.out.println("Operación cancelada.");
            return;
        }
        boolean eliminado = SERVICIO.eliminarUsuario(id);
        System.out.println(eliminado ? "Usuario eliminado." : "No existe un usuario con ese id.");
    }

    private static void iniciarSesion() throws AccesoDatosException {
        String correo = leerTexto("Correo electrónico: ");
        String contrasena = leerTexto("Contraseña: ");
        Optional<Usuario> autenticado = SERVICIO.autenticar(correo, contrasena);
        if (autenticado.isPresent()) {
            Usuario usuario = autenticado.get();
            System.out.println("Bienvenido, " + usuario.getNombreCompleto() + " (" + usuario.getRol() + ").");
        } else {
            System.out.println("Credenciales incorrectas o usuario inactivo.");
        }
    }

    // ------------------------------------------------------------------ PRESENTACIÓN

    private static void mostrarRoles() throws AccesoDatosException {
        List<Rol> roles = SERVICIO.listarRoles();
        System.out.println("Roles disponibles:");
        for (Rol rol : roles) {
            System.out.println("  " + rol.getIdRol() + " - " + rol.getNombreRol());
        }
    }

    private static void mostrarTabla(List<Usuario> usuarios) {
        if (usuarios.isEmpty()) {
            System.out.println("No hay usuarios para mostrar.");
            return;
        }
        String formato = "%-5s %-26s %-30s %-14s %-9s%n";
        System.out.printf(formato, "ID", "Nombre", "Correo", "Rol", "Estado");
        System.out.println("-".repeat(88));
        for (Usuario usuario : usuarios) {
            System.out.printf(formato, usuario.getIdUsuario(), usuario.getNombreCompleto(),
                    usuario.getCorreo(), usuario.getRol().getNombreRol(), usuario.getEstado().getValorBD());
        }
    }

    private static void mostrarDetalle(Usuario usuario) {
        System.out.println("Id:              " + usuario.getIdUsuario());
        System.out.println("Nombre:          " + usuario.getNombreCompleto());
        System.out.println("Correo:          " + usuario.getCorreo());
        System.out.println("Rol:             " + usuario.getRol().getNombreRol());
        System.out.println("Estado:          " + usuario.getEstado().getValorBD());
        System.out.println("Fecha registro:  " + usuario.getFechaRegistro());
    }

    // ------------------------------------------------------------------ LECTURA DE DATOS

    private static String leerTexto(String mensaje) {
        System.out.print(mensaje);
        return TECLADO.nextLine().trim();
    }

    private static String leerTextoOpcional(String mensaje, String valorActual) {
        String texto = leerTexto(mensaje);
        return texto.isEmpty() ? valorActual : texto;
    }

    private static int leerEntero(String mensaje) {
        while (true) {
            String texto = leerTexto(mensaje);
            try {
                return Integer.parseInt(texto);
            } catch (NumberFormatException e) {
                System.out.println("Ingrese un número válido.");
            }
        }
    }

    private static int leerEnteroOpcional(String mensaje, int valorActual) {
        while (true) {
            String texto = leerTexto(mensaje);
            if (texto.isEmpty()) {
                return valorActual;
            }
            try {
                return Integer.parseInt(texto);
            } catch (NumberFormatException e) {
                System.out.println("Ingrese un número válido.");
            }
        }
    }
}
