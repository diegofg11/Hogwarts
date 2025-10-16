module org.equiporon {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.sql;
    requires org.slf4j;

    opens org.equiporon to javafx.fxml;
    opens org.equiporon.Controlador to javafx.fxml; // 👈 Añade esta línea

    exports org.equiporon;
}
