package org.equiporon.Conexion;

import java.sql.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ConexionBD {

    private static final Logger logger = LoggerFactory.getLogger(ConexionBD.class);


    public static Connection conectarCasa(String casa) {
        String url, user, password;

        switch (casa) {
            case "Gryffindor" -> {
                url = Config.getGryffindorUrl();
                user = Config.getGryffindorUser();
                password = Config.getGryffindorPassword();
            }
            case "Ravenclaw" -> {
                url = Config.getRavenclawUrl();
                user = Config.getRavenclawUser();
                password = Config.getRavenclawPassword();
            }
            case "Slytherin" -> {
                url = Config.getSlytherinUrl();
                user = Config.getSlytherinUser();
                password = Config.getSlytherinPassword();
            }
            case "Hufflepuff" -> {
                url = Config.getHufflepuffUrl();
                user = Config.getHufflepuffUser();
                password = Config.getHufflepuffPassword();
            }
            case "Hogwarts" -> {
                url = Config.getMariaDBUrl();
                user = Config.getMariaDBUser();
                password = Config.getMariaDBPassword();
            }
            default -> {
                logger.info("Casa no válida: " + casa);
                return null;
            }
        }

        try {
            Connection conn = DriverManager.getConnection(url, user, password);

            // 👇 Este bloque soluciona el error de Derby (APP.ESTUDIANTES no encontrado)
            // imports:


// dentro de tu try tras crear la conn:
            logger.info("[Derby] URL usada: " + url + " user=" + user);

// Solo para Gryffindor/Derby:
            if ("Gryffindor".equalsIgnoreCase(casa)) {
                try (Statement st = conn.createStatement()) {
                    try (ResultSet rs = st.executeQuery("VALUES CURRENT_USER")) {
                        if (rs.next()) logger.info("[Derby] CURRENT_USER=" + rs.getString(1));
                    }
                    try (ResultSet rs = st.executeQuery("VALUES CURRENT SCHEMA")) {
                        if (rs.next()) logger.info("[Derby] CURRENT_SCHEMA=" + rs.getString(1));
                    }
                    try (ResultSet rs = st.executeQuery(
                            "SELECT TABLENAME FROM SYS.SYSTABLES WHERE TABLETYPE='T' AND UPPER(TABLENAME)='ESTUDIANTES'")) {
                        boolean exists = rs.next();
                        logger.info("[Derby] ¿Existe tabla ESTUDIANTES en metadata? " + exists);
                    }
                    // Probar acceso real:
                    try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM APP.ESTUDIANTES")) {
                        if (rs.next()) logger.info("[Derby] Filas en APP.ESTUDIANTES = " + rs.getInt(1));
                    }
                } catch (SQLException ex) {
                    logger.error("[Derby] Diagnóstico falló: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }


            logger.info("Conexión exitosa con " + casa);
            return conn;

        } catch (SQLException e) {
            logger.error("❌ Error al conectar con la base de datos de " + casa + ": " + e.getMessage());
            return null;
        }
    }

    public static Connection getConnection() {
        try {
            Connection conn = DriverManager.getConnection(
                    Config.getMariaDBUrl(),
                    Config.getMariaDBUser(),
                    Config.getMariaDBPassword()
            );
            logger.info("Conexión exitosa con MariaDB (Hogwarts)");
            return conn;
        } catch (SQLException e) {
            logger.error("❌ Error al conectar con MariaDB: " + e.getMessage());
            return null;
        }
    }
}



