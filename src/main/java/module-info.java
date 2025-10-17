module org.equiporon {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.sql;
    requires org.slf4j;

    // 👇 HABILITA REFLEXIÓN para JavaFX
    opens org.equiporon.Controlador to javafx.fxml;
    opens org.equiporon to javafx.fxml;

    // 👇 EXPORTA LOS PAQUETES si necesitas usar las clases fuera del módulo
    exports org.equiporon;
    exports org.equiporon.Controlador;
}