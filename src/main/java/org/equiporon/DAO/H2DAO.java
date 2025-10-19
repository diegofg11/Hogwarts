package org.equiporon.DAO;

import org.equiporon.Conexion.ConexionBD;
import org.equiporon.Modelo.Modelo_Estudiante;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data Access Object (DAO) para la casa Hufflepuff (Base de Datos H2).
 * NOTA: La conexión a Hufflepuff apunta a la URL de h2 según ConexionBD.
 * * @author  Igor
 * * @version 2.0
 * */

public class H2DAO {

    // CORRECCIÓN: Logger apunta a la clase correcta
    private static final Logger logger = LoggerFactory.getLogger(H2DAO.class);
    private static final ExecutorService dbExecutor = Executors.newFixedThreadPool(1); // Pool dedicado
    private final String casa = "Hufflepuff"; //  CASA CAMBIADA A Hufflepuff (H2)

    // --- MÉTODOS ASÍNCRONOS (Public) ---
    public Future<Boolean> aniadirAsync(Modelo_Estudiante estudiante) {
        return dbExecutor.submit(() -> aniadirSync(estudiante));
    }

    public Future<Boolean> editarAsync(Modelo_Estudiante estudiante) {
        return dbExecutor.submit(() -> editarSync(estudiante));
    }

    public Future<Boolean> borrarAsync(String id) {
        return dbExecutor.submit(() -> borrarSync(id));
    }

    public Future<List<Modelo_Estudiante>> getAllAsync() {
        return dbExecutor.submit(this::getAllSync);
    }

    // --- MÉTODOS SÍNCRONOS (Internal) ---

    /** [SÍNCRONO] Implementación interna para leer todos los estudiantes de Hufflepuff. */
    private List<Modelo_Estudiante> getAllSync() throws SQLException {
        List<Modelo_Estudiante> estudiantes = new ArrayList<>();
        // CORRECCIÓN: Usando ESTUDIANTES
        final String SQL = "SELECT id, nombre, apellidos, casa, curso, patronus FROM ESTUDIANTES ORDER BY id";
        logger.debug("Ejecutando consulta SÍNCRONA Hufflepuff: {}", SQL);

        try (Connection conn = ConexionBD.conectarCasa("Hufflepuff");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL)) {

            while (rs.next()) {
                Modelo_Estudiante e = new Modelo_Estudiante(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getString("apellidos"),
                        rs.getString("casa"),
                        rs.getInt("curso"),
                        rs.getString("patronus")
                );
                estudiantes.add(e);
            }
        } catch (SQLException e) {
            logger.error("ERROR SÍNCRONO al obtener estudiantes de Hufflepuff (H2).", e);
            throw e;
        }
        return estudiantes;
    }

    /** [SÍNCRONO] Implementación interna para insertar un nuevo estudiante en Hufflepuff. */
    private Boolean aniadirSync(Modelo_Estudiante e) throws SQLException {
        final String SQL = "INSERT INTO ESTUDIANTES (nombre, apellidos, casa, curso, patronus) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConexionBD.conectarCasa("Hufflepuff");
             PreparedStatement pstmt = conn.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, e.getNombre());
            pstmt.setString(2, e.getApellidos());
            pstmt.setString(3, e.getCasa());
            pstmt.setInt(4, e.getCurso());
            pstmt.setString(5, e.getPatronus());

            if (pstmt.executeUpdate() > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        e.setId(rs.getInt(1)); // Establecer el ID generado
                        logger.info("Inserción exitosa en Hufflepuff con ID: {}", e.getId());
                        return true;
                    }
                    return false;
                }
            }
        } catch (SQLException ex) {
            logger.error("ERROR SÍNCRONO al insertar ESTUDIANTE en Hufflepuff (H2).", ex);
            throw ex;
        }
        // CORRECCIÓN: Devolver false en lugar de null al final
        return false;
    }

    private boolean borrarSync(String id) throws SQLException {
        String sql = "DELETE FROM ESTUDIANTES WHERE ID = ?";
        try (Connection conn = ConexionBD.conectarCasa(casa);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id);
            ps.executeUpdate();
            // CORRECCIÓN: Log de Hufflepuff
            logger.info("Borrado exitoso en Hufflepuff (H2) para ID: {}", id);
            return true;

        } catch (SQLException e) {
            logger.error("Error SÍNCRONO al borrar estudiante en Hufflepuff (H2).", e);
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
            ps.setString(5, String.valueOf(estudiante.getId()));
            ps.executeUpdate();
            logger.info("Edición exitosa en Hufflepuff (H2) para ID: {}", estudiante.getId());
            return true;

        } catch (SQLException e) {
            logger.error("Error SÍNCRONO al editar estudiante en Hufflepuff(H2).", e);
            throw e;
        }
    }

    /** Cierra el pool de hilos. */
    public static void shutdown() {
        if (!dbExecutor.isShutdown()) {
            dbExecutor.shutdown();
            logger.info("Pool de hilos de HufflepuffDAO cerrado.");
        }
    }
}