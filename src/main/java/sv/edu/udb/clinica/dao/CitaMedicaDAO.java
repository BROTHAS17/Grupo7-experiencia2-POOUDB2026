package sv.edu.udb.clinica.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import sv.edu.udb.clinica.conexion.ConexionBD;
import sv.edu.udb.clinica.modelo.CitaMedica;

/**
 * Operaciones de inserción, actualización y consulta sobre la tabla citas_medicas.
 */
public class CitaMedicaDAO {

    private static final String SQL_INSERTAR =
            "INSERT INTO citas_medicas (id_paciente, id_doctor, fecha, hora, motivo, estado, observaciones) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_ACTUALIZAR =
            "UPDATE citas_medicas SET id_paciente = ?, id_doctor = ?, fecha = ?, hora = ?, motivo = ?, "
            + "estado = ?, observaciones = ? WHERE id_cita = ?";

    private static final String SQL_ACTUALIZAR_ESTADO =
            "UPDATE citas_medicas SET estado = ? WHERE id_cita = ?";

    // Trae también el nombre del paciente y del doctor para mostrarlos en pantalla.
    private static final String SQL_SELECT_BASE =
            "SELECT c.id_cita, c.id_paciente, c.id_doctor, c.fecha, c.hora, c.motivo, c.estado, "
            + "c.observaciones, "
            + "CONCAT(p.nombres, ' ', p.apellidos) AS nombre_paciente, "
            + "CONCAT(d.nombres, ' ', d.apellidos) AS nombre_doctor "
            + "FROM citas_medicas c "
            + "INNER JOIN pacientes p ON p.id_paciente = c.id_paciente "
            + "INNER JOIN doctores d ON d.id_doctor = c.id_doctor ";

    private static final String ORDEN = " ORDER BY c.fecha, c.hora";

    private static final String SQL_BUSCAR_POR_ID = SQL_SELECT_BASE + "WHERE c.id_cita = ?";
    private static final String SQL_LISTAR = SQL_SELECT_BASE + ORDEN;
    private static final String SQL_LISTAR_POR_PACIENTE = SQL_SELECT_BASE + "WHERE c.id_paciente = ?" + ORDEN;
    private static final String SQL_LISTAR_POR_DOCTOR = SQL_SELECT_BASE + "WHERE c.id_doctor = ?" + ORDEN;

    private static final String SQL_EXISTE_EN_HORARIO =
            "SELECT COUNT(*) FROM citas_medicas "
            + "WHERE id_doctor = ? AND fecha = ? AND hora = ? AND estado <> 'CANCELADA' AND id_cita <> ?";

    /**
     * Inserta una cita y le asigna el id generado por la base de datos.
     *
     * @return id de la nueva cita
     */
    public int insertar(CitaMedica cita) throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion();
                PreparedStatement ps = con.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, cita.getIdPaciente());
            ps.setInt(2, cita.getIdDoctor());
            ps.setObject(3, cita.getFecha());
            ps.setObject(4, cita.getHora());
            ps.setString(5, cita.getMotivo());
            ps.setString(6, cita.getEstado().name());
            ps.setString(7, cita.getObservaciones());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    cita.setIdCita(rs.getInt(1));
                }
            }
            return cita.getIdCita();
        }
    }

    /**
     * Actualiza todos los datos de la cita según su id.
     *
     * @return true si se modificó algún registro
     */
    public boolean actualizar(CitaMedica cita) throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion();
                PreparedStatement ps = con.prepareStatement(SQL_ACTUALIZAR)) {
            ps.setInt(1, cita.getIdPaciente());
            ps.setInt(2, cita.getIdDoctor());
            ps.setObject(3, cita.getFecha());
            ps.setObject(4, cita.getHora());
            ps.setString(5, cita.getMotivo());
            ps.setString(6, cita.getEstado().name());
            ps.setString(7, cita.getObservaciones());
            ps.setInt(8, cita.getIdCita());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Cambia solo el estado de la cita (por ejemplo, para cancelarla o marcarla como atendida).
     *
     * @return true si se modificó algún registro
     */
    public boolean actualizarEstado(int idCita, CitaMedica.Estado estado) throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion();
                PreparedStatement ps = con.prepareStatement(SQL_ACTUALIZAR_ESTADO)) {
            ps.setString(1, estado.name());
            ps.setInt(2, idCita);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * @return la cita con ese id, o null si no existe
     */
    public CitaMedica buscarPorId(int idCita) throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion();
                PreparedStatement ps = con.prepareStatement(SQL_BUSCAR_POR_ID)) {
            ps.setInt(1, idCita);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public List<CitaMedica> listar() throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion();
                PreparedStatement ps = con.prepareStatement(SQL_LISTAR)) {
            return ejecutarConsulta(ps);
        }
    }

    public List<CitaMedica> listarPorPaciente(int idPaciente) throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion();
                PreparedStatement ps = con.prepareStatement(SQL_LISTAR_POR_PACIENTE)) {
            ps.setInt(1, idPaciente);
            return ejecutarConsulta(ps);
        }
    }

    public List<CitaMedica> listarPorDoctor(int idDoctor) throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion();
                PreparedStatement ps = con.prepareStatement(SQL_LISTAR_POR_DOCTOR)) {
            ps.setInt(1, idDoctor);
            return ejecutarConsulta(ps);
        }
    }

    /**
     * Indica si el doctor ya tiene otra cita (no cancelada) en la misma fecha y hora.
     * Sirve para validar antes de insertar o reprogramar; al actualizar se ignora la misma cita.
     */
    public boolean existeCitaEnHorario(CitaMedica cita) throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion();
                PreparedStatement ps = con.prepareStatement(SQL_EXISTE_EN_HORARIO)) {
            ps.setInt(1, cita.getIdDoctor());
            ps.setObject(2, cita.getFecha());
            ps.setObject(3, cita.getHora());
            ps.setInt(4, cita.getIdCita());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private List<CitaMedica> ejecutarConsulta(PreparedStatement ps) throws SQLException {
        List<CitaMedica> citas = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                citas.add(mapear(rs));
            }
        }
        return citas;
    }

    private CitaMedica mapear(ResultSet rs) throws SQLException {
        CitaMedica c = new CitaMedica();
        c.setIdCita(rs.getInt("id_cita"));
        c.setIdPaciente(rs.getInt("id_paciente"));
        c.setIdDoctor(rs.getInt("id_doctor"));
        c.setFecha(rs.getObject("fecha", LocalDate.class));
        c.setHora(rs.getObject("hora", LocalTime.class));
        c.setMotivo(rs.getString("motivo"));
        c.setEstado(CitaMedica.Estado.valueOf(rs.getString("estado")));
        c.setObservaciones(rs.getString("observaciones"));
        c.setNombrePaciente(rs.getString("nombre_paciente"));
        c.setNombreDoctor(rs.getString("nombre_doctor"));
        return c;
    }
}
