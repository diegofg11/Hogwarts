
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
 * Data Access Object (DAO) Asíncrono para gestionar operaciones CRUD en HSQLDB (Slytherin)
 * sobre la tabla ESTUDIANTES, utilizando Modelo_Estudiante.
 *
 * @author Igor
 * @version 2.0
 */
public class HSQLDBDAO {

    private static final Logger logger = LoggerFactory.getLogger(HSQLDBDAO.class);
    private static final ExecutorService dbExecutor = Executors.newFixedThreadPool(1); // Pool dedicado para Slytherin
    private final String casa = "Slytherin";
    //Sé instancia un objeto de mariaDBDAO para llamar a los metodos de sincronizacion.
    private final MariaDBDAO mariaDBDAO = new MariaDBDAO();

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

    /** [SÍNCRONO] Implementación interna para leer todos los estudiantes de Slytherin. */
    private List<Modelo_Estudiante> getAllSync() throws SQLException {
        List<Modelo_Estudiante> estudiantes = new ArrayList<>();
        // Usando ESTUDIANTES
        final String SQL = "SELECT id, nombre, apellidos, casa, curso, patronus FROM ESTUDIANTES ORDER BY id";
        logger.debug("Ejecutando consulta SÍNCRONA Slytherin: {}", SQL);

        try (Connection conn = ConexionBD.conectarCasa(casa);
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
            logger.error("ERROR SÍNCRONO al obtener estudiantes de Slytherin (HSQLDB).", e);
            throw e;
        }
        return estudiantes;
    }

    /** [SÍNCRONO] Implementación interna para insertar un nuevo estudiante en Slytherin. */
    private Boolean aniadirSync(Modelo_Estudiante e) throws SQLException {
        // Usando ESTUDIANTES y devolviendo Boolean (consistente)
        final String SQL = "INSERT INTO ESTUDIANTES (nombre, apellidos, casa, curso, patronus) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConexionBD.conectarCasa(casa);
             PreparedStatement pstmt = conn.prepareStatement(SQL, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, e.getNombre());
            pstmt.setString(2, e.getApellidos());
            pstmt.setString(3, e.getCasa());
            pstmt.setInt(4, e.getCurso());
            pstmt.setString(5, e.getPatronus());



            if (pstmt.executeUpdate() > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        e.setId(rs.getString(1));

                        String idConPrefijo = casa.substring(0, 1).toUpperCase() + e.getId();

                        logger.info("Inserción exitosa en Slytherin con ID: {}", e.getId());
                        //Llama al metodo sincronizarInsert de mariadbdao para que se sincronicen.
                        mariaDBDAO.sincronizarInsert(e,casa,idConPrefijo);
                        return true;
                    }
                }
            }
            return false;
        } catch (SQLException ex) {
            logger.error("ERROR SÍNCRONO al insertar estudiante en Slytherin (HSQLDB).", ex);
            throw ex;
        }
    }

    /** [SÍNCRONO] Implementación interna para editar. */
    private boolean editarSync(Modelo_Estudiante estudiante) throws SQLException {
        // Usando ESTUDIANTES
        String sql = "UPDATE ESTUDIANTES SET NOMBRE = ?, APELLIDOS = ?, CURSO = ?, PATRONUS = ? WHERE ID = ?";
        try (Connection conn = ConexionBD.conectarCasa(casa);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, estudiante.getNombre());
            ps.setString(2, estudiante.getApellidos());
            ps.setInt(3, estudiante.getCurso());
            ps.setString(4, estudiante.getPatronus());
            ps.setInt(5, Integer.parseInt(estudiante.getId()));
            ps.executeUpdate();
            //Llama al metodo sincronizarUpdate de mariadbdao para que se sincronicen.
            String idConPrefijo = casa.substring(0, 1).toUpperCase() + estudiante.getId();
            mariaDBDAO.sincronizarUpdate(estudiante,casa,idConPrefijo);
            logger.info("Edición exitosa en Slytherin (HSQLDB) para ID: {}", estudiante.getId());

            return true;

        } catch (SQLException e) {
            logger.error("Error SÍNCRONO al editar estudiante en Slytherin (HSQLDB).", e);
            throw e;
        }
    }

    /** [SÍNCRONO] Implementación interna para borrar. */
    private boolean borrarSync(String id) throws SQLException {
        // Usando ESTUDIANTES
        String sql = "DELETE FROM ESTUDIANTES WHERE ID = ?";
        try (Connection conn = ConexionBD.conectarCasa(casa);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, Integer.parseInt(id));
            ps.executeUpdate();
            logger.info("Borrado exitoso en Slytherin (HSQLDB) para ID: {}", id);
            //Llama al metodo sincronizarUpdate de mariadbdao para que se sincronicen.
            String idConPrefijo = casa.substring(0, 1).toUpperCase() + id;
            mariaDBDAO.sincronizarDelete(idConPrefijo,casa);
            return true;

        } catch (SQLException e) {
            logger.error("Error SÍNCRONO al borrar estudiante en Slytherin (HSQLDB).", e);
            throw e;
        }
    }

    /** Cierra el pool de hilos. */
    public static void shutdown() {
        if (!dbExecutor.isShutdown()) {
            dbExecutor.shutdown();
            logger.info("Pool de hilos de SlytherinDAO cerrado.");
        }
    }
}