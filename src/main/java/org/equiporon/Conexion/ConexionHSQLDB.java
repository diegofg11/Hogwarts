package org.equiporon.Conexion; // Coincide con el import en HSQLDBDAO.java

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.InputStream;
import java.util.Properties;

/**
 * Clase estática para gestionar la conexión a la base de datos HSQLDB (Slytherin).
 * Carga las credenciales específicas para HSQLDB/Slytherin desde application.properties.
 */
public class ConexionHSQLDB {

    private static final Logger logger = LoggerFactory.getLogger(ConexionHSQLDB.class);
    private static final Properties PROPS = new Properties();

    private static String URL;
    private static String USER;
    private static String PASSWORD;

    // Bloque estático: Carga las propiedades específicas para HSQLDB (Slytherin)
    static {
        try (InputStream input = ConexionHSQLDB.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                logger.warn("No se encontró el archivo application.properties. Usando valores por defecto para HSQLDB.");
                // Valores por defecto para la base de datos de archivo HSQLDB
                URL = "jdbc:hsqldb:file:./hsqldb/slytherin_db;shutdown=true";
                USER = "SA";
                PASSWORD = "";
            } else {
                PROPS.load(input);
                // Usamos el prefijo 'db.slytherin.' para la configuración
                URL = PROPS.getProperty("db.slytherin.url", "jdbc:hsqldb:file:./hsqldb/slytherin_db;shutdown=true");
                USER = PROPS.getProperty("db.slytherin.user", "SA");
                PASSWORD = PROPS.getProperty("db.slytherin.password", "");
                logger.info("Configuración de HSQLDB (Slytherin) cargada desde application.properties.");
            }
        } catch (Exception ex) {
            logger.error("Error al cargar o parsear application.properties para HSQLDB.", ex);
            URL = "jdbc:hsqldb:file:./hsqldb/slytherin_db;shutdown=true";
            USER = "SA";
            PASSWORD = "";
        }
    }

    /**
     * Establece y devuelve una nueva conexión con la base de datos HSQLDB.
     */
    public static Connection getConnection() throws SQLException {
        logger.debug("Intentando establecer conexión a la BDD HSQLDB: {}", URL);
        try {
            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
            logger.info("Conexión a la BDD HSQLDB (Slytherin) establecida con éxito.");
            return connection;
        } catch (SQLException e) {
            logger.error("ERROR: Fallo al conectar con HSQLDB. Revise el servicio/credenciales.", e);
            throw e;
        }
    }
}