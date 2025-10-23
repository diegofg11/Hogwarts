package org.equiporon.DAO;

import org.equiporon.Conexion.ConexionBD;
import org.equiporon.Modelo.Modelo_Estudiante;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
/**
 *
 * DAO de la base de datos de Hogwarts en local con SQLite
 *
 * Clase SQLiteDAO encargada de manejar la conexión y operaciones
 * con la base de datos SQLite.
 *
 * Incluye métodos para crear la tabla, insertar, obtener,
 * actualizar y eliminar registros de estudiantes.
 *
 * @author Diego, Unai, Gaizka
 */
public class SQLiteDAO extends BaseDAO {

    private static final Logger logger = LoggerFactory.getLogger(SQLiteDAO.class);

    @Override
    protected String getCasa() {
        return "Backup";
    }

    @Override
    protected Connection getConnection() throws SQLException {
        return ConexionBD.getSQLiteConnection();
    }

    /** 🔁 Sincroniza los datos de Hogwarts a SQLite */
    public boolean sincronizarDesdeHogwarts(Modelo_Estudiante e, String tipo) {
        try (Connection conn = getConnection()) {
            if (conn == null) {
                logger.error("❌ No hay conexión a SQLite.");
                return false;
            }

            switch (tipo.toLowerCase()) {
                case "insert" -> insertarBackup(conn, e);
                case "update" -> editarBackup(conn, e);
                case "delete" -> borrarBackup(conn, e.getId());
            }
            return true;
        } catch (Exception ex) {
            logger.error("⚠️ Error sincronizando con SQLite.", ex);
            return false;
        }
    }

    private void insertarBackup(Connection conn, Modelo_Estudiante e) throws SQLException {
        String sql = "INSERT INTO ESTUDIANTES (id, nombre, apellidos, casa, curso, patronus) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, e.getId());
            ps.setString(2, e.getNombre());
            ps.setString(3, e.getApellidos());
            ps.setString(4, e.getCasa());
            ps.setInt(5, e.getCurso());
            ps.setString(6, e.getPatronus());
            ps.executeUpdate();
        }
        logger.info("💾 Copiado a SQLite (ID {}).", e.getId());
    }

    private void editarBackup(Connection conn, Modelo_Estudiante e) throws SQLException {
        String sql = "UPDATE ESTUDIANTES SET nombre=?, apellidos=?, casa=?, curso=?, patronus=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, e.getNombre());
            ps.setString(2, e.getApellidos());
            ps.setString(3, e.getCasa());
            ps.setInt(4, e.getCurso());
            ps.setString(5, e.getPatronus());
            ps.setString(6, e.getId());
            ps.executeUpdate();
        }
        logger.info("✏️ Actualizado en SQLite (ID {}).", e.getId());
    }

    private void borrarBackup(Connection conn, String id) throws SQLException {
        String sql = "DELETE FROM ESTUDIANTES WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.executeUpdate();
        }
        logger.info("🗑️ Borrado de SQLite (ID {}).", id);
    }
    public void hacerBackupCompleto() {
        try (Connection connSqlite = getConnection();
             Connection connMaria = ConexionBD.getConnection()) {

            if (connSqlite == null || connMaria == null) return;

            // 1️⃣ Vaciar SQLite
            try (PreparedStatement del = connSqlite.prepareStatement("DELETE FROM ESTUDIANTES")) {
                del.executeUpdate();
            }

            // 2️⃣ Copiar todos los registros desde MariaDB a SQLite
            String select = "SELECT * FROM ESTUDIANTES";
            try (PreparedStatement ps = connMaria.prepareStatement(select);
                 ResultSet rs = ps.executeQuery()) {

                String insert = "INSERT INTO ESTUDIANTES (id, nombre, apellidos, casa, curso, patronus) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ins = connSqlite.prepareStatement(insert)) {
                    while (rs.next()) {
                        ins.setString(1, rs.getString("id"));
                        ins.setString(2, rs.getString("nombre"));
                        ins.setString(3, rs.getString("apellidos"));
                        ins.setString(4, rs.getString("casa"));
                        ins.setInt(5, rs.getInt("curso"));
                        ins.setString(6, rs.getString("patronus"));
                        ins.addBatch();
                    }
                    ins.executeBatch();
                }
            }
            logger.info("💾 Backup completo realizado en SQLite.");
        } catch (Exception e) {
            logger.error("⚠️ Error haciendo backup en SQLite.", e);
        }
    }
    public void restaurarBackupEnHogwarts(String casaARevertir) {
        try (Connection connSqlite = getConnection()) {
            if (connSqlite == null) {
                logger.error("❌ No hay conexión a SQLite.");
                return;
            }

            // Recuperamos todos los estudiantes del backup
            String select = "SELECT * FROM ESTUDIANTES";
            try (PreparedStatement ps = connSqlite.prepareStatement(select);
                 ResultSet rs = ps.executeQuery()) {

                java.util.List<Modelo_Estudiante> lista = new java.util.ArrayList<>();
                while (rs.next()) {
                    Modelo_Estudiante e = new Modelo_Estudiante(
                            rs.getString("id"),
                            rs.getString("nombre"),
                            rs.getString("apellidos"),
                            rs.getString("casa"),
                            rs.getInt("curso"),
                            rs.getString("patronus")
                    );
                    lista.add(e);
                }

                // 🧩 Restaurar Hogwarts siempre
                restaurarCasa("Hogwarts", ConexionBD.getConnection(), lista);

                // 🧩 Si se restauró desde Hogwarts → sincronizar TODAS las casas
                if (casaARevertir.equalsIgnoreCase("Hogwarts")) {
                    restaurarCasa("Gryffindor", ConexionBD.conectarCasa("Gryffindor"), lista);
                    restaurarCasa("Hufflepuff", ConexionBD.conectarCasa("Hufflepuff"), lista);
                    restaurarCasa("Slytherin", ConexionBD.conectarCasa("Slytherin"), lista);
                    restaurarCasa("Ravenclaw", ConexionBD.conectarCasa("Ravenclaw"), lista);
                    logger.info("♻️ Restauración completa (Hogwarts + todas las casas) desde SQLite.");
                } else {
                    // 🧩 Si se restauró desde una casa → solo esa
                    switch (casaARevertir.toLowerCase()) {
                        case "gryffindor" -> restaurarCasa("Gryffindor", ConexionBD.conectarCasa("Gryffindor"), lista);
                        case "hufflepuff" -> restaurarCasa("Hufflepuff", ConexionBD.conectarCasa("Hufflepuff"), lista);
                        case "slytherin" -> restaurarCasa("Slytherin", ConexionBD.conectarCasa("Slytherin"), lista);
                        case "ravenclaw" -> restaurarCasa("Ravenclaw", ConexionBD.conectarCasa("Ravenclaw"), lista);
                    }
                    logger.info("♻️ Restauración parcial ({}) desde SQLite.", casaARevertir);
                }

            }
        } catch (Exception e) {
            logger.error("⚠️ Error restaurando desde SQLite.", e);
        }
    }


    /**
     * 💾 Realiza un backup instantáneo del estado actual de Hogwarts (MariaDB)
     * justo antes de una operación, para poder deshacer siempre.
     */
    public void hacerBackupInstantaneo() {
        try (Connection connSqlite = getConnection();
             Connection connMaria = ConexionBD.getConnection()) {

            if (connSqlite == null || connMaria == null) return;

            // 1️⃣ Vaciar SQLite
            try (PreparedStatement del = connSqlite.prepareStatement("DELETE FROM ESTUDIANTES")) {
                del.executeUpdate();
            }

            // 2️⃣ Copiar todos los registros actuales desde MariaDB a SQLite
            String select = "SELECT * FROM ESTUDIANTES";
            try (PreparedStatement ps = connMaria.prepareStatement(select);
                 ResultSet rs = ps.executeQuery()) {

                String insert = "INSERT INTO ESTUDIANTES (id, nombre, apellidos, casa, curso, patronus) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ins = connSqlite.prepareStatement(insert)) {
                    while (rs.next()) {
                        ins.setString(1, rs.getString("id"));
                        ins.setString(2, rs.getString("nombre"));
                        ins.setString(3, rs.getString("apellidos"));
                        ins.setString(4, rs.getString("casa"));
                        ins.setInt(5, rs.getInt("curso"));
                        ins.setString(6, rs.getString("patronus"));
                        ins.addBatch();
                    }
                    ins.executeBatch();
                }
            }

            logger.info("💾 Backup instantáneo hecho antes de la operación.");
        } catch (Exception e) {
            logger.error("⚠️ Error haciendo backup instantáneo.", e);
        }
    }


    /**
     * Restaura una casa específica desde la lista de estudiantes del backup.
     */
    /**
     * Restaura una casa específica desde la lista de estudiantes del backup,
     * insertando solo los que pertenecen a esa casa.
     */
    private void restaurarCasa(String nombreCasa, Connection conn, java.util.List<Modelo_Estudiante> lista) {
        if (conn == null) {
            logger.warn("⚠️ No hay conexión para {}", nombreCasa);
            return;
        }

        try {
            // 🔸 1. Vaciar tabla
            try (PreparedStatement del = conn.prepareStatement("DELETE FROM ESTUDIANTES")) {
                del.executeUpdate();
            }

            // 🔸 2. Insertar solo los estudiantes de esa casa
            String insert = "INSERT INTO ESTUDIANTES (id, nombre, apellidos, casa, curso, patronus) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ins = conn.prepareStatement(insert)) {

                for (Modelo_Estudiante e : lista) {
                    boolean esMariaDB = nombreCasa.equalsIgnoreCase("Hogwarts");
                    boolean coincideCasa =
                            esMariaDB ||
                                    (nombreCasa.equalsIgnoreCase("Gryffindor") && e.getCasa().equalsIgnoreCase("Gryffindor")) ||
                                    (nombreCasa.equalsIgnoreCase("Hufflepuff") && e.getCasa().equalsIgnoreCase("Hufflepuff")) ||
                                    (nombreCasa.equalsIgnoreCase("Slytherin") && e.getCasa().equalsIgnoreCase("Slytherin")) ||
                                    (nombreCasa.equalsIgnoreCase("Ravenclaw") && e.getCasa().equalsIgnoreCase("Ravenclaw"));

                    if (!coincideCasa) continue;

                    // 🧩 Ajustar ID solo si no es Hogwarts
                    String id = e.getId();
                    if (!esMariaDB) id = id.replaceAll("^(GR|HF|RV|SL)", "");

                    ins.setString(1, id);
                    ins.setString(2, e.getNombre());
                    ins.setString(3, e.getApellidos());
                    ins.setString(4, e.getCasa());
                    ins.setInt(5, e.getCurso());
                    ins.setString(6, e.getPatronus());
                    ins.addBatch();
                }

                ins.executeBatch();
            }

            logger.info("✅ Restaurada la casa {} desde SQLite.", nombreCasa);

        } catch (Exception ex) {
            logger.error("❌ Error restaurando casa {} desde SQLite", nombreCasa, ex);
        }
    }







}
