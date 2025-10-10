package org.equiporon.Controlador;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.equiporon.DAO.*;
import org.equiporon.Modelo.Modelo_Estudiante;

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
    private Label lblCasa;

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
        // 1️⃣ Actualizar el Label con la casa seleccionada
        lblCasaSeleccionada.setText(casa);

        // 2️⃣ Mostrar u ocultar la columna 'Casa' en la tabla
        if (tableCasa != null) { // asegurarse que la columna existe
            tableCasa.setVisible(casa.equals("Hogwarts"));
            lblCasa.setVisible(casa.equals("Hogwarts"));
            txtCasa.setVisible(casa.equals("Hogwarts"));
        }

        // 3️⃣ Cargar datos según la casa seleccionada usando tus DAOs
        //    Suponiendo que tienes un metodo obtenerTodos() en cada DAO:
        ObservableList<Modelo_Estudiante> lista = FXCollections.observableArrayList();

        switch (casa) {
            case "Gryffindor" -> lista.addAll(DerbyDAO.obtenerTodos());
            case "Hufflepuff" -> lista.addAll(H2DAO.obtenerTodos());
            case "Ravenclaw" -> lista.addAll(OracleDAO.obtenerTodos());
            case "Slytherin" -> lista.addAll(HSQLDBDAO.obtenerTodos());
            case "Hogwarts" -> lista.addAll(MariaDBDAO.obtenerTodos());
        }

        // Suponiendo que tu TableView se llama tablaEstudiantes
        tablaEstudiantes.setItems(lista);
    }
}