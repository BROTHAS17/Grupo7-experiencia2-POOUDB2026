# Grupo7-experiencia2-POOUDB2026
Experiencia de aprendizaje 2 POO UDB 2026 Grupo 7

## Base de datos (MySQL + JDBC)

### 1. Crear la base de datos

**Para una instalación nueva de desarrollo con datos de prueba**, ejecutar en MySQL Workbench o desde la consola:

```bash
mysql -u root -p < database/citas_medicas.sql
```

**Atención:** este script es destructivo: elimina y vuelve a crear las tablas `citas_medicas`, `doctores` y `pacientes`, por lo que borra los datos que ya existan. Úselo solo cuando quiera reiniciar la base de pruebas y después de confirmar que no necesita esos datos.

Si solo necesita crear las tablas que faltan sin eliminar tablas existentes ni cargar datos de ejemplo, use:

```bash
mysql -u root -p < database/crear_esquema.sql
```

El script `crear_esquema.sql` usa `CREATE TABLE IF NOT EXISTS`: conserva las tablas existentes, pero no modifica una estructura antigua ni agrega datos de prueba. En `database/sentencias_basicas.sql` están las sentencias de inserción, actualización y consulta de cada tabla.

### 2. Configurar la conexión

Editar `src/main/resources/db.properties` con el usuario y la contraseña de MySQL de cada uno:

```properties
db.url=jdbc:mysql://localhost:3306/citas_medicas?useSSL=false&allowPublicKeyRetrieval=true
db.usuario=root
db.password=tu_contraseña
```

### 3. Probar la conexión

Abrir el proyecto (es Maven: NetBeans, IntelliJ o VS Code lo reconocen solos) y ejecutar
`sv.edu.udb.clinica.PruebaConexion`. Si todo está bien, muestra los pacientes, doctores y citas.

### Estructura

| Paquete | Contenido |
|---------|-----------|
| `sv.edu.udb.clinica.conexion` | `ConexionBD`: abre la conexión JDBC leyendo `db.properties` |
| `sv.edu.udb.clinica.modelo` | `Paciente`, `Doctor`, `CitaMedica` (una clase por tabla) |
| `sv.edu.udb.clinica.dao` | `PacienteDAO`, `DoctorDAO`, `CitaMedicaDAO`: insertar, actualizar y consultar |

Ejemplo de uso desde otra parte de la aplicación:

```java
PacienteDAO dao = new PacienteDAO();
Paciente p = new Paciente("Juan", "Pérez", "01234567-8", LocalDate.of(1999, 4, 1),
        "M", "7000-0000", "juan@correo.com", "San Salvador");
dao.insertar(p);                 // p.getIdPaciente() ya trae el id generado
p.setTelefono("7111-1111");
dao.actualizar(p);
List<Paciente> todos = dao.listar();
```
