package org.equiporon.Controlador;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.equiporon.Conexion.ConexionBD;
import org.equiporon.DAO.*;
import org.equiporon.Modelo.Modelo_Estudiante;
import javafx.scene.control.cell.PropertyValueFactory;

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
    @FXML
    private Label lblCasaSeleccionada;
    @FXML
    private ChoiceBox<String> choiceCasas;
    @FXML
    private Button botAdd;
    @FXML
    private Button botBorrar;
    @FXML
    private Button botEditar;
    @FXML
    private Label lblCasa;
    @FXML
    private TableView<?> tablaEstudiantes;
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
    private TextField txtNombre;
    @FXML
    private TextField txtPatronus;
    private DerbyDAO derbydao = new DerbyDAO();
    private H2DAO h2dao = new H2DAO();
    private HSQLDBDAO hsqldao = new HSQLDBDAO();
    private MariaDBDAO mariadao = new MariaDBDAO();
    private OracleDAO oracledao = new OracleDAO();
    private SQLiteDAO sqldao = new SQLiteDAO();

    private String casaActual = null;

    /**
     * Este metodo se llama automáticamente después de que el archivo fxml ha sido cargado.
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
     * Este metodo es llamado por el listener del ChoiceBox.
     *
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
                Alert alert = new Alert(Alert.AlertType.ERROR,"Error al conectar con " + casa);
                alert.showAndWait();
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR,"Error crítico al conectar con " + casa + ": " + e.getMessage());
            alert.showAndWait();
            e.printStackTrace();
        }
    }

    // --- Métodos para los botones del formulario ---
    @FXML
    void clickOnAdd(ActionEvent event) {
        // Lógica para añadir un nuevo estudiante
        String nombre = txtNombre.getText();
        String apellido = txtApellidos.getText();
        String curso = txtCurso.getText();
        String casa = txtCasa.getText();
        String patronus = txtPatronus.getText();


        if (nombre.isEmpty() || apellido.isEmpty() || curso.isEmpty() || patronus.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Rellena todos los campos");
            alert.showAndWait();
            return;
        }

        Modelo_Estudiante est = new Modelo_Estudiante(0, nombre, apellido, casa, curso, patronus);

        switch (casaActual) {
            case "Hogwarts":
                if (mariadao.insertarEstudiante(est) || sqldao.insertarEstudiante(est)) {
                    tablaEstudiantes.getItems().add(est);
                }

                break;

            case "Gryffindor":


                if (derbydao.aniadir(est)) {
                    tablaEstudiantes.getItems().add(est);
                }
                break;

            case "Ravenclaw":


                if (oracledao.aniadir(est)) {
                    tablaEstudiantes.getItems().add(est);
                }
                break;

            case "Slytherin":
                if (hsqldao.insertEstudianteAsync(est)) {
                    tablaEstudiantes.getItems().add(est);
                }
                break;
            case "Hufflepuff":
                if (h2dao.insertEstudianteAsync(est)) {
                    tablaEstudiantes.getItems().add(est);
                }
                break;

            default:
                Alert alert = new Alert(Alert.AlertType.ERROR, "No se pudo añadir la persona");
                alert.showAndWait();
        }
    }

        @FXML
        void clickOnBorrar (ActionEvent event){
            // Lógica para borrar un estudiante seleccionado
        }

        @FXML
        void clickOnEditar (ActionEvent event){
            // Lógica para editar un estudiante
        }

        // --- Métodos de la barra de menú ---
        @FXML
        void clickOnFile (ActionEvent event){
            // Lógica para el menú File -> Close
        }

        @FXML
        void clickOnEdit (ActionEvent event){
            // Lógica para el menú Edit -> Delete
        }

        @FXML
        void clickOnHelp (ActionEvent event){
            // Lógica para el menú Help -> About
    }
}