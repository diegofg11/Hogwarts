package org.equiporon.Controlador;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import org.equiporon.Conexion.ConexionBD;

import java.sql.Connection;

/**
 * Controlador principal del proyecto Hogwarts.
 * Permite seleccionar una casa desde el menú y conectarse
 * a la base de datos correspondiente.
 * Además, cambia los colores de la ventana según la casa seleccionada
 * y muestra estandartes e imágenes de escudos según la casa.
 *
 *
 */
public class Controlador {

    @FXML private AnchorPane rootPane;
    @FXML private Label lblCasaSeleccionada;
    @FXML private ComboBox<String> choiceCasas;
    @FXML private Button botAdd;
    @FXML private Button botBorrar;
    @FXML private Button botEditar;
    @FXML private Label lblCasa;
    @FXML private TableView<?> tablaEstudiantes;
    @FXML private TableColumn<?, ?> tableApellidos;
    @FXML private TableColumn<?, ?> tableCasa;
    @FXML private TableColumn<?, ?> tableCurso;
    @FXML private TableColumn<?, ?> tableId;
    @FXML private TableColumn<?, ?> tableNombre;
    @FXML private TableColumn<?, ?> tablePatronus;
    @FXML private TextField txtApellidos;
    @FXML private TextField txtCasa;
    @FXML private TextField txtCurso;
    @FXML private TextField txtNombre;
    @FXML private TextField txtPatronus;
    @FXML private ImageView escudoCasa;
    @FXML private ImageView bannerIzquierdo;
    @FXML private ImageView bannerDerecho;

    private String casaActual = null;

    /**
     * Inicializa el controlador: llena el ComboBox, configura colores, listener e imágenes iniciales.
     * También posiciona los estandartes a 10px de los bordes y pegados arriba.
     * @author Xiker
     */
    @FXML
    private void initialize() {
        // Rellenar ComboBox
        choiceCasas.getItems().addAll("Hogwarts", "Gryffindor", "Hufflepuff", "Ravenclaw", "Slytherin");

        // Listener de selección
        choiceCasas.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        seleccionarCasa(newValue);
                        aplicarColorVentana(newValue);
                        aplicarImagenesCasa(newValue);
                    }
                }
        );

        // Colorear ComboBox
        setupComboBoxColors();

        // Selección por defecto
        choiceCasas.setValue("Hogwarts");
        aplicarColorVentana("Hogwarts");
        aplicarImagenesCasa("Hogwarts");

        // Posicionar banners
        AnchorPane.setTopAnchor(bannerIzquierdo, 0.0);
        AnchorPane.setLeftAnchor(bannerIzquierdo, 10.0);

        AnchorPane.setTopAnchor(bannerDerecho, 0.0);
        AnchorPane.setRightAnchor(bannerDerecho, 10.0);
    }

    /**
     * Aplica imágenes de escudo y estandartes según la casa seleccionada.
     * @param casa Nombre de la casa seleccionada.
     * @author Xiker
     */
    private void aplicarImagenesCasa(String casa) {
        String basePath = "/images/";
        String nombre = casa.toLowerCase();

        // Escudo
        String escudoPath = basePath + nombre + "_escudo.png";
        escudoCasa.setImage(new javafx.scene.image.Image(getClass().getResourceAsStream(escudoPath)));

        // Banners
        String bannerPath = basePath + nombre + "_banner.png";
        bannerIzquierdo.setImage(new javafx.scene.image.Image(getClass().getResourceAsStream(bannerPath)));
        bannerDerecho.setImage(new javafx.scene.image.Image(getClass().getResourceAsStream(bannerPath)));
    }

    /**
     * Configura los colores de los items y del botón del ComboBox.
     */
    private void setupComboBoxColors() {
        choiceCasas.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle(getCasaColorStyle(item));
                }
            }
        });

        choiceCasas.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle(getCasaColorStyle(item));
                }
            }
        });
    }

    /**
     * Devuelve el estilo CSS según la casa.
     * @param casa Nombre de la casa
     * @return cadena CSS
     */
    private String getCasaColorStyle(String casa) {
        return switch (casa) {
            case "Gryffindor" -> "-fx-background-color: #7F0909; -fx-text-fill: #FFC500;";
            case "Slytherin" -> "-fx-background-color: #1A472A; -fx-text-fill: #AAAAAA;";
            case "Ravenclaw" -> "-fx-background-color: #0E1A40; -fx-text-fill: #946B2D;";
            case "Hufflepuff" -> "-fx-background-color: #EEE117; -fx-text-fill: #000000;";
            case "Hogwarts" -> "-fx-background-color: #7B3F00; -fx-text-fill: #000000;";
            default -> "-fx-background-color: white; -fx-text-fill: black;";
        };
    }

    /**
     * Aplica la clase CSS de la casa al AnchorPane raíz.
     * Esto cambia los colores de toda la ventana según la casa.
     */
    private void aplicarColorVentana(String casa) {
        rootPane.getStyleClass().removeAll("gryffindor", "slytherin", "ravenclaw", "hufflepuff", "hogwarts");
        switch (casa) {
            case "Gryffindor" -> rootPane.getStyleClass().add("gryffindor");
            case "Slytherin" -> rootPane.getStyleClass().add("slytherin");
            case "Ravenclaw" -> rootPane.getStyleClass().add("ravenclaw");
            case "Hufflepuff" -> rootPane.getStyleClass().add("hufflepuff");
            default -> rootPane.getStyleClass().add("hogwarts");
        }
    }

    /**
     * Lógica de selección de casa y conexión a la base de datos.
     */
    private void seleccionarCasa(String casa) {
        casaActual = casa;
        lblCasaSeleccionada.setText("Casa seleccionada: " + casa);
        txtCasa.setText(casa);

        try (Connection conn = ConexionBD.conectarCasa(casa)) {
            if (conn != null) {
                System.out.println("Conectado a " + casa);
            } else {
                mostrarError("Error al conectar con " + casa);
            }
        } catch (Exception e) {
            mostrarError("Error crítico al conectar con " + casa + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML void clickOnAdd(ActionEvent event) {}
    @FXML void clickOnBorrar(ActionEvent event) {}
    @FXML void clickOnEditar(ActionEvent event) {}
    @FXML void clickOnFile(ActionEvent event) {}
    @FXML void clickOnEdit(ActionEvent event) {}
    @FXML void clickOnHelp(ActionEvent event) {}

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error de Conexión");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
