package sv.edu.udb.clinica;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import sv.edu.udb.clinica.conexion.ConexionBD;
import sv.edu.udb.clinica.dao.CitaMedicaDAO;
import sv.edu.udb.clinica.dao.DoctorDAO;
import sv.edu.udb.clinica.dao.PacienteDAO;
import sv.edu.udb.clinica.modelo.CitaMedica;
import sv.edu.udb.clinica.modelo.Doctor;
import sv.edu.udb.clinica.modelo.Paciente;

// Verifica que la conexión JDBC funcione y muestra los datos de las tres tablas
// Ejecutar después de cargar database/citas_medicas.sql en MySQL

public class PruebaConexion {

    public static void main(String[] args) {
        try (Connection con = ConexionBD.obtenerConexion()) {
            DatabaseMetaData meta = con.getMetaData();
            System.out.println("Conexión exitosa a " + meta.getURL());
            System.out.println("Servidor: " + meta.getDatabaseProductName() + " " + meta.getDatabaseProductVersion());
        } catch (SQLException e) {
            System.err.println("No se pudo conectar a la base de datos: " + e.getMessage());
            System.err.println("Revise el usuario y la contraseña en src/main/resources/db.properties");
            return;
        }

        try {
            System.out.println("\n--- Pacientes ---");
            for (Paciente p : new PacienteDAO().listar()) {
                System.out.println(p + " | DUI: " + p.getDui() + " | Tel: " + p.getTelefono());
            }

            System.out.println("\n--- Doctores ---");
            for (Doctor d : new DoctorDAO().listar()) {
                System.out.println(d.getIdDoctor() + " - " + d + " | JVPM: " + d.getNumeroJvpm());
            }

            System.out.println("\n--- Citas médicas ---");
            for (CitaMedica c : new CitaMedicaDAO().listar()) {
                System.out.println(c + " | Paciente: " + c.getNombrePaciente() + " | Doctor: " + c.getNombreDoctor());
            }
        } catch (SQLException e) {
            System.err.println("Error al consultar los datos: " + e.getMessage());
        }
    }
}
