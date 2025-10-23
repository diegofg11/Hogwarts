package org.equiporon.DAO;

import org.equiporon.Conexion.ConexionBD;
import org.equiporon.Modelo.Modelo_Estudiante;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.equiporon.Utils.Alertas.mostrarError;

/**
 * Clase base genérica para los DAOs de Hogwarts y las Casas.
 * Gestiona operaciones CRUD básicas, sincronización bidireccional
 * y ejecución asíncrona.
 * Además, comprueba la funcionalidad antes de realizar cambios
 * @author Gaizka, Igor, Unai, Ruben
 */
public abstract class BaseDAO {

    protected final static Logger logger = LoggerFactory.getLogger(BaseDAO.class);

    /** Pool de hilos para ejecución asíncrona */
    protected static final ExecutorService dbExecutor =
            Executors.newFixedThreadPool(2);

    /** Devuelve el nombre de la casa (o Hogwarts). */
    protected abstract String getCasa();

    /** Devuelve la conexión a la base correspondiente. */
    protected abstract Connection getConnection() throws SQLException;

    // ============================================================
    // === MÉTODOS ASÍNCRONOS COMUNES =============================
    // ============================================================

    public Future<Boolean> insertarAsync(Modelo_Estudiante e) {
        return dbExecutor.submit(() -> insertarEstudiante(e,false));
    }

    public Future<Boolean> editarAsync(Modelo_Estudiante e) {
        return dbExecutor.submit(() -> editarEstudiante(e, false));
    }

    public Future<Boolean> borrarAsync(String id) {
        return dbExecutor.submit(() -> borrarEstudiante(id, false));
    }

    public Future<List<Modelo_Estudiante>> obtenerTodosAsync() {
        return dbExecutor.submit(this::obtenerTodos);
    }

    // ============================================================
    // === MÉTODOS SÍNCRONOS BASE =================================
    // ============================================================

    /** Genera el siguiente ID local según el motor (Oracle usa NUMBER). */
    protected int generarNuevoIdLocal(Connection conn) throws SQLException {
        String tipo = getCasa().equalsIgnoreCase("Ravenclaw") ? "NUMBER" : "INTEGER";
        String sql = "SELECT MAX(CAST(id AS " + tipo + ")) AS maximo FROM ESTUDIANTES";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                int ultimo = rs.getInt("maximo");
                return rs.wasNull() ? 1 : ultimo + 1;
            }
        }
        return 1;
    }

    /** Inserta estudiante local + sincronización Hogwarts */
    public boolean insertarEstudiante(Modelo_Estudiante e, boolean esSincronizacion) {

        if (!comprobarEstudiante(e)) {
            return false;
        }

        final String sql = "INSERT INTO ESTUDIANTES (id, nombre, apellidos, casa, curso, patronus) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // ⚙️ Solo generar número (sin prefijo)
            String nuevoId = generarNuevoIdNumerico(conn);
            e.setId(nuevoId);

            ps.setString(1, e.getId());
            ps.setString(2, e.getNombre());
            ps.setString(3, e.getApellidos());
            ps.setString(4, getCasa());
            ps.setInt(5, e.getCurso());
            ps.setString(6, e.getPatronus());
            ps.executeUpdate();

            logger.info("✅ Insertado en {} con ID {}", getCasa(), e.getId());

            // 🔁 Sincronizar hacia Hogwarts (MariaDB)
            if (!esSincronizacion && !getCasa().equalsIgnoreCase("Hogwarts")) {
                MariaDBDAO hogwartsDAO = new MariaDBDAO();

                // Prefijo para Hogwarts según la casa
                String prefijo = switch (getCasa().toLowerCase()) {
                    case "gryffindor" -> "GR";
                    case "hufflepuff" -> "HF";
                    case "ravenclaw" -> "RV";
                    case "slytherin" -> "SL";
                    default -> "HO";
                };

                String idConPrefijo = prefijo + e.getId();

                Modelo_Estudiante copia = new Modelo_Estudiante(
                        idConPrefijo, e.getNombre(), e.getApellidos(),
                        getCasa(), e.getCurso(), e.getPatronus()
                );

                hogwartsDAO.insertarEstudiante(copia, true);
                logger.info("🔄 Sincronizado {} → Hogwarts (INSERT ID {}).", getCasa(), e.getId());
            }

            return true;

        } catch (SQLException ex) {
            logger.error("❌ Error al insertar en {}.", getCasa(), ex);
            return false;
        }
    }


    /** Editar + sincronizar con Hogwarts */
    public boolean editarEstudiante(Modelo_Estudiante e, boolean esSincronizacion) {

        if (!comprobarEstudiante(e)) {
            return false;
        }

        final String sql = "UPDATE ESTUDIANTES SET nombre=?, apellidos=?, casa=?, curso=?, patronus=? WHERE id=?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, e.getNombre());
            ps.setString(2, e.getApellidos());
            ps.setString(3, e.getCasa());
            ps.setInt(4, e.getCurso());
            ps.setString(5, e.getPatronus());
            ps.setString(6, e.getId());
            ps.executeUpdate();

            logger.info("✏️ Editado en {} (ID {}).", getCasa(), e.getId());

            if (!esSincronizacion && !getCasa().equalsIgnoreCase("Hogwarts")) {
                MariaDBDAO hogwartsDAO = new MariaDBDAO();
                String idConPrefijo = getPrefijoCasa() + e.getId();
                Modelo_Estudiante copia = new Modelo_Estudiante(
                        idConPrefijo, e.getNombre(), e.getApellidos(),
                        getCasa(), e.getCurso(), e.getPatronus()
                );
                hogwartsDAO.editarEstudiante(copia, true);
                logger.info("🔄 Sincronizado {} → Hogwarts (UPDATE ID {}).", getCasa(), e.getId());
            }

            return true;

        } catch (SQLException ex) {
            logger.error("❌ Error al editar en {}.", getCasa(), ex);
            return false;
        }
    }


    /** Borra estudiante local + sincronización Hogwarts */
    public boolean borrarEstudiante(String id, boolean esSincronizacion) {
        final String sql = "DELETE FROM ESTUDIANTES WHERE id=?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id);
            int filas = ps.executeUpdate();

            if (filas == 0) {
                logger.warn("⚠️ No se encontró el ID {} en {} para borrar.", id, getCasa());
                return false;
            }

            logger.info("🗑️ Borrado en {} (ID {}).", getCasa(), id);

            // 🔁 Sincronización bidireccional (solo si no estamos en Hogwarts)
            if (!esSincronizacion && !getCasa().equalsIgnoreCase("Hogwarts")) {
                MariaDBDAO hogwartsDAO = new MariaDBDAO();

                String idConPrefijo = getPrefijoCasa() + id;
                hogwartsDAO.borrarEstudiante(idConPrefijo, true);
                logger.info("🔄 Sincronizado {} → Hogwarts (DELETE ID {}).", getCasa(), id);
            }

            return true;

        } catch (SQLException ex) {
            logger.error("❌ Error al borrar en {} (ID {}).", getCasa(), id, ex);
            return false;
        }
    }


    /** SELECT */
    public List<Modelo_Estudiante> obtenerTodos() {
        List<Modelo_Estudiante> lista = new ArrayList<>();
        final String sql = "SELECT id, nombre, apellidos, casa, curso, patronus FROM ESTUDIANTES";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                lista.add(new Modelo_Estudiante(
                        rs.getString("id"),
                        rs.getString("nombre"),
                        rs.getString("apellidos"),
                        rs.getString("casa"),
                        rs.getInt("curso"),
                        rs.getString("patronus")
                ));
            }

        } catch (SQLException ex) {
            logger.error("❌ Error al obtener estudiantes de {}.", getCasa(), ex);
        }
        return lista;
    }
    protected String getPrefijoCasa() {
        return switch (getCasa().toLowerCase()) {
            case "gryffindor" -> "GR";
            case "hufflepuff" -> "HF";
            case "ravenclaw" -> "RV";
            case "slytherin" -> "SL";
            default -> "HO";
        };
    }
    /**
     * Genera un nuevo ID con prefijo según la casa.
     * Ejemplo: GR1, HF2, RV3, SL1, HO4...
     */
    /**
     * Genera un nuevo ID con prefijo según la casa (compatible con todos los motores).
     */
    /**
     * Genera un nuevo ID numérico local (sin prefijo) para las casas.
     */
    /**
     * Genera un nuevo ID numérico (como texto) para la tabla ESTUDIANTES.
     * Compatible con bases que usan IDs tipo String.
     */
    protected String generarNuevoIdNumerico(Connection conn) throws SQLException {
        String sql = "SELECT id FROM ESTUDIANTES";
        int maximo = 0;

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String id = rs.getString("id");
                if (id == null) continue;

                // Extrae solo los dígitos del ID (ignora prefijos como GR, HF, etc.)
                String numeros = id.replaceAll("\\D+", ""); // elimina todo lo que no sea número

                if (!numeros.isEmpty()) {
                    try {
                        int valor = Integer.parseInt(numeros);
                        if (valor > maximo) maximo = valor;
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        // Devuelve el siguiente número (como texto)
        return String.valueOf(maximo + 1);
    }



    /**
     * Metodo para comprobar si los estudiantes son válidos, teniendo en cuenta diferentes factores:
     * <ul>
     *   <li>Que no haya campos vacíos.</li>
     *   <li>Que el curso esté entre 1 y 7.</li>
     *   <li>Que los campos de texto (nombre, apellidos y patronus) contengan solo letras y espacios.</li>
     *   <li>Que la casa sea una de las cuatro válidas (Gryffindor, Slytherin, Hufflepuff o Ravenclaw).</li>
     * </ul>
     *
     * Si se detecta un error, se muestra un mensaje de error y se registra en el log.
     *
     * @author Unai Zugaza, Ruben
     * @param e Objeto {@link Modelo_Estudiante} a comprobar.
     * @return {@code true} si el estudiante es válido, {@code false} en caso contrario.
     */
    public static boolean comprobarEstudiante(Modelo_Estudiante e) {

        if (e.getCasa().isEmpty() || e.getCasa() == null || e.getApellidos().isEmpty() || e.getNombre().isEmpty()
                || e.getCurso() == null || e.getPatronus().isEmpty()) {
            logger.error("No dejes campos vacíos");
            mostrarError("Error campos vacíos", "No dejes campos vacíos");
            return false;
        }

        if (e.getCurso() <= 0 || e.getCurso() > 7) {
            logger.error("Curso no válido");
            mostrarError("Error curso inválido", "Los cursos válidos son del 1 al 7");
            return false;
        }

        if (!comprobarStrings(e.getPatronus())) {
            logger.error("Patronus no válido");
            mostrarError("Error patronus inválido", "Los patronus válidos contienen solo letras y espacios");
            return false;
        }

        if (!comprobarStrings(e.getNombre())) {
            logger.error("Nombre no válido");
            mostrarError("Error nombre inválido", "Los nombres válidos contienen solo letras y espacios");
            return false;
        }

        if (!comprobarStrings(e.getApellidos())) {
            logger.error("Apellidos no válidos");
            mostrarError("Error apellidos inválidos", "Los apellidos válidos contienen solo letras y espacios");
            return false;
        }


        if (!(e.getCasa().equalsIgnoreCase("Gryffindor") ||
                e.getCasa().equalsIgnoreCase("Slytherin") ||
                e.getCasa().equalsIgnoreCase("Hogwarts") ||
                e.getCasa().equalsIgnoreCase("Hufflepuff") ||
                e.getCasa().equalsIgnoreCase("Ravenclaw"))) {

            logger.error("Casa no válida");
            mostrarError("Error casa inválida", "No has introducido la casa correctamente");
            return false;
        }

        return true;
    }

    /**
     * Metodo auxiliar para comprobar si una cadena contiene solo letras y espacios.
     *
     * @author Unai Zugaza, Ruben
     * @param str Cadena a comprobar.
     * @return {@code true} si la cadena contiene únicamente letras y al menos una de ellas,
     *         {@code false} si contiene números, caracteres especiales o está vacía.
     */
    private static boolean comprobarStrings(String str) {
        boolean valido = true;
        int letras = 0;

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);

            // Contar letras válidas
            if (Character.isLetter(c)) {
                letras++;
            }

            // Si no es letra ni espacio, no es válido
            if (!Character.isLetter(c) && c != ' ') {
                valido = false;
            }
        }

        return valido && letras > 0;
    }


    // ============================================================
    // === SHUTDOWN ===============================================
    // ============================================================
    public static void shutdown() {
        if (!dbExecutor.isShutdown()) {
            dbExecutor.shutdown();
            System.out.println("🧹 Pool de hilos cerrado.");
        }
    }
}
