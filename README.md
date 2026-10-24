# InfoManager System — Módulo de Usuarios (JDBC)

Módulo de gestión de usuarios de **InfoManager System**, desarrollado en Java con acceso a MySQL mediante **JDBC**.
Permite **insertar, consultar, actualizar y eliminar** usuarios (con su rol), cambiar contraseñas y probar el inicio de sesión.

Evidencia **GA7-220501096-AA2-EV01** — Codificación de módulos del software.
Aprendiz: Jair Guerrero · Ficha 3336142 · Instructor: Juan Hernández.

## Requisitos

- JDK 11 o superior
- MySQL Server 8.x en ejecución
- Un IDE con soporte para Maven (NetBeans, Eclipse o IntelliJ IDEA) o Maven instalado

## Puesta en marcha

1. **Crear la base de datos.** En MySQL Workbench ejecute, en orden:
   - `database/01_infomanager_system_script.sql` (crea `infomanager_db` y sus 6 tablas)
   - `database/02_datos_iniciales.sql` (inserta los roles *Administrador* y *Usuario*)
2. **Configurar la conexión.** En `src/main/resources/` copie `db.properties.example` como `db.properties` y escriba la contraseña de su usuario de MySQL en `db.clave`.
3. **Abrir el proyecto** como proyecto Maven en el IDE (el `pom.xml` descarga el controlador `mysql-connector-j` automáticamente).
4. **Ejecutar** la clase `co.edu.sena.infomanager.app.Main`.

También se puede ejecutar desde la terminal, dentro de la carpeta del proyecto:

```
mvn compile exec:java
```

Si no usa Maven, agregue manualmente el archivo `mysql-connector-j-8.4.0.jar` a las bibliotecas del proyecto.

## Funcionalidades (menú de consola)

| Opción | Operación | Tipo CRUD | Método del DAO |
|---|---|---|---|
| 1 | Registrar usuario | Inserción | `UsuarioDAO.insertar` |
| 2 | Listar usuarios | Consulta | `UsuarioDAO.listarTodos` |
| 3 | Buscar usuario por id | Consulta | `UsuarioDAO.buscarPorId` |
| 4 | Buscar usuarios por nombre | Consulta | `UsuarioDAO.buscarPorNombre` |
| 5 | Actualizar usuario | Actualización | `UsuarioDAO.actualizar` |
| 6 | Cambiar contraseña | Actualización | `UsuarioDAO.actualizarContrasena` |
| 7 | Eliminar usuario | Eliminación | `UsuarioDAO.eliminar` |
| 8 | Probar inicio de sesión | Consulta | `UsuarioDAO.buscarPorCorreo` |

Reglas del módulo: el correo es único; las contraseñas se guardan **cifradas** (PBKDF2 con sal) y nunca en texto plano; solo los usuarios *activos* pueden iniciar sesión; un usuario con documentos o reportes asociados no se puede eliminar (restricción `ON DELETE RESTRICT` de la base de datos).

## Estructura del proyecto

```
InfoManager-Usuarios-JDBC/
├── pom.xml
├── database/                          Scripts SQL (esquema y datos iniciales)
├── docs/                              Artefactos del módulo (historias de usuario, casos de uso, diagrama de clases)
└── src/main/
    ├── java/co/edu/sena/infomanager/
    │   ├── app/         Main                      Menú de consola
    │   ├── servicio/    UsuarioServicio           Validaciones y reglas del negocio
    │   ├── dao/         UsuarioDAO, RolDAO        Acceso a datos con JDBC (PreparedStatement)
    │   ├── conexion/    ConexionBD                Conexión JDBC (lee db.properties)
    │   ├── modelo/      Usuario, Rol, EstadoUsuario
    │   ├── util/        ContrasenaUtil            Cifrado de contraseñas
    │   └── excepcion/   AccesoDatosException, ValidacionException
    └── resources/
        └── db.properties.example
```

## Estándares de codificación aplicados

Basados en el informe técnico de estándares de codificación del proyecto (GA7-220501096-AA1-EV02), adaptados a Java:

| Elemento | Convención | Ejemplos del proyecto |
|---|---|---|
| Paquetes | minúsculas, sin guiones | `co.edu.sena.infomanager.dao` |
| Clases e interfaces | PascalCase, sustantivos | `UsuarioDAO`, `ConexionBD`, `UsuarioServicio` |
| Métodos | camelCase, inician con verbo | `buscarPorCorreo`, `actualizarContrasena` |
| Variables y atributos | camelCase, nombres descriptivos | `idUsuario`, `fechaRegistro` |
| Constantes | UPPER_SNAKE_CASE | `SQL_INSERTAR`, `LONGITUD_MINIMA_CONTRASENA` |
| Constantes de enum | UPPER_SNAKE_CASE | `ACTIVO`, `INACTIVO` |
| Tablas y columnas SQL | snake_case | `usuarios`, `id_usuario`, `fecha_registro` |

Otras reglas: identificadores en español; comentarios Javadoc en clases y métodos públicos; llaves siempre presentes; sangría de 4 espacios (convención habitual en Java; el informe de estándares usa 2 espacios para JavaScript); todas las consultas con `PreparedStatement` y los recursos JDBC cerrados con `try-with-resources`.

## Solución de problemas

- **`No se encontró el archivo db.properties`**: falta copiar `db.properties.example` como `db.properties` (paso 2).
- **`Access denied for user`**: usuario o contraseña incorrectos en `db.properties`.
- **`Communications link failure`**: MySQL no está en ejecución o el puerto/URL es distinto de `localhost:3306`.
- **`Unknown database 'infomanager_db'`**: falta ejecutar `01_infomanager_system_script.sql`.
- **Tildes o caracteres extraños en la consola de Windows**: ejecute desde la consola del IDE, o use `chcp 65001` antes de ejecutar en CMD.
