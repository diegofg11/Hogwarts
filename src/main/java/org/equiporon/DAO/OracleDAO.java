package org.equiporon.DAO;

import org.equiporon.Conexion.ConexionBD;
import org.equiporon.Modelo.Modelo_Estudiante;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DAO Asíncrono para gestionar operaciones CRUD en Oracle (Ravenclaw)
 * sobre la tabla ESTUDIANTES, utilizando Modelo_Estudiante.
 *
 * @author Xiker (Modificado)
 * @version 2.0
 */
public class OracleDAO {

    private static final Logger logger = LoggerFactory.getLogger(OracleDAO.class);
    private static final ExecutorService dbExecutor = Executors.newFixedThreadPool(1); // Pool dedicado para Oracle/Ravenclaw
    private final String casa = "Ravenclaw";

    // --- MÉTODOS ASÍNCRONOS (Public) ---

    /**
     * Inserta un nuevo estudiante de forma asíncrona.
     * @param estudiante Objeto Modelo_Estudiante con los datos a guardar.
     * @return Future<Boolean> que indica si la inserción fue exitosa.
     */
    public Future<Boolean> aniadirAsync(Modelo_Estudiante estudiante) {
        return dbExecutor.submit(() -> aniadirSync(estudiante));
    }

    /**
     * Edita un estudiante existente de forma asíncrona.
     * @param estudiante Objeto Modelo_Estudiante con los nuevos datos (incluyendo ID).
     * @return Future<Boolean> que indica si la edición fue exitosa.
     */
    public Future<Boolean> editarAsync(Modelo_Estudiante estudiante) {
        return dbExecutor.submit(() -> editarSync(estudiante));
    }

    /**
     * Borra un estudiante por su ID de forma asíncrona.
     * @param id Identificador del estudiante a borrar (String).
     * @return Future<Boolean> que indica si el borrado fue exitoso.
     */
    public Future<Boolean> borrarAsync(String id) {
        return dbExecutor.submit(() -> borrarSync(id));
    }

    // --- MÉTODOS SÍNCRONOS (Internal) ---

    /** [SÍNCRONO] Implementación interna para añadir. */
    private boolean aniadirSync(Modelo_Estudiante estudiante) throws SQLException {
        // Nota: Asumimos que ID en Oracle es un String/VARCHAR
        String sql = "INSERT INTO ESTUDIANTES (ID, NOMBRE, APELLIDOS, CURSO, PATRONUS) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = ConexionBD.conectarCasa(casa);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, String.valueOf(estudiante.getId())); // Usamos el ID del modelo
            ps.setString(2, estudiante.getNombre());
            ps.setString(3, estudiante.getApellidos());
            ps.setInt(4, estudiante.getCurso());
            ps.setString(5, estudiante.getPatronus());
            ps.executeUpdate();
            logger.info("Inserción exitosa en Oracle (Ravenclaw) para ID: {}", estudiante.getId());
            return true;

        } catch (SQLException e) {
            logger.error("Error SÍNCRONO al añadir estudiante en Oracle.", e);
            throw e;
        }
    }

    /** [SÍNCRONO] Implementación interna para editar. */
    private boolean editarSync(Modelo_Estudiante estudiante) throws SQLException {
        String sql = "UPDATE ESTUDIANTES SET NOMBRE = ?, APELLIDOS = ?, CURSO = ?, PATRONUS = ? WHERE ID = ?";
        try (Connection conn = ConexionBD.conectarCasa(casa);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, estudiante.getNombre());
            ps.setString(2, estudiante.getApellidos());
            ps.setInt(3, estudiante.getCurso());
            ps.setString(4, estudiante.getPatronus());
            ps.setString(5, String.valueOf(estudiante.getId())); // Usamos el ID para WHERE
            ps.executeUpdate();
            logger.info("Edición exitosa en Oracle (Ravenclaw) para ID: {}", estudiante.getId());
            return true;

        } catch (SQLException e) {
            logger.error("Error SÍNCRONO al editar estudiante en Oracle.", e);
            throw e;
        }
    }

    /** [SÍNCRONO] Implementación interna para borrar. */
    private boolean borrarSync(String id) throws SQLException {
        String sql = "DELETE FROM ESTUDIANTES WHERE ID = ?";
        try (Connection conn = ConexionBD.conectarCasa(casa);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id);
            ps.executeUpdate();
            logger.info("Borrado exitoso en Oracle (Ravenclaw) para ID: {}", id);
            return true;

        } catch (SQLException e) {
            logger.error("Error SÍNCRONO al borrar estudiante en Oracle.", e);
            throw e;
        }
    }

    /** Cierra el pool de hilos. */
    public static void shutdown() {
        if (!dbExecutor.isShutdown()) {
            dbExecutor.shutdown();
            logger.info("Pool de hilos de OracleDAO cerrado.");
        }
    }
}