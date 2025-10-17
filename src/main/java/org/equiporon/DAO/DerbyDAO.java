package org.equiporon.DAO;

import org.equiporon.Conexion.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * DAO para gestionar operaciones CRUD en Apache Derby (Hufflepuff)
 * sobre la tabla ESTUDIANTES.
 *
 * @author Xiker
 * @version 1.2
 */
public class DerbyDAO {

    private final String casa = "Hufflepuff";

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
            System.out.println("Error al añadir estudiante en Derby: " + e.getMessage());
            return false;
        }
    }

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
            System.out.println("Error al editar estudiante en Derby: " + e.getMessage());
            return false;
        }
    }

    public boolean borrar(String id) {
        String sql = "DELETE FROM ESTUDIANTES WHERE ID = ?";
        try (Connection conn = ConexionBD.conectarCasa(casa);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id);
            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println("Error al borrar estudiante en Derby: " + e.getMessage());
            return false;
        }
    }
}
