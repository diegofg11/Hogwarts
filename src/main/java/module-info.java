module org.equiporon {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    opens org.equiporon to javafx.fxml;
    exports org.equiporon;
}