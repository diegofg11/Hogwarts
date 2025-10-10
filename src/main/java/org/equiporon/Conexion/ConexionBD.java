package org.equiporon.Conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.InputStream;
import java.util.Properties;

/**
 * Clase estática para gestionar la conexión a la base de datos GLOBAL (MariaDB).
 * Carga las credenciales específicas para la conexión global desde application.properties.
 */
public class ConexionBD { // 👈 Nombre de archivo según tu solicitud

    private static final Logger logger = LoggerFactory.getLogger(ConexionBD.class);
    private static final Properties PROPS = new Properties();

    private static String URL;
    private static String USER;
    private static String PASSWORD;

    // Bloque estático: Carga las propiedades específicas para MariaDB (Global)
    static {
        // Intentamos cargar el archivo de configuración.
        try (InputStream input = ConexionBD.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                logger.warn("No se encontró el archivo application.properties. Usando valores por defecto para MariaDB.");
                // Valores de fallback (ajusta si es necesario)
                URL = "jdbc:mysql://localhost:3306/HogwartsGlobal";
                USER = "root";
                PASSWORD = "admin";
            } else {
                PROPS.load(input);
                // Usamos el prefijo 'db.global.' para la configuración de la BDD Global
                URL = PROPS.getProperty("db.global.url", "jdbc:mysql://localhost:3306/HogwartsGlobal");
                USER = PROPS.getProperty("db.global.user", "root");
                PASSWORD = PROPS.getProperty("db.global.password", "admin");
                logger.info("Configuración de MariaDB (Global) cargada desde application.properties.");
            }
        } catch (Exception ex) {
            logger.error("Error al cargar o parsear application.properties para MariaDB.", ex);
            // Si hay un error, usamos los valores por defecto
            URL = "jdbc:mysql://localhost:3306/HogwartsGlobal";
            USER = "root";
            PASSWORD = "admin";
        }
    }

    /**
     * Establece y devuelve una nueva conexión con la base de datos MariaDB.
     * @return Objeto Connection activo.
     * @throws SQLException Si ocurre un error de conexión.
     */
    public static Connection getConnection() throws SQLException {
        logger.debug("Intentando establecer conexión a la BDD GLOBAL: {}", URL);
        try {
            Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
            logger.info("Conexión a la BDD Global (MariaDB) establecida con éxito.");
            return connection;
        } catch (SQLException e) {
            logger.error("ERROR: Fallo al conectar con MariaDB. Revise el servicio/credenciales.", e);
            throw e;
        }
    }
}
