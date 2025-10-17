package org.equiporon.Controlador;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.equiporon.Conexion.ConexionBD;

import java.sql.Connection;

/**
 * Controlador principal del proyecto Hogwarts.
 * Permite seleccionar una casa desde el menú y conectarse
 * a la base de datos correspondiente.
 *
 * @author Diego,Ruben,Unai
 */
public class Controlador {

    // --- Elementos FXML Conectados ---
    @FXML private Label lblCasaSeleccionada;
    @FXML private ChoiceBox<String> choiceCasas;
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

    private String casaActual = null;

    /**
     * Este método se llama automáticamente después de que el archivo fxml ha sido cargado.
     * Lo usamos para configurar el estado inicial de la interfaz.
     */
    @FXML
    private void initialize() {
        // 1. Rellena el ChoiceBox con las opciones de casas
        choiceCasas.getItems().addAll("Hogwarts", "Gryffindor", "Hufflepuff", "Ravenclaw", "Slytherin");

        // 2. Añade un "listener" que se activa cuando el usuario elige una casa
        choiceCasas.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        seleccionarCasa(newValue);
                    }
                }
        );

        // 3. Establece "Hogwarts" como la selección por defecto al iniciar
        choiceCasas.setValue("Hogwarts");
    }

    /**
     * Lógica central para conectarse a la base de datos de la casa seleccionada.
     * Este método es llamado por el listener del ChoiceBox.
     * @param casa El nombre de la casa a la que conectar.
     */
    private void seleccionarCasa(String casa) {
        casaActual = casa;
        lblCasaSeleccionada.setText("Casa seleccionada: " + casa);
        txtCasa.setText(casa); // Actualiza también el campo de texto del formulario

        try (Connection conn = ConexionBD.conectarCasa(casa)) {
            if (conn != null) {
                System.out.println("Conectado a " + casa);
                // Aquí iría la lógica para cargar los datos en la tabla, por ejemplo.
            } else {
                mostrarError("Error al conectar con " + casa);
            }
        } catch (Exception e) {
            mostrarError("Error crítico al conectar con " + casa + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    // --- Métodos para los botones del formulario ---
    @FXML
    void clickOnAdd(ActionEvent event) {
        // Lógica para añadir un nuevo estudiante
    }

    @FXML
    void clickOnBorrar(ActionEvent event) {
        // Lógica para borrar un estudiante seleccionado
    }

    @FXML
    void clickOnEditar(ActionEvent event) {
        // Lógica para editar un estudiante
    }

    // --- Métodos de la barra de menú ---
    @FXML
    void clickOnFile(ActionEvent event) {
        // Lógica para el menú File -> Close
    }

    @FXML
    void clickOnEdit(ActionEvent event) {
        // Lógica para el menú Edit -> Delete
    }

    @FXML
    void clickOnHelp(ActionEvent event) {
        // Lógica para el menú Help -> About
    }


    // --- Métodos de utilidad para mostrar alertas ---
    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error de Conexión");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}