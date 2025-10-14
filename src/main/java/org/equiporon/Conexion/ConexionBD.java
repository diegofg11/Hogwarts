package org.equiporon.Conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clase encargada de gestionar las conexiones JDBC
 * tanto a las casas como a la base central Hogwarts.
 *
 * @author Diego
 */
public class ConexionBD {

    /**
     * Devuelve una conexión JDBC según la casa seleccionada.
     *
     * @param casa Nombre de la casa seleccionada.
     * @return Objeto Connection o null si la conexión falla.
     */
    public static Connection conectarCasa(String casa) {
        String url = "";

        switch (casa) {
            case "Gryffindor":
                url = Config.H2_URL;
                break;
            case "Ravenclaw":
                url = Config.ORACLE_URL;
                break;
            case "Slytherin":
                url = Config.HSQL_URL;
                break;
            case "Hufflepuff":
                url = Config.DERBY_URL;
                break;
            default:
                System.out.println("Casa no válida");
                return null;
        }

        try {
            return DriverManager.getConnection(url, Config.USER, Config.PASSWORD);
        } catch (SQLException e) {
            System.out.println("Error al conectar con la base de datos de " + casa + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Devuelve una conexión a la base central Hogwarts (MariaDB).
     *
     * @return conexión a MariaDB o null si ocurre un error.
     */
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(Config.MARIADB_URL, Config.USER, Config.PASSWORD);
        } catch (SQLException e) {
            System.out.println("Error al conectar con MariaDB: " + e.getMessage());
            return null;
        }
    }
}
