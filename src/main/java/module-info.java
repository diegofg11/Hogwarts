module org.equiporon {

    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    // Drivers de bases de datos que estás usando
    requires org.mariadb.jdbc;
    requires com.h2database;
    requires org.apache.derby.client;
    requires com.oracle.database.jdbc;

    // Librerías adicionales
    requires org.slf4j;
    requires com.github.benmanes.caffeine;
    requires javafx.graphics;

    // 👇 Esta línea es la CLAVE para tu error:
    opens org.equiporon.Controlador to javafx.fxml;

    // Si tienes más controladores en otros paquetes, añádelos igual:
    // opens org.equiporon.Conexion to javafx.fxml;

    exports org.equiporon;
}