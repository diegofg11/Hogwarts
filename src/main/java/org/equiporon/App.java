package org.equiporon;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Clase principal de la aplicación JavaFX.
 * <p>
 * Esta clase inicializa la interfaz gráfica, carga los archivos FXML y
 * permite cambiar entre diferentes escenas dentro de la aplicación.
 * </p>
 *
 * @author Unai, Ruben, Diego
 */
public class App extends Application {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    /** Ventana principal de la aplicación (Stage). */
    private static Stage stage;

    /**
     * Metodo de inicio de la aplicación JavaFX.
     * <p>
     * Se ejecuta automáticamente al lanzar la aplicación e inicializa la
     * escena principal definida en el archivo FXML "primary.fxml".
     * Además, incorpora un icono a la aplicación.
     * </p>
     *
     * @author Ruben, Diego
     * @param s instancia del {@link Stage} principal proporcionado por JavaFX.
     * @throws IOException si ocurre un error al cargar el archivo FXML inicial.
     */
    @Override
    public void start(@SuppressWarnings("exports") Stage s) throws IOException {
        stage = s;
        setRoot("primary", "");
        Image icon = new Image(
                App.class.getResource("/images/hogwarts_escudo.png").toExternalForm()
        );
        s.getIcons().add(icon);
    }

    /**
     * Cambia la escena principal de la aplicación cargando un nuevo archivo FXML.
     * <p>
     * Mantiene el título actual del escenario.
     * </p>
     *
     * @param fxml nombre del archivo FXML (sin extensión) que se desea cargar.
     * @throws IOException si ocurre un error al cargar el archivo FXML.
     */
    static void setRoot(String fxml) throws IOException {
        setRoot(fxml, stage.getTitle());
    }

    /**
     * Cambia la escena principal de la aplicación con un nuevo archivo FXML y título.
     *
     * @param fxml nombre del archivo FXML (sin extensión) que se desea cargar.
     * @param title título de la ventana que se mostrará tras el cambio.
     * @throws IOException si ocurre un error al cargar el archivo FXML.
     */
    static void setRoot(String fxml, String title) throws IOException {
        Scene scene = new Scene(loadFXML(fxml));
        //scene.getStylesheets().add(App.class.getResource("/styles/Styles.css").toExternalForm());
        stage.setMinWidth(600);
        stage.setMinHeight(400);
        stage.setTitle("Hogwarts");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Carga un archivo FXML desde el directorio de recursos y devuelve su contenido.
     *
     * @param fxml nombre del archivo FXML (sin extensión) que se desea cargar.
     * @return el nodo raíz ({@link Parent}) del archivo FXML cargado.
     * @throws IOException si no se puede encontrar o cargar el archivo FXML.
     */
    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/fxml/" + fxml + ".fxml"));
        return fxmlLoader.load();
    }

    /**
     * Metodo principal que lanza la aplicación JavaFX.
     *
     * @param args argumentos de la línea de comandos (no utilizados en esta aplicación).
     */
    public static void main(String[] args) {
        launch(args);
    }
}
