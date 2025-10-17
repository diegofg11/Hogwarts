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
 * con la base de datos MariaDB.
 *
 * Contiene métodos para insertar, obtener, actualizar y eliminar registros
 * de estudiantes.
 *
 * @author Diego,Unai
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
        return dbExecutor.submit(() -> aniadirSync(estudiante));
    }

    public Future<List<Modelo_Estudiante>> getAllAsync() {
        return dbExecutor.submit(this::getAllSync);
    }

    public Future<Boolean> editarAsync(Modelo_Estudiante estudiante) {
        return dbExecutor.submit(() -> editarSync(estudiante));
    }

    public Future<Boolean> borrarAsync(int id) {
        return dbExecutor.submit(() -> borrarSync(id));
    }

    // --------------------------------------------------------
    // --- MÉTODOS SÍNCRONOS (Internal) ---
    // --------------------------------------------------------

    /** [SÍNCRONO] Inserta un nuevo estudiante en la base de datos.
     *
     * @param estudiante Objeto Modelo_Estudiante con los datos a guardar.
     * @return true si la inserción fue exitosa, false en caso contrario.
     * @author Diego,Unai
     */
    private boolean aniadirSync(Modelo_Estudiante estudiante) throws SQLException {
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

    /** [SÍNCRONO] Obtiene todos los estudiantes de la base de datos.
     *
     * @return Lista con todos los estudiantes encontrados.
     * @author Diego,Unai
     */
    private List<Modelo_Estudiante> getAllSync() throws SQLException {
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

    /** [SÍNCRONO] Actualiza los datos de un estudiante existente.
     *
     * @param estudiante Objeto con los nuevos datos del estudiante.
     * @return true si la actualización fue exitosa, false si ocurrió un error.
     * @author Diego,Unai
     */
    private boolean editarSync(Modelo_Estudiante estudiante) throws SQLException {
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

    /** [SÍNCRONO] Elimina un estudiante de la base de datos por su ID.
     *
     * @param id Identificador del estudiante a eliminar.
     * @return true si el estudiante fue eliminado correctamente, false en caso contrario.
     * @author Diego,Unai
     */
    private boolean borrarSync(int id) throws SQLException {
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