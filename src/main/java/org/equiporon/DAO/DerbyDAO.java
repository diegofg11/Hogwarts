
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
 * DAO Asíncrono para gestionar operaciones CRUD en Apache Derby (Gryffindor)
 * sobre la tabla ESTUDIANTES, utilizando Modelo_Estudiante.
 *
 * @author Xiker, Igor(Modificado)
 * @version 2.0
 */
public class DerbyDAO {

    private static final Logger logger = LoggerFactory.getLogger(DerbyDAO.class);
    private static final ExecutorService dbExecutor = Executors.newFixedThreadPool(1); // Pool dedicado
    private final String casa = "Gryffindor"; // Casa Gryffindor para Derby
    //Sé instancia un objeto de la clase MariaDBDAO para poder llamar a los metodos de sincronizacion.
    private final MariaDBDAO mariaDBDAO = new MariaDBDAO();
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
     * @param id Identificador del estudiante a borrar.
     * @return Future<Boolean> que indica si el borrado fue exitoso.
     */
    public Future<Boolean> borrarAsync(String id) {
        return dbExecutor.submit(() -> borrarSync(id));
    }

    /**
     * Muestra todos los alumnos de la lista de forma asíncrona.
     * @return Future<List<Modelo_Estudiante>> con la lista de estudiantes.
     */
    public Future<List<Modelo_Estudiante>> getAllAsync() {
        return dbExecutor.submit(this::getAllSync);
    }

    // --- MÉTODOS SÍNCRONOS (Internal) ---

    /** [SÍNCRONO] Implementación interna para añadir. */
    private Boolean aniadirSync(Modelo_Estudiante e) throws SQLException {
        // Usamos ESTUDIANTES y lógica RETURN_GENERATED_KEYS
        String sql = "INSERT INTO APP.ESTUDIANTES (NOMBRE, APELLIDOS, CASA, CURSO, PATRONUS) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConexionBD.conectarCasa(casa);
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, e.getNombre());
            pstmt.setString(2, e.getApellidos());
            pstmt.setString(3, e.getCasa());
            pstmt.setInt(4, e.getCurso());
            pstmt.setString(5, e.getPatronus());



            if (pstmt.executeUpdate() > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        e.setId(rs.getString(1)); // Establecer el ID generado
                        String idConPrefijo = casa.substring(0, 1).toUpperCase() + e.getId();
                        // CORRECCIÓN: Log actualizado a Derby
                        logger.info("Inserción exitosa en Gryffindor (Derby) con ID: {}", e.getId());
                        //Llama al metodo sincronizarInsert de mariadbdao para que se sincronicen.

                        mariaDBDAO.sincronizarInsert(e,casa,idConPrefijo);
                        return true;
                    }
                }
            }
            return false;

        } catch (SQLException ex) {
            // CORRECCIÓN: Error Log actualizado a Derby
            logger.error("Error SÍNCRONO al añadir estudiante en Gryffindor (Derby).", ex);
            throw ex;
        }
    }

    /** [SÍNCRONO] Implementación interna para editar. */
    private boolean editarSync(Modelo_Estudiante estudiante) throws SQLException {
        String sql = "UPDATE APP.ESTUDIANTES SET NOMBRE = ?, APELLIDOS = ?, CURSO = ?, PATRONUS = ? WHERE ID = ?";
        try (Connection conn = ConexionBD.conectarCasa(casa);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, estudiante.getNombre());
            ps.setString(2, estudiante.getApellidos());
            ps.setInt(3, estudiante.getCurso());
            ps.setString(4, estudiante.getPatronus());
            ps.setInt(5, Integer.parseInt(estudiante.getId()));
            ps.executeUpdate();
            // CORRECCIÓN: Log actualizado a Derby
            logger.info("Edición exitosa en Gryffindor (Derby) para ID: {}", estudiante.getId());
            //Llama al metodo sincronizarUpdate de mariadbdao para que se sincronicen.
            String idConPrefijo = "G" + estudiante.getId();
            mariaDBDAO.sincronizarUpdate(estudiante,casa, idConPrefijo);
            return true;

        } catch (SQLException e) {
            // CORRECCIÓN: Error Log actualizado a Derby
            logger.error("Error SÍNCRONO al editar estudiante en Gryffindor (Derby).", e);
            throw e;
        }
    }

    /** [SÍNCRONO] Implementación interna para borrar. */
    private boolean borrarSync(String id) throws SQLException {
        String sql = "DELETE FROM APP.ESTUDIANTES WHERE ID = ?";
        try (Connection conn = ConexionBD.conectarCasa(casa);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, Integer.parseInt(id));
            ps.executeUpdate();
            // CORRECCIÓN: Log actualizado a Derby
            logger.info("Borrado exitoso en Gryffindor (Derby) para ID: {}", id);
            //Llama al metodo sincronizarDelete de mariadbdao para que se sincronicen.
            String idConPrefijo = casa.substring(0, 1).toUpperCase() + id;
            mariaDBDAO.sincronizarDelete(idConPrefijo,casa);
            return true;

        } catch (SQLException e) {
            // CORRECCIÓN: Error Log actualizado a Derby
            logger.error("Error SÍNCRONO al borrar estudiante en Gryffindor (Derby).", e);
            throw e;
        }
    }

    private List<Modelo_Estudiante> getAllSync() throws SQLException {
        List<Modelo_Estudiante> estudiantes = new ArrayList<>();
        // Usando ESTUDIANTES
        final String SQL = "SELECT id, nombre, apellidos, casa, curso, patronus FROM APP.ESTUDIANTES ORDER BY id";
        logger.debug("Ejecutando consulta SÍNCRONA Gryffindor: {}", SQL);

        try (Connection conn = ConexionBD.conectarCasa("Gryffindor");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL)) {

            while (rs.next()) {
                Modelo_Estudiante e = new Modelo_Estudiante(
                        rs.getString("id"),
                        rs.getString("nombre"),
                        rs.getString("apellidos"),
                        rs.getString("casa"),
                        rs.getInt("curso"),
                        rs.getString("patronus")
                );
                estudiantes.add(e);
            }
        } catch (SQLException e) {
            // CORRECCIÓN: Error Log actualizado a Derby
            logger.error("ERROR SÍNCRONO al obtener estudiantes de Gryffindor (Derby).", e);
            throw e;
        }
        return estudiantes;
    }

    /** Cierra el pool de hilos. */
    public static void shutdown() {
        if (!dbExecutor.isShutdown()) {
            dbExecutor.shutdown();
            logger.info("Pool de hilos de DerbyDAO cerrado.");
        }
    }
}