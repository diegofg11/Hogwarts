package org.equiporon.DAO;

import org.equiporon.Conexion.ConexionBD;
import org.equiporon.Modelo.Modelo_Estudiante;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
/**
 * DAO de la base de datos de Hogwarts en MariaDB
 *
 * Clase MariaDBDAO encargada de manejar la conexión y operaciones
 * con la base de datos MariaDB.
 *
 * Contiene métodos para insertar, obtener, actualizar y eliminar registros
 * de estudiantes.
 *
 * @author Diego,Unai, Gaizka, Igor
 */
public class MariaDBDAO extends BaseDAO {

    private static final Logger logger = LoggerFactory.getLogger(MariaDBDAO.class);
    /**
     * {@inheritDoc}
     *
     * Implementación del metodo abstracto para identificar la entidad
     * de la base de datos que maneja esta clase DAO.
     *
     * En este caso, devuelve la cadena literal "Hogwarts" ya que esta
     * clase {@code MariaDBDAO} gestiona la base de datos central de Hogwarts
     * almacenada en MariaDB.
     *
     * @return Siempre la cadena "Hogwarts".
     */

    @Override
    protected String getCasa() {
        return "Hogwarts";
    }
    /**
     * {@inheritDoc}
     *
     * Implementa el metodo abstracto para proporcionar una conexión activa
     * a la base de datos de Hogwarts.
     *
     * Utiliza la clase de utilidad estática {@code ConexionBD} para
     * obtener la conexión subyacente a la base de datos MariaDB.
     *
     * @return La {@code Connection} activa para interactuar con la base de datos MariaDB (Hogwarts).
     * @throws SQLException Si ocurre un error de acceso a la base de datos o el driver no puede cargar.
     */

    @Override
    protected Connection getConnection() throws SQLException {
        return ConexionBD.getConnection();
    }


    /**
     * Inserta un nuevo registro de estudiante en la tabla {@code ESTUDIANTES} de Hogwarts (MariaDB).
     *
     * Este metodo primero valida el objeto {@code Modelo_Estudiante} y luego determina
     * el **prefijo de casa** (GR, HF, RV, SL o HO) para generar un **nuevo ID único**
     * para el estudiante en el sistema central de Hogwarts.
     *
     * Tras la inserción exitosa, si la operación **no** proviene de una sincronización,
     * llama a {@code sincronizarConCasa} para propagar el nuevo registro a la base de datos
     * de la casa de procedencia (eliminando el prefijo de Hogwarts del ID para la casa).
     *
     * @param e El objeto {@code Modelo_Estudiante} con los datos a insertar. Su ID será actualizado.
     * @param esSincronizacion Booleano que indica si la operación fue iniciada por una sincronización desde una casa (true).
     * Si es {@code false}, se procede a sincronizar la inserción hacia la casa.
     * @return {@code true} si la inserción en Hogwarts fue exitosa, {@code false} si falla la validación o la operación SQL.
     */

    @Override
    public boolean insertarEstudiante(Modelo_Estudiante e, boolean esSincronizacion) {

        // 1. AÑADIR LA VALIDACIÓN QUE ESTABA EN BaseDAO
        if (!comprobarEstudiante(e)) {
            return false;
        }

        final String sql = "INSERT INTO ESTUDIANTES (id, nombre, apellidos, casa, curso, patronus) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Determinar prefijo según la casa
            String prefijo = switch (e.getCasa().toLowerCase()) {
                case "gryffindor" -> "GR";
                case "hufflepuff" -> "HF";
                case "ravenclaw" -> "RV";
                case "slytherin" -> "SL";
                default -> "HO";
            };

            // Generar ID nuevo en Hogwarts
            String nuevoId = generarNuevoIdHogwarts(conn, prefijo);
            e.setId(nuevoId);

            ps.setString(1, nuevoId);
            ps.setString(2, e.getNombre());
            ps.setString(3, e.getApellidos());
            ps.setString(4, e.getCasa());
            ps.setInt(5, e.getCurso());
            ps.setString(6, e.getPatronus());
            ps.executeUpdate();

            logger.info("✅ Insertado en Hogwarts con ID {}", e.getId());

            // 🔁 Sincronizar con la casa correspondiente (solo si no viene de la casa)
            if (!esSincronizacion) {
                sincronizarConCasa(e, "insert");
            }

            return true;

        } catch (SQLException ex) {
            logger.error("❌ Error insertando en Hogwarts.", ex);
            return false;
        }
    }

    /**
     * Edita un registro de estudiante existente en la base de datos de Hogwarts (MariaDB).
     *
     * Este metodo delega la lógica de actualización SQL al metodo {@code super.editarEstudiante()}
     * definido en {@code BaseDAO}.
     *
     * Si la actualización en Hogwarts es exitosa ({@code ok} es true) y la operación
     * no es una sincronización que viene de una casa ({@code esSincronizacion} es false),
     * entonces propaga el cambio a la base de datos de la casa correspondiente
     * llamando a {@code sincronizarConCasa} con el tipo "update".
     *
     * @param e El objeto {@code Modelo_Estudiante} conteniendo el ID y los nuevos datos del estudiante.
     * @param esSincronizacion Booleano que indica si la operación proviene de una sincronización (true)
     * o es una operación directa en Hogwarts (false).
     * @return {@code true} si la edición fue exitosa tanto en la base principal como, si aplica, en la sincronización,
     * {@code false} en caso contrario.
     */

    @Override
    public boolean editarEstudiante(Modelo_Estudiante e, boolean esSincronizacion) {
        boolean ok = super.editarEstudiante(e, esSincronizacion);
        if (ok && !esSincronizacion) {
            sincronizarConCasa(e, "update");
        }
        return ok;
    }

    /**
     * Elimina un registro de estudiante de la tabla {@code ESTUDIANTES} en la base de datos central de Hogwarts (MariaDB).
     *
     * Ejecuta una sentencia SQL {@code DELETE} utilizando el ID del estudiante (que incluye el prefijo de casa, ej: "GR101").
     *
     * Si la eliminación es exitosa y esta operación **no** ha sido desencadenada por una sincronización
     * desde una casa (es decir, {@code esSincronizacion} es {@code false}),
     * se llama a {@code sincronizarBorradoConCasa} para propagar la eliminación a la base de datos
     * de la casa correspondiente, deduciendo la casa a partir del prefijo del ID.
     *
     * @param id El ID único del estudiante en Hogwarts a eliminar (con prefijo de casa).
     * @param esSincronizacion Booleano que indica si la operación proviene de una sincronización (true)
     * o es una operación directa en Hogwarts (false).
     * @return {@code true} si el registro fue eliminado exitosamente, {@code false} si ocurre un error SQL.
     */

    @Override
    public boolean borrarEstudiante(String id, boolean esSincronizacion) {
        final String sql = "DELETE FROM ESTUDIANTES WHERE id=?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, id);
            ps.executeUpdate();
            logger.info("🗑️ Borrado en Hogwarts (ID {}).", id);

            // 🔁 Sincronizar hacia la casa solo si no es sincronización
            if (!esSincronizacion) {
                sincronizarBorradoConCasa(id);
            }

            return true;

        } catch (SQLException ex) {
            logger.error("❌ Error al borrar en Hogwarts.", ex);
            return false;
        }
    }

    /**
     * Propaga una operación (inserción o actualización) de estudiante realizada
     * en la base de datos central de Hogwarts hacia la base de datos de la casa correspondiente.
     *
     * **Pasos de la Sincronización:**
     * 1. Determina el {@code BaseDAO} de la casa (DerbyDAO, H2DAO, OracleDAO o HSQLDBDAO)
     * basándose en el valor de {@code e.getCasa()}.
     * 2. Crea una copia del objeto {@code Modelo_Estudiante}, crucialmente **eliminando**
     * el prefijo de Hogwarts (GR, HF, RV, SL) del ID para adaptarlo al formato de ID de la casa.
     * 3. Ejecuta la operación especificada por el parámetro {@code tipo} ("insert" o "update")
     * en el DAO de la casa, marcándola como una operación de sincronización ({@code true}).
     *
     * @param e El objeto {@code Modelo_Estudiante} con los datos y el ID de Hogwarts a sincronizar.
     * @param tipo El tipo de operación que se debe ejecutar en la casa ("insert" o "update"). El tipo "delete" debería usarse con {@code sincronizarBorradoConCasa}.
     */

    private void sincronizarConCasa(Modelo_Estudiante e, String tipo) {
        String casa = e.getCasa().trim().toLowerCase();

        BaseDAO daoCasa = switch (casa) {
            case "gryffindor" -> new DerbyDAO();
            case "hufflepuff" -> new H2DAO();
            case "ravenclaw" -> new OracleDAO();
            case "slytherin" -> new HSQLDBDAO();
            default -> null;
        };

        if (daoCasa == null) {
            logger.warn("⚠️ No se encontró DAO para la casa '{}'.", casa);
            return;
        }

        // ⚙️ Crear copia con ID sin prefijo (para la casa)
        String idNumerico = e.getId().replaceAll("^(GR|HF|RV|SL)", "");  // <-- quita prefijo
        Modelo_Estudiante copia = new Modelo_Estudiante(
                idNumerico,
                e.getNombre(),
                e.getApellidos(),
                e.getCasa(),
                e.getCurso(),
                e.getPatronus()
        );

        // 🧱 Ejecutar la operación en la casa
        switch (tipo) {
            case "insert" -> daoCasa.insertarEstudiante(copia, true);
            case "update" -> daoCasa.editarEstudiante(copia, true);
            case "delete" -> daoCasa.borrarEstudiante(copia.getId(), true);
        }

        logger.info("🔄 Hogwarts → {} ({} ID {}).", e.getCasa(), tipo, e.getId());
    }


    /**
     * Propaga una operación de borrado de estudiante desde la base de datos
     * central de Hogwarts hacia la base de datos de la casa correspondiente.
     *
     * **Mecanismo de Propagación:**
     * 1. El metodo extrae el **prefijo de dos letras** (ej: "GR", "HF") del ID de Hogwarts
     * para determinar la casa de destino (Gryffindor, Hufflepuff, etc.) y, por ende, el {@code BaseDAO} a utilizar.
     * 2. Una vez que se identifica el DAO de la casa, se extrae el **ID numérico** (sin prefijo)
     * para adaptarlo al formato de la casa.
     * 3. Finalmente, ejecuta el metodo {@code borrarEstudiante()} en el DAO de la casa,
     * marcando la operación como sincronización ({@code true}) para evitar ciclos de borrado.
     *
     * @param id El ID del estudiante en Hogwarts (con prefijo de casa, ej: "RV205") que ha sido borrado.
     */

    private void sincronizarBorradoConCasa(String id) {
        if (id.length() < 3) return;
        String prefijo = id.substring(0, 2).toUpperCase();
        String idSinPrefijo = id.substring(2);

        BaseDAO daoCasa = switch (prefijo) {
            case "GR" -> new DerbyDAO();
            case "HF" -> new H2DAO();
            case "RV" -> new OracleDAO();
            case "SL" -> new HSQLDBDAO();
            default -> null;
        };

        if (daoCasa != null) {
            daoCasa.borrarEstudiante(idSinPrefijo, true);
            logger.info("🔄 Hogwarts → {} (DELETE ID {}).", daoCasa.getCasa(), idSinPrefijo);
        } else {
            logger.warn("⚠️ No se pudo determinar casa para prefijo '{}'.", prefijo);
        }
    }

    /**
     * Genera un nuevo ID único y secuencial para un estudiante en la base de datos central de Hogwarts (MariaDB).
     *
     * El ID se construye combinando un prefijo de casa de dos letras (ej: "GR", "SL")
     * con el siguiente número entero consecutivo al ID numérico más alto encontrado
     * previamente para esa casa específica.
     *
     * **Mecanismo SQL:** La consulta busca el valor máximo de la parte numérica del ID
     * (obtenida tras eliminar el prefijo de dos caracteres) para los IDs que coinciden con
     * el patrón de prefijo proporcionado. Si no se encuentra un ID previo, comienza en '1'.
     *
     * @param conn La {@code Connection} activa a la base de datos MariaDB.
     * @param casaPrefijo El prefijo de dos letras de la casa (ej: "GR" para Gryffindor) para la cual se genera el ID.
     * @return El nuevo ID único generado para Hogwarts (ej: "GR102" o "SL1").
     * @throws SQLException Si ocurre un error durante la ejecución de la consulta SQL.
     *
     * @author Gaizka
     */

    private String generarNuevoIdHogwarts(Connection conn, String casaPrefijo) throws SQLException {
        String sql = "SELECT MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)) AS maximo FROM ESTUDIANTES WHERE id LIKE ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, casaPrefijo + "%");
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int ultimo = rs.getInt("maximo");
                    return casaPrefijo + (ultimo + 1);
                }
            }
        }
        return casaPrefijo + "1";
    }
}
