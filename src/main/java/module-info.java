module org.equiporon {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.sql;
    requires org.slf4j;

    // Abre los paquetes para reflexión de FXML
    opens org.equiporon.Controlador to javafx.fxml;
    opens org.equiporon to javafx.fxml;

    // Exporta lo que necesites
    exports org.equiporon;
}
