package org.equiporon.DAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLiteDAO {

    // Crear tabla si no existe
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

    // INSERTAR
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

    // OBTENER TODOS
    public List<Modelo_Estudiante> obtenerTodos() {
        List<Modelo_Estudiante> lista = new ArrayList<>();
        String sql = "SELECT * FROM estudiantes";
        try (Connection conn = ConexionBD.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Modelo_Estudiante e = new Modelo_Estudiante();
                e.setId(rs.getInt("id"));
                e.setNombre(rs.getString("nombre"));
                e.setApellidos(rs.getString("apellidos"));
                e.setCasa(rs.getString("casa"));
                e.setCurso(rs.getInt("curso"));
                e.setPatronus(rs.getString("patronus"));
                lista.add(e);
            }

        } catch (SQLException e) {
            System.out.println("Error al obtener estudiantes: " + e.getMessage());
        }
        return lista;
    }

    // EDITAR
    public boolean actualizarEstudiante(Modelo_Estudiante estudiante) {
        String sql = "UPDATE estudiantes SET nombre = ?, apellidos = ?, casa = ?, curso = ?, patronus = ? WHERE id = ?";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, estudiante.getNombre());
            stmt.setString(2, estudiante.getApellidos());
            stmt.setString(3, estudiante.getCasa());
            stmt.setInt(4, estudiante.getCurso());
            stmt.setString(5, estudiante.getPatronus());
            stmt.setInt(6, estudiante.getId());
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.out.println("Error al actualizar estudiante: " + e.getMessage());
            return false;
        }
    }

    // ELIMINAR
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

