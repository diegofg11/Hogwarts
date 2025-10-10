module org.equiporon {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.sql;
    opens org.equiporon to javafx.fxml;
    exports org.equiporon;
}