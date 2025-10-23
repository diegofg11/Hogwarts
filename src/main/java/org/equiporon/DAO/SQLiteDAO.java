package org.equiporon.DAO;

import org.equiporon.Conexion.ConexionBD;
import org.equiporon.Modelo.Modelo_Estudiante;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;

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
    public void restaurarBackupEnHogwarts() {
        try (Connection connSqlite = getConnection();
             Connection connMaria = ConexionBD.getConnection()) {

            if (connSqlite == null || connMaria == null) return;

            // 1️⃣ Borrar Hogwarts
            try (PreparedStatement del = connMaria.prepareStatement("DELETE FROM ESTUDIANTES")) {
                del.executeUpdate();
            }

            // 2️⃣ Copiar desde SQLite
            String select = "SELECT * FROM ESTUDIANTES";
            try (PreparedStatement ps = connSqlite.prepareStatement(select);
                 ResultSet rs = ps.executeQuery()) {

                String insert = "INSERT INTO ESTUDIANTES (id, nombre, apellidos, casa, curso, patronus) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement ins = connMaria.prepareStatement(insert)) {
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
            logger.info("♻️ Hogwarts restaurado desde SQLite.");
        } catch (Exception e) {
            logger.error("⚠️ Error restaurando Hogwarts desde backup SQLite.", e);
        }
    }


}
