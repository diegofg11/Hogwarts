package org.equiporon.DAO;

import org.equiporon.Conexion.ConexionBD;
import org.equiporon.Modelo.Estudiante;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
// ... otras importaciones ...

public class HSQLDBDAO { // Este DAO gestiona Estudiantes en la BDD HSQLDB

    private static final Logger logger = LoggerFactory.getLogger(HSQLDBDAO.class);
    private static final ExecutorService dbExecutor = Executors.newFixedThreadPool(5);
    private static final String CASA_ASIGNADA = "SLYTHERIN"; // Clave para ConexionBBDD

    // --- MÉTODOS ASÍNCRONOS (Public) ---
    public Future<List<Estudiante>> getAllEstudiantesAsync() {
        return dbExecutor.submit(this::getAllEstudiantesSync);
    }

    // --- MÉTODOS SÍNCRONOS (Internos) ---
    private List<Estudiante> getAllEstudiantesSync() throws SQLException {
        // ... (Lógica de acceso a datos similar a H2DAO) ...
        // La diferencia principal será el código de conexión llamado en ConexionBBDD:
        try (Connection conn = ConexionBBDD.getConnection(CASA_ASIGNADA);
             // ...
        ) {
            // ...
        } catch (SQLException e) {
            logger.error("ERROR HSQLDBDAO al obtener estudiantes de {}.", CASA_ASIGNADA, e);
            throw e;
        }
        return new ArrayList<>();
    }

    public static void shutdown() {
        if (!dbExecutor.isShutdown()) {
            dbExecutor.shutdown();
            logger.info("El pool de hilos del HSQLDBDAO se ha cerrado.");
        }
    }
    // TODO: Implementar insertEstudianteSync, update, delete.
}