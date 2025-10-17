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
        String url = null;
        String user = null;
        String password = null;

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
            default -> {
                System.out.println("Casa no válida: " + casa);
                return null;
            }
        }

        try {
            Connection conn = DriverManager.getConnection(url, user, password);
            return conn;
        } catch (SQLException e) {
            System.err.println("Error al conectar con la base de datos de " + casa + ": " + e.getMessage());
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
            Connection conn = DriverManager.getConnection(
                    Config.getMariaDBUrl(),
                    Config.getMariaDBUser(),
                    Config.getMariaDBPassword()
            );
            return conn;
        } catch (SQLException e) {
            System.err.println("Error al conectar con MariaDB: " + e.getMessage());
            return null;
        }
    }
}

