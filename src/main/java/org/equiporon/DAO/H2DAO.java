package org.equiporon.DAO;

import org.equiporon.Conexion.ConexionBD;
import org.equiporon.Modelo.Estudiante;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class H2DAO { // Este DAO gestiona Estudiantes en la BDD H2

    private static final Logger logger = LoggerFactory.getLogger(H2DAO.class);
    private static final ExecutorService dbExecutor = Executors.newFixedThreadPool(5);
    private static final String CASA_ASIGNADA = "HUFFLEPUFF"; // Clave para ConexionBBDD

    // --- MÉTODOS ASÍNCRONOS (Public) ---
    public Future<List<Estudiante>> getAllEstudiantesAsync() {
        return dbExecutor.submit(this::getAllEstudiantesSync);
    }

    // --- MÉTODOS SÍNCRONOS (Internos) ---
    private List<Estudiante> getAllEstudiantesSync() throws SQLException {
        List<Estudiante> estudiantes = new ArrayList<>();
        final String SQL = "SELECT id, nombre, apellidos, casa, curso, patronus FROM Estudiante_H2 WHERE casa = ?";

        try (Connection conn = ConexionBBDD.getConnection(CASA_ASIGNADA);
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {

            pstmt.setString(1, CASA_ASIGNADA);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Estudiante e = new Estudiante(
                            rs.getInt("id"),
                            rs.getString("nombre"),
                            rs.getString("apellidos"),
                            rs.getString("casa"),
                            rs.getInt("curso"),
                            rs.getString("patronus")
                    );
                    estudiantes.add(e);
                }
            }
        } catch (SQLException e) {
            logger.error("ERROR H2DAO al obtener estudiantes de {}.", CASA_ASIGNADA, e);
            throw e;
        }
        return estudiantes;
    }

    public static void shutdown() {
        if (!dbExecutor.isShutdown()) {
            dbExecutor.shutdown();
            logger.info("El pool de hilos del H2DAO se ha cerrado.");
        }
    }
    // TODO: Implementar insertEstudianteSync, update, delete.
}