package sv.edu.udb.clinica.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import sv.edu.udb.clinica.conexion.ConexionBD;
import sv.edu.udb.clinica.modelo.Paciente;

/**
 * Operaciones de inserción, actualización y consulta sobre la tabla pacientes.
 */
public class PacienteDAO {

    private static final String COLUMNAS =
            "id_paciente, nombres, apellidos, dui, fecha_nacimiento, genero, "
            + "telefono, correo, direccion, fecha_registro";

    private static final String SQL_INSERTAR =
            "INSERT INTO pacientes (nombres, apellidos, dui, fecha_nacimiento, genero, "
            + "telefono, correo, direccion) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_ACTUALIZAR =
            "UPDATE pacientes SET nombres = ?, apellidos = ?, dui = ?, fecha_nacimiento = ?, "
            + "genero = ?, telefono = ?, correo = ?, direccion = ? WHERE id_paciente = ?";

    private static final String SQL_BUSCAR_POR_ID =
            "SELECT " + COLUMNAS + " FROM pacientes WHERE id_paciente = ?";

    private static final String SQL_BUSCAR_POR_DUI =
            "SELECT " + COLUMNAS + " FROM pacientes WHERE dui = ?";

    private static final String SQL_LISTAR =
            "SELECT " + COLUMNAS + " FROM pacientes ORDER BY apellidos, nombres";

    /**
     * Inserta un paciente y le asigna el id generado por la base de datos.
     *
     * @return id del nuevo paciente
     */
    public int insertar(Paciente paciente) throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion();
                PreparedStatement ps = con.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, paciente.getNombres());
            ps.setString(2, paciente.getApellidos());
            ps.setString(3, paciente.getDui());
            ps.setObject(4, paciente.getFechaNacimiento());
            ps.setString(5, paciente.getGenero());
            ps.setString(6, paciente.getTelefono());
            ps.setString(7, paciente.getCorreo());
            ps.setString(8, paciente.getDireccion());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    paciente.setIdPaciente(rs.getInt(1));
                }
            }
            return paciente.getIdPaciente();
        }
    }

    /**
     * Actualiza los datos del paciente según su id.
     *
     * @return true si se modificó algún registro
     */
    public boolean actualizar(Paciente paciente) throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion();
                PreparedStatement ps = con.prepareStatement(SQL_ACTUALIZAR)) {
            ps.setString(1, paciente.getNombres());
            ps.setString(2, paciente.getApellidos());
            ps.setString(3, paciente.getDui());
            ps.setObject(4, paciente.getFechaNacimiento());
            ps.setString(5, paciente.getGenero());
            ps.setString(6, paciente.getTelefono());
            ps.setString(7, paciente.getCorreo());
            ps.setString(8, paciente.getDireccion());
            ps.setInt(9, paciente.getIdPaciente());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * @return el paciente con ese id, o null si no existe
     */
    public Paciente buscarPorId(int idPaciente) throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion();
                PreparedStatement ps = con.prepareStatement(SQL_BUSCAR_POR_ID)) {
            ps.setInt(1, idPaciente);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    /**
     * @return el paciente con ese DUI, o null si no existe
     */
    public Paciente buscarPorDui(String dui) throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion();
                PreparedStatement ps = con.prepareStatement(SQL_BUSCAR_POR_DUI)) {
            ps.setString(1, dui);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public List<Paciente> listar() throws SQLException {
        List<Paciente> pacientes = new ArrayList<>();
        try (Connection con = ConexionBD.obtenerConexion();
                PreparedStatement ps = con.prepareStatement(SQL_LISTAR);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                pacientes.add(mapear(rs));
            }
        }
        return pacientes;
    }

    private Paciente mapear(ResultSet rs) throws SQLException {
        Paciente p = new Paciente();
        p.setIdPaciente(rs.getInt("id_paciente"));
        p.setNombres(rs.getString("nombres"));
        p.setApellidos(rs.getString("apellidos"));
        p.setDui(rs.getString("dui"));
        p.setFechaNacimiento(rs.getObject("fecha_nacimiento", LocalDate.class));
        p.setGenero(rs.getString("genero"));
        p.setTelefono(rs.getString("telefono"));
        p.setCorreo(rs.getString("correo"));
        p.setDireccion(rs.getString("direccion"));
        p.setFechaRegistro(rs.getObject("fecha_registro", LocalDateTime.class));
        return p;
    }
}
