package org.equiporon.Conexion;

import java.sql.*;

public class ConexionBD {

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
                System.out.println("Casa no válida: " + casa);
                return null;
            }
        }

        try {
            Connection conn = DriverManager.getConnection(url, user, password);

            // 👇 Este bloque soluciona el error de Derby (APP.ESTUDIANTES no encontrado)
            // imports:


// dentro de tu try tras crear la conn:
            System.out.println("[Derby] URL usada: " + url + " user=" + user);

// Solo para Gryffindor/Derby:
            if ("Gryffindor".equalsIgnoreCase(casa)) {
                try (Statement st = conn.createStatement()) {
                    try (ResultSet rs = st.executeQuery("VALUES CURRENT_USER")) {
                        if (rs.next()) System.out.println("[Derby] CURRENT_USER=" + rs.getString(1));
                    }
                    try (ResultSet rs = st.executeQuery("VALUES CURRENT SCHEMA")) {
                        if (rs.next()) System.out.println("[Derby] CURRENT_SCHEMA=" + rs.getString(1));
                    }
                    try (ResultSet rs = st.executeQuery(
                            "SELECT TABLENAME FROM SYS.SYSTABLES WHERE TABLETYPE='T' AND UPPER(TABLENAME)='ESTUDIANTES'")) {
                        boolean exists = rs.next();
                        System.out.println("[Derby] ¿Existe tabla ESTUDIANTES en metadata? " + exists);
                    }
                    // Probar acceso real:
                    try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM APP.ESTUDIANTES")) {
                        if (rs.next()) System.out.println("[Derby] Filas en APP.ESTUDIANTES = " + rs.getInt(1));
                    }
                } catch (SQLException ex) {
                    System.err.println("[Derby] Diagnóstico falló: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }


            System.out.println("Conexión exitosa con " + casa);
            return conn;

        } catch (SQLException e) {
            System.err.println("❌ Error al conectar con la base de datos de " + casa + ": " + e.getMessage());
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
            System.out.println("Conexión exitosa con MariaDB (Hogwarts)");
            return conn;
        } catch (SQLException e) {
            System.err.println("❌ Error al conectar con MariaDB: " + e.getMessage());
            return null;
        }
    }
}



