package org.equiporon.Controlador;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class Controlador {

    @FXML
    private Label lblCasaSeleccionada;

    @FXML
    private Button botAdd;

    @FXML
    private Button botBorrar;

    @FXML
    private Button botEditar;

    @FXML
    private Menu menuCasas;

    @FXML
    private Menu menuEdit;

    @FXML
    private Menu menuFile;

    @FXML
    private Menu menuHelp;

    @FXML
    private TableColumn<?, ?> tableApellidos;

    @FXML
    private TableColumn<?, ?> tableCasa;

    @FXML
    private TableColumn<?, ?> tableCurso;

    @FXML
    private TableColumn<?, ?> tableId;

    @FXML
    private TableColumn<?, ?> tableNombre;

    @FXML
    private TableColumn<?, ?> tablePatronus;

    @FXML
    private TextField txtApellidos;

    @FXML
    private TextField txtCasa;

    @FXML
    private TextField txtCurso;

    @FXML
    private TextField txtId;

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtPatronus;



    @FXML
    void clickOnAdd(ActionEvent event) {

    }

    @FXML
    void clickOnBorrar(ActionEvent event) {

    }

    @FXML
    void clickOnCasas(ActionEvent event) {

    }

    @FXML
    void clickOnEdit(ActionEvent event) {

    }

    @FXML
    void clickOnEditar(ActionEvent event) {

    }

    @FXML
    void clickOnFile(ActionEvent event) {

    }

    @FXML
    void clickOnHelp(ActionEvent event) {

    }


    /**
     * Inicializa el controlador una vez cargado el archivo FXML.
     * <p>
     * Este metodo se ejecuta automáticamente al crear la interfaz.
     * Añade dinámicamente al menú {@code menuCasas} las cinco casas
     * del universo de Harry Potter: Gryffindor, Hufflepuff, Ravenclaw,
     * Slytherin y Hogwarts.
     * </p>
     * <p>
     * Cada elemento del menú invoca el metodo {@link #seleccionarCasa(String)}
     * al ser seleccionado por el usuario.
     * </p>
     *
     * @see #seleccionarCasa(String)
     */
    @FXML
    public void initialize() {
        // Crear las casas de Harry Potter
        String[] casas = {"Gryffindor", "Hufflepuff", "Ravenclaw", "Slytherin", "Hogwarts"};

        for (String casa : casas) {
            MenuItem item = new MenuItem(casa);
            // Asignamos acción a cada item del menú
            item.setOnAction(event -> seleccionarCasa(casa));
            menuCasas.getItems().add(item);
        }
    }

    private void seleccionarCasa(String casa) {
        //TODO
    }
}
