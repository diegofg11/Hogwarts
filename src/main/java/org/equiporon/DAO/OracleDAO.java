package org.equiporon.DAO;

import org.equiporon.Conexion.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * DAO para gestionar operaciones CRUD en Oracle (Ravenclaw)
 * sobre la tabla ESTUDIANTES.
 *
 * @author Xiker
 * @version 1.2
 */
public class OracleDAO {

    private final String casa = "Ravenclaw";

    /**
     * Inserta un nuevo estudiante.
     *
     * @param id        Identificador único (DNI u otro).
     * @param nombre    Nombre del estudiante.
     * @param apellidos Apellidos del estudiante.
     * @param curso     Curso actual.
     * @param patronus  Patronus del estudiante.
     * @return true si se inserta correctamente, false en caso de error.
     */
    public boolean aniadir(String id, String nombre, String apellidos, int curso, String patronus) {
        String sql = "INSERT INTO ESTUDIANTES (ID, NOMBRE, APELLIDOS, CURSO, PATRONUS) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = ConexionBD.conectarCasa(casa);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id);
            ps.setString(2, nombre);
            ps.setString(3, apellidos);
            ps.setInt(4, curso);
            ps.setString(5, patronus);
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println("Error al añadir estudiante en Oracle: " + e.getMessage());
            return false;
        }
    }

    /**
     * Edita un estudiante existente.
     *
     * @param id        Identificador del estudiante.
     * @param nombre    Nuevo nombre.
     * @param apellidos Nuevos apellidos.
     * @param curso     Nuevo curso.
     * @param patronus  Nuevo patronus.
     * @return true si se actualiza correctamente, false en caso de error.
     */
    public boolean editar(String id, String nombre, String apellidos, int curso, String patronus) {
        String sql = "UPDATE ESTUDIANTES SET NOMBRE = ?, APELLIDOS = ?, CURSO = ?, PATRONUS = ? WHERE ID = ?";
        try (Connection conn = ConexionBD.conectarCasa(casa);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ps.setString(2, apellidos);
            ps.setInt(3, curso);
            ps.setString(4, patronus);
            ps.setString(5, id);
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println("Error al editar estudiante en Oracle: " + e.getMessage());
            return false;
        }
    }

    /**
     * Borra un estudiante por su ID.
     *
     * @param id Identificador del estudiante.
     * @return true si se borra correctamente, false en caso de error.
     */
    public boolean borrar(String id) {
        String sql = "DELETE FROM ESTUDIANTES WHERE ID = ?";
        try (Connection conn = ConexionBD.conectarCasa(casa);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id);
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println("Error al borrar estudiante en Oracle: " + e.getMessage());
            return false;
        }
    }
}
