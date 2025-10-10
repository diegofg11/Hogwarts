package org.equiporon.Conexion;

/**
 * Configuración de URLs JDBC y credenciales
 * para cada base de datos de las casas de Hogwarts.
 *
 * @author Diego
 */
public class Config {
    public static final String H2_URL = "jdbc:h2:tcp://localhost/~/gryffindor";          // Gryffindor
    public static final String ORACLE_URL = "jdbc:oracle:thin:@localhost:1521:ravenclaw"; // Ravenclaw
    public static final String HSQL_URL = "jdbc:hsqldb:hsql://localhost/slytherin";       // Slytherin
    public static final String DERBY_URL = "jdbc:derby://localhost:1527/hufflepuff";      // Hufflepuff

    public static final String USER = "root";
    public static final String PASSWORD = "root";
}

