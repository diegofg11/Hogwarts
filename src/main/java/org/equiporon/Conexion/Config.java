package org.equiporon.Conexion;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Clase de configuración que carga las URLs y credenciales de las
 * bases de datos desde el archivo config.properties.
 *
 * @author Diego
 */
public class Config {

    private static final Properties props = new Properties();

    static {
        try (InputStream input = Config.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new IOException("No se encontró el archivo config.properties en el classpath.");
            }
            props.load(input);
        } catch (IOException e) {
            System.err.println("Error al cargar config.properties: " + e.getMessage());
        }
    }

    /** Devuelve el valor de una propiedad por clave */
    public static String get(String key) {
        return props.getProperty(key);
    }

    // Base central (MariaDB)
    public static String getMariaDBUrl() { return get("mariadb.url"); }
    public static String getMariaDBUser() { return get("mariadb.user"); }
    public static String getMariaDBPassword() { return get("mariadb.password"); }

    // Gryffindor (Derby)
    public static String getGryffindorUrl() { return get("gryffindor.url"); }
    public static String getGryffindorUser() { return get("gryffindor.user"); }
    public static String getGryffindorPassword() { return get("gryffindor.password"); }

    // Ravenclaw (Oracle)
    public static String getRavenclawUrl() { return get("ravenclaw.url"); }
    public static String getRavenclawUser() { return get("ravenclaw.user"); }
    public static String getRavenclawPassword() { return get("ravenclaw.password"); }

    // Hufflepuff (H2)
    public static String getHufflepuffUrl() { return get("hufflepuff.url"); }
    public static String getHufflepuffUser() { return get("hufflepuff.user"); }
    public static String getHufflepuffPassword() { return get("hufflepuff.password"); }

    // Slytherin (HSQLDB)
    public static String getSlytherinUrl() { return get("slytherin.url"); }
    public static String getSlytherinUser() { return get("slytherin.user"); }
    public static String getSlytherinPassword() { return get("slytherin.password"); }
}



