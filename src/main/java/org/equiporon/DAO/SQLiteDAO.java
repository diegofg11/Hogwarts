package org.equiporon.DAO;

import org.equiporon.Conexion.ConexionBD;
import org.equiporon.Modelo.Modelo_Estudiante;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase SQLiteDAO encargada de manejar la conexión y operaciones
 * con la base de datos SQLite.
 *
 * Incluye métodos para crear la tabla, insertar, obtener,
 * actualizar y eliminar registros de estudiantes.
 *
 * @author Diego
 */
public class SQLiteDAO {

    /**
     * Crea la tabla de estudiantes si no existe.
     */
    public void crearTabla() {
        String sql = "CREATE TABLE IF NOT EXISTS estudiantes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nombre TEXT," +
                "apellidos TEXT," +
                "casa TEXT," +
                "curso INTEGER," +
                "patronus TEXT)";
        try (Connection conn = ConexionBD.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println("Error al crear tabla: " + e.getMessage());
        }
    }

    /**
     * Inserta un nuevo estudiante en la base de datos.
     *
     * @param estudiante Objeto Modelo_Estudiante con los datos a guardar.
     * @return true si la inserción fue exitosa, false en caso contrario.
     */
    public boolean insertarEstudiante(Modelo_Estudiante estudiante) {
        String sql = "INSERT INTO estudiantes (nombre, apellidos, casa, curso, patronus) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, estudiante.getNombre());
            stmt.setString(2, estudiante.getApellidos());
            stmt.setString(3, estudiante.getCasa());
            stmt.setInt(4, estudiante.getCurso());
            stmt.setString(5, estudiante.getPatronus());
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println("Error al insertar estudiante: " + e.getMessage());
            return false;
        }
    }

    /**
     * Obtiene todos los estudiantes de la base de datos.
     *
     * @return Lista con todos los estudiantes encontrados.
     */
    public List<Modelo_Estudiante> obtenerTodos() {
        List<Modelo_Estudiante> lista = new ArrayList<>();
        String sql = "SELECT * FROM estudiantes";
        try (Connection conn = ConexionBD.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(new Modelo_Estudiante(
                        rs.getString("id"),
                        rs.getString("nombre"),
                        rs.getString("apellidos"),
                        rs.getString("casa"),
                        rs.getInt("curso"),
                        rs.getString("patronus")
                ));
            }


        } catch (SQLException e) {
            System.out.println("Error al obtener estudiantes: " + e.getMessage());
        }
        return lista;
    }

    /**
     * Actualiza los datos de un estudiante existente.
     *
     * @param estudiante Objeto con los nuevos datos del estudiante.
     * @return true si la actualización fue exitosa, false si ocurrió un error.
     */
    public boolean actualizarEstudiante(Modelo_Estudiante estudiante) {
        String sql = "UPDATE estudiantes SET nombre = ?, apellidos = ?, casa = ?, curso = ?, patronus = ? WHERE id = ?";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, estudiante.getNombre());
            stmt.setString(2, estudiante.getApellidos());
            stmt.setString(3, estudiante.getCasa());
            stmt.setInt(4, estudiante.getCurso());
            stmt.setString(5, estudiante.getPatronus());
            stmt.setString(6, estudiante.getId());
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println("Error al actualizar estudiante: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina un estudiante de la base de datos por su ID.
     *
     * @param id Identificador del estudiante a eliminar.
     * @return true si el estudiante fue eliminado correctamente, false en caso contrario.
     */
    public boolean eliminarEstudiante(int id) {
        String sql = "DELETE FROM estudiantes WHERE id = ?";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println("Error al eliminar estudiante: " + e.getMessage());
            return false;
        }
    }
}


