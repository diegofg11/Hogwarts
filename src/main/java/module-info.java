module org.equiporon {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;

    opens org.equiporon.Controlador to javafx.fxml; // <--- abrir el paquete del controlador
    exports org.equiporon;
}
