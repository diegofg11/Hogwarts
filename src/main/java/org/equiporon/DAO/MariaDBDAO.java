
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
 * DAO de la base de datos de Hogwarts en MariaDB
 *
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

    // --- MÉTODOS ASÍNCRONOS (Public) ---

    /**
     * Inserta un nuevo estudiante de forma asíncrona.
     * @param estudiante Objeto Modelo_Estudiante con los datos a guardar.
     * @return Future<Boolean> que indica si la inserción fue exitosa.
     * @author Unai
     */
    public Future<Boolean> aniadirAsync(Modelo_Estudiante estudiante) {
        return dbExecutor.submit(() -> aniadirSync(estudiante));
    }

    /**
     * Edita un estudiante existente de forma asíncrona.
     * @param estudiante Objeto Modelo_Estudiante con los nuevos datos (incluyendo ID).
     * @return Future<Boolean> que indica si la edición fue exitosa.
     * @author Unai
     */
    public Future<Boolean> editarAsync(Modelo_Estudiante estudiante) {
        return dbExecutor.submit(() -> editarSync(estudiante));
    }

    /**
     * Borra un estudiante por su ID de forma asíncrona.
     * @param id Identificador del estudiante a borrar (String).
     * @return Future<Boolean> que indica si el borrado fue exitoso.
     * @author Unai
     */
    public Future<Boolean> borrarAsync(String id) {
        return dbExecutor.submit(() -> borrarSync(id));
    }

    /**
     * Devuelve todos los alumnos de forma asíncrona.
     * @return Future<List<Modelo_Estudiante>> que devuelve todos los alumnos.
     * @author Unai
     */
    public Future<List<Modelo_Estudiante>> getAllAsync() {
        return dbExecutor.submit(this::getAllSync);
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
        String sql = "INSERT INTO ESTUDIANTES (nombre, apellidos, casa, curso, patronus) VALUES (?, ?, ?, ?, ?)";
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
        String sql = "SELECT id, nombre, apellidos, casa, curso, patronus FROM ESTUDIANTES";

        try (Connection conn = ConexionBD.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // ... (código para construir Modelo_Estudiante)
                Modelo_Estudiante estudiante = new Modelo_Estudiante(
                        rs.getString("id"),
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
        String sql = "UPDATE ESTUDIANTES SET nombre = ?, apellidos = ?, casa = ?, curso = ?, patronus = ? WHERE id = ?";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, estudiante.getNombre());
            stmt.setString(2, estudiante.getApellidos());
            stmt.setString(3, estudiante.getCasa());
            stmt.setInt(4, estudiante.getCurso());
            stmt.setString(5, estudiante.getPatronus());
            stmt.setString(6, estudiante.getId());
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
    private boolean borrarSync(String id) throws SQLException {
        String sql = "DELETE FROM ESTUDIANTES WHERE id = ?";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, id);
            stmt.executeUpdate();
            logger.info("Eliminación exitosa en MariaDB para ID: {}", id);
            return true;

        } catch (SQLException e) {
            logger.error("Error SÍNCRONO al eliminar estudiante en MariaDB.", e);
            throw e;
        }
    }
    // ================================================================
    // === MÉTODOS DE SINCRONIZACIÓN ENTRE HOGWARTS Y LAS CASAS =======
    // ================================================================

    /**
     * Inserta un estudiante en la base central (MariaDB) procedente de una casa.
     * @param e Estudiante a insertar.
     * @param casaOrigen Nombre de la casa desde donde se origina el cambio.
     * @author Gaizka
     */
    public boolean sincronizarInsert(Modelo_Estudiante e, String casaOrigen, String idConPrefijo) {
        final String sql = "INSERT INTO ESTUDIANTES (id, nombre, apellidos, casa, curso, patronus) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, idConPrefijo);
            ps.setString(2, e.getNombre());
            ps.setString(3, e.getApellidos());
            ps.setString(4, casaOrigen); // 🔹 La casa se pasa desde el DAO local
            ps.setInt(5, e.getCurso());
            ps.setString(6, e.getPatronus());
            ps.executeUpdate();

            logger.info("Sincronización INSERT desde {} hacia MariaDB exitosa (ID {}).", casaOrigen, e.getId());
            return true;

        } catch (SQLException ex) {
            logger.error("Error al sincronizar INSERT desde {} hacia MariaDB.", casaOrigen, ex);
            return false;
        }
    }

    /**
     * Actualiza un estudiante en la base central (MariaDB) procedente de una casa.
     * @param e Estudiante con datos actualizados.
     * @param casaOrigen Nombre de la casa desde donde se origina el cambio.
     * @author Gaizka
     */
    public boolean sincronizarUpdate(Modelo_Estudiante e, String casaOrigen, String idConPrefijo) {
        final String sql = "UPDATE ESTUDIANTES SET nombre=?, apellidos=?, curso=?, patronus=?, casa=? WHERE id=?";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, e.getNombre());
            ps.setString(2, e.getApellidos());
            ps.setInt(3, e.getCurso());
            ps.setString(4, e.getPatronus());
            ps.setString(5, casaOrigen);
            ps.setString(6, idConPrefijo);
            ps.executeUpdate();

            logger.info("Sincronización UPDATE desde {} hacia MariaDB exitosa (ID {}).", casaOrigen, e.getId());
            return true;

        } catch (SQLException ex) {
            logger.error("Error al sincronizar UPDATE desde {} hacia MariaDB.", casaOrigen, ex);
            return false;
        }
    }

    /**
     * Elimina un estudiante en la base central (MariaDB) procedente de una casa.
     * @param idConPrefijo ID del estudiante a eliminar.
     * @param casaOrigen Nombre de la casa desde donde se origina el cambio.
     * @author Gaizka
     */
    public boolean sincronizarDelete(String idConPrefijo, String casaOrigen) {
        final String sql = "DELETE FROM ESTUDIANTES WHERE id = ?";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, idConPrefijo);
            ps.executeUpdate();

            logger.info("Sincronización DELETE desde {} hacia MariaDB exitosa (ID {}).", casaOrigen, idConPrefijo);
            return true;

        } catch (SQLException ex) {
            logger.error("Error al sincronizar DELETE desde {} hacia MariaDB.", casaOrigen, ex);
            return false;
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