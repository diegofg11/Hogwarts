package org.equiporon.DAO;

import org.equiporon.Conexion.ConexionHSQLDB; // 👈 Conexión a HSQLDB
import org.equiporon.Modelo.Modelo_Estudiante;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data Access Object (DAO) para la casa Slytherin (Base de Datos HSQLDB).
 */
public class HSQLDBDAO {

    private static final Logger logger = LoggerFactory.getLogger(HSQLDBDAO.class);
    private static final ExecutorService dbExecutor = Executors.newFixedThreadPool(1); // Pool dedicado para Slytherin

    // --- MÉTODOS ASÍNCRONOS (Public) ---
    public Future<List<Modelo_Estudiante>> getAllEstudiantesAsync() {
        return dbExecutor.submit(this::getAllEstudiantesSync);
    }
    public Future<Modelo_Estudiante> insertEstudianteAsync(Modelo_Estudiante e) {
        return dbExecutor.submit(() -> insertEstudianteSync(e));
    }

    // --- MÉTODOS SÍNCRONOS (Internal) ---

    /** [SÍNCRONO] Implementación interna para leer todos los estudiantes de Slytherin. */
    private List<Modelo_Estudiante> getAllEstudiantesSync() throws SQLException {
        List<Modelo_Estudiante> estudiantes = new ArrayList<>();
        final String SQL = "SELECT id, nombre, apellidos, casa, curso, patronus FROM estudiante ORDER BY id";

        try (Connection conn = ConexionHSQLDB.getConnection(); // 👈 USA CONEXIÓN HSQLDB
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
            logger.error("ERROR SÍNCRONO al obtener estudiantes de Slytherin (HSQLDB).", e);
            throw e;
        }
        return estudiantes;
    }

    /** [SÍNCRONO] Implementación interna para insertar un nuevo estudiante en Slytherin. */
    private Modelo_Estudiante insertEstudianteSync(Modelo_Estudiante e) throws SQLException {
        final String SQL = "INSERT INTO estudiante (nombre, apellidos, casa, curso, patronus) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConexionHSQLDB.getConnection(); // 👈 USA CONEXIÓN HSQLDB
             PreparedStatement pstmt = conn.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, e.getNombre());
            pstmt.setString(2, e.getApellidos());
            pstmt.setString(3, e.getCasa());
            pstmt.setInt(4, e.getCurso());
            pstmt.setString(5, e.getPatronus());

            if (pstmt.executeUpdate() > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        e.setId(rs.getInt(1));
                        logger.info("Inserción exitosa en Slytherin con ID: {}", e.getId());
                        return e;
                    }
                }
            }
        } catch (SQLException ex) {
            logger.error("ERROR SÍNCRONO al insertar estudiante en Slytherin (HSQLDB).", ex);
            throw ex;
        }
        return null;
    }

    /** Cierra el pool de hilos. */
    public static void shutdown() {
        if (!dbExecutor.isShutdown()) {
            dbExecutor.shutdown();
            logger.info("Pool de hilos de SlytherinDAO cerrado.");
        }
    }
}