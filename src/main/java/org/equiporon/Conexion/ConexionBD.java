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
    public static Connection getSQLiteConnection() {
        try {
            // 1️⃣ Cargar el driver de SQLite
            Class.forName("org.sqlite.JDBC");

            // 2️⃣ Ruta del archivo local (en carpeta data/)
            String url = "jdbc:sqlite:../data/hogwarts_backup.db";
            Connection conn = DriverManager.getConnection(url);

            // 3️⃣ Crear la tabla si no existe
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS ESTUDIANTES (
                    id TEXT PRIMARY KEY,
                    nombre TEXT,
                    apellidos TEXT,
                    casa TEXT,
                    curso INTEGER,
                    patronus TEXT
                )
            """);
            }

            System.out.println("✅ Conectado a SQLite (backup local de Hogwarts)");
            return conn;

        } catch (Exception e) {
            System.err.println("❌ Error conectando a SQLite: " + e.getMessage());
            return null;
        }
    }


}



