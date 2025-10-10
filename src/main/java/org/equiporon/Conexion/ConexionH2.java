package org.equiporon.Conexion; // Coincide con el import en H2DAO.java

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.InputStream;
import java.util.Properties;

/**
 * Clase estática para gestionar la conexión a la base de datos H2 (Hufflepuff).
 * Carga las credenciales específicas para H2/Hufflepuff desde application.properties.
 */
public class ConexionH2 {

    private static final Logger logger = LoggerFactory.getLogger(ConexionH2.class);
    private static final Properties PROPS = new Properties();

    private static String URL;
    private static String USER;
    private static String PASSWORD;

    // Bloque estático: Carga las propiedades específicas para H2 (Hufflepuff)
    static {
        try (InputStream input = ConexionH2.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                logger.warn("No se encontró el archivo application.properties. Usando valores por defecto para H2.");
                // Valores por defecto para la base de datos embebida H2
                URL = "jdbc:h2:~/hufflepuff_db";
                USER = "sa";
                PASSWORD = "";
            } else {
                PROPS.load(input);
                // Usamos el prefijo 'db.hufflepuff.' para la configuración
                URL = PROPS.getProperty("db.hufflepuff.url", "jdbc:h2:~/hufflepuff_db");
                USER = PROPS.getProperty("db.hufflepuff.user", "sa");
                PASSWORD = PROPS.getProperty("db.hufflepuff.password", "");
                logger.info("Configuración de H2 (Hufflepuff) cargada desde application.properties.");
            }
        } catch (Exception ex) {
            logger.error("Error al cargar o parsear application.properties para H2.", ex);
            URL = "jdbc:h2:~/hufflepuff_db";
            USER = "sa";
            PASSWORD = "";
        }
    }

    /**
     * Establece y devuelve una nueva conexión con la base de datos H2.
     */
    public static Connection getConnection() throws SQLException {
        logger.debug("Intentando establecer conexión a la BDD H2: {}", URL);
        try {
            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
            logger.info("Conexión a la BDD H2 (Hufflepuff) establecida con éxito.");
            return connection;
        } catch (SQLException e) {
            logger.error("ERROR: Fallo al conectar con H2. Revise el servicio/credenciales.", e);
            throw e;
        }
    }
}