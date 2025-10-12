package org.equiporon.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Clase DAO para gestionar operaciones CRUD en la base de datos Oracle
 * sobre la tabla ESTUDIANTES.
 *
 * Métodos implementados:
 * - aniadir: inserta un nuevo estudiante.
 * - editar: actualiza nombre, apellidos, curso y patronus.
 * - borrar: elimina un estudiante por ID.
 *
 * @author Xiker
 * @version 1.1
 */
public class OracleDAO {

    private final Connection connection;

    /**
     * Constructor que recibe la conexión a la base de datos Oracle.
     *
     * @param connection Objeto Connection ya inicializado.
     */
    public OracleDAO(Connection connection) {
        this.connection = connection;
    }

    /**
     * Inserta un nuevo registro en la tabla ESTUDIANTES.
     *
     * @param id        Identificador único (DNI u otro).
     * @param nombre    Nombre del estudiante.
     * @param apellidos Apellidos del estudiante.
     * @param curso     Curso actual.
     * @param patronus  Patronus del estudiante.
     */
    public void aniadir(String id, String nombre, String apellidos, String curso, String patronus) {
        String sql = "INSERT INTO ESTUDIANTES (ID, NOMBRE, APELLIDOS, CURSO, PATRONUS) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, nombre);
            ps.setString(3, apellidos);
            ps.setString(4, curso);
            ps.setString(5, patronus);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al añadir estudiante en Oracle: " + e.getMessage());
        }
    }

    /**
     * Edita los datos de un estudiante existente.
     *
     * @param id        Identificador único del estudiante a actualizar.
     * @param nombre    Nuevo nombre.
     * @param apellidos Nuevos apellidos.
     * @param curso     Nuevo curso.
     * @param patronus  Nuevo patronus.
     */
    public void editar(String id, String nombre, String apellidos, String curso, String patronus) {
        String sql = "UPDATE ESTUDIANTES SET NOMBRE = ?, APELLIDOS = ?, CURSO = ?, PATRONUS = ? WHERE ID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, apellidos);
            ps.setString(3, curso);
            ps.setString(4, patronus);
            ps.setString(5, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al editar estudiante en Oracle: " + e.getMessage());
        }
    }

    /**
     * Borra un estudiante por su ID.
     *
     * @param id Identificador único del estudiante a eliminar.
     */
    public void borrar(String id) {
        String sql = "DELETE FROM ESTUDIANTES WHERE ID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al borrar estudiante en Oracle: " + e.getMessage());
        }
    }
}
