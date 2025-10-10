package org.equiporon.DAO;

import org.equiporon.Conexion.ConexionH2; // 👈 Conexión a H2
import org.equiporon.Modelo.Modelo_Estudiante;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data Access Object (DAO) para la casa Hufflepuff (Base de Datos H2).
 */
public class H2DAO {

    private static final Logger logger = LoggerFactory.getLogger(H2DAO.class);
    private static final ExecutorService dbExecutor = Executors.newFixedThreadPool(1); // Pool dedicado para Hufflepuff

    // --- MÉTODOS ASÍNCRONOS (Public) ---

    public Future<List<Modelo_Estudiante>> getAllEstudiantesAsync() {
        return dbExecutor.submit(this::getAllEstudiantesSync);
    }

    public Future<Modelo_Estudiante> insertEstudianteAsync(Modelo_Estudiante e) {
        return dbExecutor.submit(() -> insertEstudianteSync(e));
    }

    // --- MÉTODOS SÍNCRONOS (Internal) ---

    /** [SÍNCRONO] Implementación interna para leer todos los estudiantes de Hufflepuff. */
    private List<Modelo_Estudiante> getAllEstudiantesSync() throws SQLException {
        List<Modelo_Estudiante> estudiantes = new ArrayList<>();
        // Nota: Asumimos que la tabla se llama 'estudiante'
        final String SQL = "SELECT id, nombre, apellidos, casa, curso, patronus FROM estudiante ORDER BY id";
        logger.debug("Ejecutando consulta SÍNCRONA Hufflepuff: {}", SQL);

        try (Connection conn = ConexionH2.getConnection(); // 👈 USA CONEXIÓN H2
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
    private Modelo_Estudiante insertEstudianteSync(Modelo_Estudiante e) throws SQLException {
        final String SQL = "INSERT INTO estudiante (nombre, apellidos, casa, curso, patronus) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConexionH2.getConnection(); // 👈 USA CONEXIÓN H2
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
                        return e;
                    }
                }
            }
        } catch (SQLException ex) {
            logger.error("ERROR SÍNCRONO al insertar estudiante en Hufflepuff (H2).", ex);
            throw ex;
        }
        return null;
    }

    /** Cierra el pool de hilos. */
    public static void shutdown() {
        if (!dbExecutor.isShutdown()) {
            dbExecutor.shutdown();
            logger.info("Pool de hilos de HufflepuffDAO cerrado.");
        }
    }
}