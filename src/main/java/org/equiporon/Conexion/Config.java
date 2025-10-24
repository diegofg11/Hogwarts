package org.equiporon.Conexion;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Clase de utilidad para gestionar la configuración de la aplicación.
 * <p>
 * Carga las propiedades definidas en el archivo <code>config.properties</code>
 * (ubicado en el classpath) en un bloque estático al iniciar la clase.
 * <p>
 * Proporciona métodos estáticos (getters) para acceder fácilmente a
 * configuraciones específicas, como las credenciales (URL, usuario, contraseña)
 * para las diferentes conexiones de bases de datos.
 *
 * @author Diego
 */
public class Config {
    private static final Properties props = new Properties();

    static {
        // Bloque estático para cargar las propiedades una sola vez
        try (InputStream input = Config.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                // Lanzamos una excepción más informativa si el archivo no se encuentra
                throw new IOException("No se encontró el archivo 'config.properties' en el classpath.");
            }
            props.load(input);
            System.out.println("Configuración cargada correctamente desde config.properties");
        } catch (IOException e) {
            // Manejo de error si la carga falla
            System.err.println("Error fatal al cargar 'config.properties': " + e.getMessage());
            // Dependiendo de la criticidad, podrías querer lanzar una RuntimeException aquí
            // throw new RuntimeException("No se pudo cargar la configuración", e);
        }
    }

    /**
     * Obtiene el valor de una propiedad específica del archivo de configuración.
     * Es el metodo base utilizado por todos los demás getters.
     *
     * @param key La clave (nombre) de la propiedad a buscar (ej. "mariadb.url").
     * @return El valor (String) asociado a la clave, o <code>null</code> si la clave no se encuentra.
     */
    public static String get(String key) {
        return props.getProperty(key);
    }

    // --- Getters para MariaDB ---

    /**
     * Obtiene la URL de conexión para la base de datos MariaDB.
     *
     * @return El valor de la propiedad "mariadb.url".
     */
    public static String getMariaDBUrl()      { return get("mariadb.url"); }

    /**
     * Obtiene el nombre de usuario para la base de datos MariaDB.
     *
     * @return El valor de la propiedad "mariadb.user".
     */
    public static String getMariaDBUser()     { return get("mariadb.user"); }

    /**
     * Obtiene la contraseña para la base de datos MariaDB.
     *
     * @return El valor de la propiedad "mariadb.password".
     */
    public static String getMariaDBPassword() { return get("mariadb.password"); }

    // --- Getters para Gryffindor ---

    /**
     * Obtiene la URL de conexión para la base de datos Gryffindor.
     *
     * @return El valor de la propiedad "gryffindor.url".
     */
    public static String getGryffindorUrl()      { return get("gryffindor.url"); }

    /**
     * Obtiene el nombre de usuario para la base de datos Gryffindor.
     *
     * @return El valor de la propiedad "gryffindor.user".
     */
    public static String getGryffindorUser()     { return get("gryffindor.user"); }

    /**
     * Obtiene la contraseña para la base de datos Gryffindor.
     *
     * @return El valor de la propiedad "gryffindor.password".
     */
    public static String getGryffindorPassword() { return get("gryffindor.password"); }

    // --- Getters para Ravenclaw ---

    /**
     * Obtiene la URL de conexión para la base de datos Ravenclaw.
     *
     * @return El valor de la propiedad "ravenclaw.url".
     */
    public static String getRavenclawUrl()      { return get("ravenclaw.url"); }

    /**
     * Obtiene el nombre de usuario para la base de datos Ravenclaw.
     *
     * @return El valor de la propiedad "ravenclaw.user".
     */
    public static String getRavenclawUser()     { return get("ravenclaw.user"); }

    /**
     * Obtiene la contraseña para la base de datos Ravenclaw.
     *
     * @return El valor de la propiedad "ravenclaw.password".
     */
    public static String getRavenclawPassword() { return get("ravenclaw.password"); }

    // --- Getters para Hufflepuff ---

    /**
     * Obtiene la URL de conexión para la base de datos Hufflepuff.
     *
     * @return El valor de la propiedad "hufflepuff.url".
     */
    public static String getHufflepuffUrl()      { return get("hufflepuff.url"); }

    /**
     * Obtiene el nombre de usuario para la base de datos Hufflepuff.
     *
     * @return El valor de la propiedad "hufflepuff.user".
     */
    public static String getHufflepuffUser()     { return get("hufflepuff.user"); }

    /**
     * Obtiene la contraseña para la base de datos Hufflepuff.
     *
     * @return El valor de la propiedad "hufflepuff.password".
     */
    public static String getHufflepuffPassword() { return get("hufflepuff.password"); }

    // --- Getters para Slytherin ---

    /**
     * Obtiene la URL de conexión para la base de datos Slytherin.
     *
     * @return El valor de la propiedad "slytherin.url".
     */
    public static String getSlytherinUrl()      { return get("slytherin.url"); }

    /**
     * Obtiene el nombre de usuario para la base de datos Slytherin.
     *
     * @return El valor de la propiedad "slytherin.user".
     */
    public static String getSlytherinUser()     { return get("slytherin.user"); }

    /**
     * Obtiene la contraseña para la base de datos Slytherin.
     *
     * @return El valor de la propiedad "slytherin.password".
     */
    public static String getSlytherinPassword() { return get("slytherin.password"); }
}