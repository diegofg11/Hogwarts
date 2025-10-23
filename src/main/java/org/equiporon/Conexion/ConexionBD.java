package org.equiporon.Conexion;

import java.sql.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clase de utilidad para gestionar y centralizar la creación de conexiones
 * a las diferentes bases de datos del proyecto.
 * <p>
 * Proporciona métodos estáticos para obtener un objeto {@link java.sql.Connection}
 * para la base de datos principal (MariaDB para "Hogwarts"), las bases de datos
 * de las casas (que según el código, usan configuraciones separadas) y
 * una base de datos local de respaldo (SQLite).
 * <p>
 * Depende de la clase {@link Config} para obtener las credenciales de conexión
 * (URL, usuario, contraseña) y utiliza SLF4J ({@link Logger}) para el
 * registro de eventos y errores de conexión.
 *
 * @author Gaizka, Unai, Diego, Ruben
 */
public class ConexionBD {

    private static final Logger logger = LoggerFactory.getLogger(ConexionBD.class);

    /**
     * Establece una conexión con la base de datos correspondiente a una "casa" específica.
     * <p>
     * Utiliza un <code>switch</code> para determinar las credenciales correctas
     * (obtenidas de {@link Config}) según el nombre de la casa proporcionado.
     * El caso "Hogwarts" se conecta a la base de datos MariaDB principal.
     * Los demás casos (Gryffindor, Ravenclaw, etc.) se conectan a sus respectivas
     * bases de datos.
     *
     * @param casa El nombre de la casa (ej. "Gryffindor", "Ravenclaw", "Hogwarts").
     * @return Un objeto {@link Connection} a la base de datos de la casa,
     * o <code>null</code> si la casa no es válida o si ocurre un error de SQL
     * durante la conexión.
     */
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
            return conn;

        } catch (SQLException e) {
            logger.error("❌ Error al conectar con la base de datos de " + casa + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Obtiene una conexión directa a la base de datos principal (MariaDB - Hogwarts).
     * <p>
     * Este metodo es un atajo que utiliza directamente las credenciales de MariaDB
     * definidas en {@link Config} (<code>mariadb.url</code>, <code>mariadb.user</code>, etc.).
     *
     * @return Un objeto {@link Connection} a MariaDB, o <code>null</code> si
     * ocurre un error de SQL.
     */
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

    /**
     * Obtiene una conexión a la base de datos local de respaldo (SQLite).
     * <p>
     * Carga el driver de SQLite, se conecta al archivo <code>hogwarts_backup.db</code>
     * ubicado en la carpeta <code>../data/</code>.
     * <p>
     * <b>Importante:</b> Este metodo también asegura que la tabla <code>ESTUDIANTES</code>
     * exista en la base de datos, ejecutando un <code>CREATE TABLE IF NOT EXISTS</code>
     * si es necesario, antes de devolver la conexión.
     *
     * @return Un objeto {@link Connection} a la base de datos SQLite, o <code>null</code>
     * si ocurre un error al cargar el driver o al conectar.
     */
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