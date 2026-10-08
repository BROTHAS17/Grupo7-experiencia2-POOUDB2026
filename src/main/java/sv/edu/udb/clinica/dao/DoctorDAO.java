package sv.edu.udb.clinica.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import sv.edu.udb.clinica.conexion.ConexionBD;
import sv.edu.udb.clinica.modelo.Doctor;

/**
 * Operaciones de inserción, actualización y consulta sobre la tabla doctores.
 */
public class DoctorDAO {

    private static final String COLUMNAS =
            "id_doctor, nombres, apellidos, especialidad, numero_jvpm, telefono, correo";

    private static final String SQL_INSERTAR =
            "INSERT INTO doctores (nombres, apellidos, especialidad, numero_jvpm, telefono, correo) "
            + "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SQL_ACTUALIZAR =
            "UPDATE doctores SET nombres = ?, apellidos = ?, especialidad = ?, numero_jvpm = ?, "
            + "telefono = ?, correo = ? WHERE id_doctor = ?";

    private static final String SQL_BUSCAR_POR_ID =
            "SELECT " + COLUMNAS + " FROM doctores WHERE id_doctor = ?";

    private static final String SQL_LISTAR =
            "SELECT " + COLUMNAS + " FROM doctores ORDER BY apellidos, nombres";

    private static final String SQL_LISTAR_POR_ESPECIALIDAD =
            "SELECT " + COLUMNAS + " FROM doctores WHERE especialidad = ? ORDER BY apellidos, nombres";

    /**
     * Inserta un doctor y le asigna el id generado por la base de datos.
     *
     * @return id del nuevo doctor
     */
    public int insertar(Doctor doctor) throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion();
                PreparedStatement ps = con.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, doctor.getNombres());
            ps.setString(2, doctor.getApellidos());
            ps.setString(3, doctor.getEspecialidad());
            ps.setString(4, doctor.getNumeroJvpm());
            ps.setString(5, doctor.getTelefono());
            ps.setString(6, doctor.getCorreo());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    doctor.setIdDoctor(rs.getInt(1));
                }
            }
            return doctor.getIdDoctor();
        }
    }

    /**
     * Actualiza los datos del doctor según su id.
     *
     * @return true si se modificó algún registro
     */
    public boolean actualizar(Doctor doctor) throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion();
                PreparedStatement ps = con.prepareStatement(SQL_ACTUALIZAR)) {
            ps.setString(1, doctor.getNombres());
            ps.setString(2, doctor.getApellidos());
            ps.setString(3, doctor.getEspecialidad());
            ps.setString(4, doctor.getNumeroJvpm());
            ps.setString(5, doctor.getTelefono());
            ps.setString(6, doctor.getCorreo());
            ps.setInt(7, doctor.getIdDoctor());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * @return el doctor con ese id, o null si no existe
     */
    public Doctor buscarPorId(int idDoctor) throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion();
                PreparedStatement ps = con.prepareStatement(SQL_BUSCAR_POR_ID)) {
            ps.setInt(1, idDoctor);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public List<Doctor> listar() throws SQLException {
        List<Doctor> doctores = new ArrayList<>();
        try (Connection con = ConexionBD.obtenerConexion();
                PreparedStatement ps = con.prepareStatement(SQL_LISTAR);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                doctores.add(mapear(rs));
            }
        }
        return doctores;
    }

    public List<Doctor> listarPorEspecialidad(String especialidad) throws SQLException {
        List<Doctor> doctores = new ArrayList<>();
        try (Connection con = ConexionBD.obtenerConexion();
                PreparedStatement ps = con.prepareStatement(SQL_LISTAR_POR_ESPECIALIDAD)) {
            ps.setString(1, especialidad);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    doctores.add(mapear(rs));
                }
            }
        }
        return doctores;
    }

    private Doctor mapear(ResultSet rs) throws SQLException {
        Doctor d = new Doctor();
        d.setIdDoctor(rs.getInt("id_doctor"));
        d.setNombres(rs.getString("nombres"));
        d.setApellidos(rs.getString("apellidos"));
        d.setEspecialidad(rs.getString("especialidad"));
        d.setNumeroJvpm(rs.getString("numero_jvpm"));
        d.setTelefono(rs.getString("telefono"));
        d.setCorreo(rs.getString("correo"));
        return d;
    }
}
