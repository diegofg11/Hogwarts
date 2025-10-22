package org.equiporon.DAO;

import org.equiporon.Conexion.ConexionBD;
import org.equiporon.Modelo.Modelo_Estudiante;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MariaDBDAO extends BaseDAO {

    private static final Logger logger = LoggerFactory.getLogger(MariaDBDAO.class);

    @Override
    protected String getCasa() {
        return "Hogwarts";
    }

    @Override
    protected Connection getConnection() throws SQLException {
        return ConexionBD.getConnection();
    }

    // --------------------------
    // 🔹 INSERTAR
    // --------------------------
    @Override
    public boolean insertarEstudiante(Modelo_Estudiante e, boolean esSincronizacion) {
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

    // --------------------------
    // 🔹 EDITAR
    // --------------------------
    @Override
    public boolean editarEstudiante(Modelo_Estudiante e, boolean esSincronizacion) {
        boolean ok = super.editarEstudiante(e, esSincronizacion);
        if (ok && !esSincronizacion) {
            sincronizarConCasa(e, "update");
        }
        return ok;
    }

    // --------------------------
    // 🔹 BORRAR
    // --------------------------
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

    // --------------------------
    // 🔹 SINCRONIZAR HACIA LAS CASAS
    // --------------------------
    private void sincronizarConCasa(Modelo_Estudiante e, String tipo) {
        String casa = e.getCasa().toLowerCase();
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

        // ⚙️ Quitar el prefijo antes de sincronizar con la casa
        if (e.getId().length() > 2 && e.getId().matches("^[A-Z]{2}\\d+$")) {
            e.setId(e.getId().substring(2));
        }

        switch (tipo) {
            case "insert" -> daoCasa.insertarEstudiante(e, true);
            case "update" -> daoCasa.editarEstudiante(e, true);
            case "delete" -> daoCasa.borrarEstudiante(e.getId(), true);
        }
        SQLiteDAO sqlite = new SQLiteDAO();
        sqlite.sincronizarDesdeHogwarts(e, tipo);
        logger.info("🔄 Hogwarts → {} ({} ID {}).", casa, tipo, e.getId());
    }

    /**
     * 🔁 Sincroniza un borrado desde Hogwarts hacia la casa correspondiente.
     * Deducción de la casa a partir del prefijo del ID (GR, HF, RV, SL)
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

    // --------------------------
    // 🔹 GENERAR NUEVO ID PARA HOGWARTS
    // --------------------------
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
