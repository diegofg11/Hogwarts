package org.equiporon.DAO;

import org.equiporon.Conexion.ConexionBD;
import org.equiporon.Modelo.Modelo_Estudiante;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import org.slf4j.Logger; // Necesario para el logging
import org.slf4j.LoggerFactory;

/**
 * Clase MariaDBDAO encargada de manejar la conexión y operaciones
 * con la base de datos MariaDB (Hogwarts Central) de forma ASÍNCRONA.
 */
public class MariaDBDAO {

    private static final Logger logger = LoggerFactory.getLogger(MariaDBDAO.class);
    // 🚨 AÑADIDO: ExecutorService para ejecutar tareas en un pool separado
    private static final ExecutorService dbExecutor = Executors.newFixedThreadPool(1);

    // --------------------------------------------------------
    // --- MÉTODOS ASÍNCRONOS (Public) ---
    // --------------------------------------------------------

    public Future<Boolean> insertarEstudianteAsync(Modelo_Estudiante estudiante) {
        // Ejecuta la inserción en el hilo del pool y devuelve un Future
        return dbExecutor.submit(() -> insertarEstudianteSync(estudiante));
    }

    public Future<List<Modelo_Estudiante>> obtenerTodosAsync() {
        return dbExecutor.submit(this::obtenerTodosSync);
    }

    public Future<Boolean> actualizarEstudianteAsync(Modelo_Estudiante estudiante) {
        return dbExecutor.submit(() -> actualizarEstudianteSync(estudiante));
    }

    public Future<Boolean> eliminarEstudianteAsync(int id) {
        return dbExecutor.submit(() -> eliminarEstudianteSync(id));
    }

    // --------------------------------------------------------
    // --- MÉTODOS SÍNCRONOS (Internal) ---
    // --------------------------------------------------------

    /** [SÍNCRONO] Inserta un nuevo estudiante. */
    private boolean insertarEstudianteSync(Modelo_Estudiante estudiante) throws SQLException {
        String sql = "INSERT INTO estudiantes (nombre, apellidos, casa, curso, patronus) VALUES (?, ?, ?, ?, ?)";
        // 🚨 USO DE LOGGER EN LUGAR DE System.out.println
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, estudiante.getNombre());
            stmt.setString(2, estudiante.getApellidos());
            stmt.setString(3, estudiante.getCasa());
            stmt.setInt(4, estudiante.getCurso());
            stmt.setString(5, estudiante.getPatronus());
            stmt.executeUpdate();
            logger.info("Inserción exitosa en MariaDB.");
            return true;

        } catch (SQLException e) {
            logger.error("Error SÍNCRONO al insertar estudiante en MariaDB.", e);
            throw e;
        }
    }

    /** [SÍNCRONO] Obtiene todos los estudiantes. */
    private List<Modelo_Estudiante> obtenerTodosSync() throws SQLException {
        List<Modelo_Estudiante> estudiantes = new ArrayList<>();
        String sql = "SELECT id, nombre, apellidos, casa, curso, patronus FROM estudiantes";

        try (Connection conn = ConexionBD.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // ... (código para construir Modelo_Estudiante)
                Modelo_Estudiante estudiante = new Modelo_Estudiante(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("apellidos"),
                        rs.getString("casa"),
                        rs.getInt("curso"),
                        rs.getString("patronus")
                );
                estudiantes.add(estudiante);
            }
            return estudiantes;

        } catch (SQLException e) {
            logger.error("Error SÍNCRONO al obtener estudiantes de MariaDB.", e);
            throw e;
        }
    }

    /** [SÍNCRONO] Actualiza un estudiante. */
    private boolean actualizarEstudianteSync(Modelo_Estudiante estudiante) throws SQLException {
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
            logger.info("Actualización exitosa en MariaDB para ID: {}", estudiante.getId());
            return true;

        } catch (SQLException e) {
            logger.error("Error SÍNCRONO al actualizar estudiante en MariaDB.", e);
            throw e;
        }
    }

    /** [SÍNCRONO] Elimina un estudiante. */
    private boolean eliminarEstudianteSync(int id) throws SQLException {
        String sql = "DELETE FROM estudiantes WHERE id = ?";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
            logger.info("Eliminación exitosa en MariaDB para ID: {}", id);
            return true;

        } catch (SQLException e) {
            logger.error("Error SÍNCRONO al eliminar estudiante en MariaDB.", e);
            throw e;
        }
    }

    /** Cierra el pool de hilos. */
    public static void shutdown() {
        if (!dbExecutor.isShutdown()) {
            dbExecutor.shutdown();
            logger.info("Pool de hilos de MariaDBDAO cerrado.");
        }
    }
}