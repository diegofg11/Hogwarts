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

    /**
     * Ejecuta la inserción de un nuevo estudiante de forma asíncrona.
     *
     * <p>
     * Envía la tarea de insertar el estudiante a un ejecutor ({@code dbExecutor})
     * utilizando el metodo {@code insertarEstudiante(e, false)}. La operación se realiza
     * en un hilo separado, y la sincronización bidireccional (si aplica) se activa.
     *
     * @param e El objeto {@link Modelo_Estudiante} a insertar.
     * @return Un objeto {@link Future} que representa el resultado pendiente de la inserción.
     * El resultado será {@code true} si la inserción fue exitosa, {@code false} en caso contrario.
     * @see #insertarEstudiante(Modelo_Estudiante, boolean)
     *
     * @author Gaizka
     */
    public Future<Boolean> insertarAsync(Modelo_Estudiante e) {
        return dbExecutor.submit(() -> insertarEstudiante(e,false));
    }

    /**
     * Ejecuta la edición de un estudiante existente de forma asíncrona.
     *
     * <p>
     * Envía la tarea de editar el estudiante a un ejecutor ({@code dbExecutor})
     * utilizando el metodo {@code editarEstudiante(e, false)}. La operación se realiza
     * en un hilo separado, y la sincronización bidireccional (si aplica) se activa.
     *
     * @param e El objeto {@link Modelo_Estudiante} con los datos actualizados.
     * @return Un objeto {@link Future} que representa el resultado pendiente de la edición.
     * El resultado será {@code true} si la edición fue exitosa, {@code false} en caso contrario.
     * @see #editarEstudiante(Modelo_Estudiante, boolean)
     *
     * @author Gaizka
     */
    public Future<Boolean> editarAsync(Modelo_Estudiante e) {
        return dbExecutor.submit(() -> editarEstudiante(e, false));
    }

    /**
     * Ejecuta la eliminación de un estudiante por ID de forma asíncrona.
     *
     * <p>
     * Envía la tarea de borrar el estudiante a un ejecutor ({@code dbExecutor})
     * utilizando el metodo {@code borrarEstudiante(id, false)}. La operación se realiza
     * en un hilo separado, y la sincronización bidireccional (si aplica) se activa.
     *
     * @param id El ID del estudiante a eliminar.
     * @return Un objeto {@link Future} que representa el resultado pendiente de la eliminación.
     * El resultado será {@code true} si la eliminación fue exitosa, {@code false} en caso contrario.
     * @see #borrarEstudiante(String, boolean)
     *
     * @author Gaizka
     */
    public Future<Boolean> borrarAsync(String id) {
        return dbExecutor.submit(() -> borrarEstudiante(id, false));
    }

    /**
     * Ejecuta la recuperación de todos los estudiantes de forma asíncrona.
     *
     * <p>
     * Envía la tarea de obtener todos los estudiantes a un ejecutor ({@code dbExecutor})
     * utilizando el metodo {@code obtenerTodos()}. La operación se realiza en un hilo separado.
     *
     * @return Un objeto {@link Future} que representa el resultado pendiente de la consulta.
     * El resultado será una {@code List} de {@link Modelo_Estudiante}.
     * @see #obtenerTodos()
     *
     * @author Gaizka
     */
    public Future<List<Modelo_Estudiante>> obtenerTodosAsync() {
        return dbExecutor.submit(this::obtenerTodos);
    }
    // ============================================================
    // === MÉTODOS SÍNCRONOS BASE =================================
    // ============================================================

    /**
     * Genera el siguiente ID numérico consecutivo disponible para la base de datos local
     * consultando el valor máximo actual de la columna {@code id} en la tabla {@code ESTUDIANTES}.
     *
     * <p>
     * El tipo de dato para la conversión del ID se ajusta dinámicamente:
     * <ul>
     * <li>Usa {@code NUMBER} para la casa "Ravenclaw" (asumiendo que usa Oracle).</li>
     * <li>Usa {@code INTEGER} para las demás casas (asumiendo bases de datos como MySQL/MariaDB).</li>
     * </ul>
     *
     * @param conn La conexión a la base de datos que se utilizará.
     * @return El siguiente ID numérico disponible. Retorna {@code 1} si la tabla está vacía.
     * @throws SQLException Si ocurre un error durante la ejecución de la consulta SQL.
     *
     * @author Gaizka
     */
    protected int generarNuevoIdLocal(Connection conn) throws SQLException {
        // Determina el tipo de dato de casting basado en la casa (para manejar diferencias de motor/tipo)
        String tipo = getCasa().equalsIgnoreCase("Ravenclaw") ? "NUMBER" : "INTEGER";

        // Consulta para obtener el ID máximo actual, casteando el string 'id' al tipo numérico apropiado
        String sql = "SELECT MAX(CAST(id AS " + tipo + ")) AS maximo FROM ESTUDIANTES";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                int ultimo = rs.getInt("maximo");
                // Si rs.wasNull() es true, significa que MAX() retornó NULL (tabla vacía), se empieza en 1.
                // Si no, se retorna el máximo + 1.
                return rs.wasNull() ? 1 : ultimo + 1;
            }
        }
        // Caso de seguridad, aunque debería ser capturado por rs.wasNull() si rs.next() es true.
        return 1;
    }

    /**
     * Inserta un nuevo estudiante en la base de datos local
     * y, opcionalmente, sincroniza la inserción con la base de datos central de Hogwarts.
     *
     * <p>
     * **Proceso:**
     * <ol>
     * <li>Verifica la validez del objeto {@code Modelo_Estudiante} usando {@code comprobarEstudiante(e)}.</li>
     * <li>Genera un nuevo ID numérico local (sin prefijo de casa) usando {@code generarNuevoIdNumerico(conn)} y lo asigna al estudiante.</li>
     * <li>Ejecuta la consulta SQL: {@code INSERT INTO ESTUDIANTES (id, nombre, apellidos, casa, curso, patronus) VALUES (?, ?, ?, ?, ?, ?)}.</li>
     * <li>Si la llamada no es de sincronización ({@code esSincronizacion = false}) y no estamos en la casa 'Hogwarts',
     * se realiza una **sincronización bidireccional** insertando el estudiante en la base de datos de Hogwarts
     * con un ID que incluye el prefijo de la casa (ej. "GR" + ID numérico).</li>
     * </ol>
     *
     * @param e El objeto {@link Modelo_Estudiante} conteniendo los datos del nuevo estudiante.
     * El campo {@code id} será sobrescrito con el nuevo ID generado.
     * @param esSincronizacion Booleano que indica si la llamada proviene de una sincronización externa
     * (ej. desde Hogwarts). Si es {@code true}, se omite la sincronización bidireccional para prevenir bucles.
     * @return {@code true} si el estudiante fue insertado y sincronizado exitosamente, {@code false}
     * si la comprobación inicial falla o si ocurre un error de {@code SQLException}.
     * @throws SQLException Si ocurre un error al acceder a la base de datos (manejado internamente con log).
     *
     * @author Gaizka
     */
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
                // Llama a Hogwarts con la bandera 'esSincronizacion' a true
                hogwartsDAO.insertarEstudiante(copia, true);
                logger.info("🔄 Sincronizado {} → Hogwarts (INSERT ID {}).", getCasa(), e.getId());
            }

            return true;

        } catch (SQLException ex) {
            logger.error("❌ Error al insertar en {}.", getCasa(), ex);
            return false;
        }
    }

    /**
     * Edita los detalles de un estudiante existente en la base de datos local
     * y, opcionalmente, sincroniza la actualización con la base de datos central de Hogwarts.
     *
     * <p>
     * **Pasos:**
     * <ol>
     * <li>Verifica la validez del objeto {@code Modelo_Estudiante} usando {@code comprobarEstudiante(e)}.</li>
     * <li>Ejecuta la consulta SQL: {@code UPDATE ESTUDIANTES SET nombre=?, apellidos=?, casa=?, curso=?, patronus=? WHERE id=?}.</li>
     * <li>Si la llamada no es de sincronización ({@code esSincronizacion = false}) y no estamos en la casa 'Hogwarts',
     * se realiza una **sincronización bidireccional** llamando a {@code editarEstudiante} en el DAO de Hogwarts,
     * utilizando un ID con prefijo de casa.</li>
     * </ol>
     *
     * @param e El objeto {@link Modelo_Estudiante} con los datos actualizados. El campo {@code id} debe coincidir con un estudiante existente.
     * @param esSincronizacion Booleano que indica si la llamada proviene de una sincronización externa
     * (ej. desde Hogwarts). Si es {@code true}, se omite la sincronización bidireccional para prevenir bucles.
     * @return {@code true} si el estudiante fue editado y sincronizado exitosamente, {@code false}
     * si la comprobación inicial falla o si ocurre un error de {@code SQLException}.
     * @throws SQLException Si ocurre un error al acceder a la base de datos (manejado internamente con log).
     *
     * @author Gaizka
     */
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

            // 🔁 Sincronización bidireccional (solo si no es una llamada de sincronización y no estamos en Hogwarts)
            if (!esSincronizacion && !getCasa().equalsIgnoreCase("Hogwarts")) {
                MariaDBDAO hogwartsDAO = new MariaDBDAO();
                // Genera el ID completo para Hogwarts
                String idConPrefijo = getPrefijoCasa() + e.getId();

                // Crea una copia del estudiante con el nuevo ID para enviarla a Hogwarts
                Modelo_Estudiante copia = new Modelo_Estudiante(
                        idConPrefijo, e.getNombre(), e.getApellidos(),
                        getCasa(), e.getCurso(), e.getPatronus()
                );

                // Llama a Hogwarts con la bandera 'esSincronizacion' a true
                hogwartsDAO.editarEstudiante(copia, true);
                logger.info("🔄 Sincronizado {} → Hogwarts (UPDATE ID {}).", getCasa(), e.getId());
            }

            return true;

        } catch (SQLException ex) {
            logger.error("❌ Error al editar en {}.", getCasa(), ex);
            return false;
        }
    }


    /**
     * Elimina un estudiante de la tabla de la base de datos local utilizando su ID
     * y, opcionalmente, sincroniza la eliminación con la base de datos central de Hogwarts.
     * <p>
     * La operación ejecuta la siguiente consulta SQL: {@code DELETE FROM ESTUDIANTES WHERE id=?}.
     *
     * @param id El ID del estudiante a eliminar. Este debe ser el ID local (sin prefijo de casa).
     * @param esSincronizacion Booleano que indica si la llamada proviene de una sincronización externa
     * (ej. desde Hogwarts).
     * Si es {@code true}, se omite la sincronización bidireccional para prevenir bucles.
     * @return {@code true} si se eliminó el estudiante exitosamente (incluyendo 0 filas afectadas
     * si la llamada fue de sincronización, pero {@code false} si fue llamada local y no existía),
     * {@code false} si ocurrió un error en la base de datos o si el estudiante no fue encontrado
     * en una llamada no sincronizada.
     * @throws SQLException Si ocurre un error al acceder a la base de datos (manejado internamente con log).
     *
     * @author Gaizka
     */
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

            // 🔁 Sincronización bidireccional (solo si no estamos en Hogwarts y no es una llamada de sincronización)
            if (!esSincronizacion && !getCasa().equalsIgnoreCase("Hogwarts")) {
                MariaDBDAO hogwartsDAO = new MariaDBDAO();

                String idConPrefijo = getPrefijoCasa() + id;
                // Llama a Hogwarts con la bandera 'esSincronizacion' a true
                hogwartsDAO.borrarEstudiante(idConPrefijo, true);
                logger.info("🔄 Sincronizado {} → Hogwarts (DELETE ID {}).", getCasa(), id);
            }

            return true;

        } catch (SQLException ex) {
            logger.error("❌ Error al borrar en {} (ID {}).", getCasa(), id, ex);
            return false;
        }
    }


    /**
     * Recupera una lista de todos los estudiantes de la base de datos.
     * <p>
     * Ejecuta una consulta SQL: {@code SELECT id, nombre, apellidos, casa, curso, patronus FROM ESTUDIANTES}
     * y mapea cada fila a un objeto {@code Modelo_Estudiante}.
     *
     * @return Una {@code List} de {@code Modelo_Estudiante} conteniendo todos los estudiantes.
     * Retorna una lista vacía si no hay estudiantes o si ocurre un error.
     * @throws SQLException Si ocurre un error al acceder a la base de datos (manejado internamente con log).
     *
     * @author Gaizka
     */
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

    /**
     * Obtiene el prefijo de dos letras asociado a la casa ({@code getCasa()}) para su uso,
     * por ejemplo, en la generación de IDs o códigos.
     *
     * @return El prefijo de dos letras para la casa:
     * <ul>
     * <li>"GR" para Gryffindor</li>
     * <li>"HF" para Hufflepuff</li>
     * <li>"RV" para Ravenclaw</li>
     * <li>"SL" para Slytherin</li>
     * <li>"HO" por defecto para cualquier otra casa o valor.</li>
     * </ul>
     *
     * @author Gaizka
     */
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


    /**
     * Cierra el pool de hilos asociado a la base de datos si aún no está cerrado.
     * <p>
     * Este metodo comprueba si el {@code dbExecutor} sigue activo y, en ese caso,
     * lo apaga de forma ordenada para liberar los recursos asociados.
     * </p>
     *
     * <p>También muestra un mensaje en consola indicando que el pool ha sido cerrado.</p>
     */
    public static void shutdown() {
        if (!dbExecutor.isShutdown()) {
            dbExecutor.shutdown();
            System.out.println("🧹 Pool de hilos cerrado.");
        }
    }

}
