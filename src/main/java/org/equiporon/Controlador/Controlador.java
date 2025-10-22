package org.equiporon.Controlador;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import org.equiporon.Conexion.ConexionBD;
import org.equiporon.DAO.*;
import org.equiporon.Modelo.Modelo_Estudiante;

import java.sql.Connection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Controlador {

    private static final Logger logger = LoggerFactory.getLogger(Controlador.class);


    // --- Elementos FXML (todos los fx:id usados por primary.fxml) ---
    @FXML private AnchorPane rootPane;
    @FXML private Label lblCasaSeleccionada;
    @FXML private ComboBox<String> choiceCasas;
    @FXML private Button botAdd;
    @FXML private Button botBorrar;
    @FXML private Button botEditar;
    @FXML private Label lblCasa;
    @FXML private TableView<Modelo_Estudiante> tablaEstudiantes;
    @FXML private TableColumn<Modelo_Estudiante, String> tableId;
    @FXML private TableColumn<Modelo_Estudiante, String> tableNombre;
    @FXML private TableColumn<Modelo_Estudiante, String> tableApellidos;
    @FXML private TableColumn<Modelo_Estudiante, String> tableCasa;
    @FXML private TableColumn<Modelo_Estudiante, Integer> tableCurso;
    @FXML private TableColumn<Modelo_Estudiante, String> tablePatronus;
    @FXML private TextField txtApellidos;
    @FXML private TextField txtCasa;
    @FXML private TextField txtCurso;
    @FXML private TextField txtNombre;
    @FXML private TextField txtPatronus;
    @FXML private ImageView escudoCasa;
    @FXML private ImageView bannerIzquierdo;
    @FXML private ImageView bannerDerecho;

    // estado
    private String casaActual = null;
    private Object daoActual = null;

    // ----------------- Inicialización -----------------
    @FXML
    private void initialize() {
        // Poblamos el ComboBox
        choiceCasas.getItems().addAll("Hogwarts", "Gryffindor", "Hufflepuff", "Ravenclaw", "Slytherin");
        choiceCasas.setValue("Hogwarts");

        // Setup tabla (factories)
        tableId.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getId()));
        tableNombre.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNombre()));
        tableApellidos.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getApellidos()));
        tableCasa.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getCasa()));
        tableCurso.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getCurso()).asObject());
        tablePatronus.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getPatronus()));

        // Listener al cambiar la casa
        choiceCasas.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        seleccionarCasa(newValue);
                        aplicarColorVentana(newValue);
                        aplicarImagenesCasa(newValue);
                    }
                }
        );

        // Colores y apariencia del ComboBox (estilo por casa)
        setupComboBoxColors();

        // Selección por defecto: Hogwarts
        choiceCasas.setValue("Hogwarts");
        txtCasa.setText("Hogwarts");

        // Aplicar estilo e imagenes iniciales
        aplicarColorVentana("Hogwarts");
        aplicarImagenesCasa("Hogwarts");

        // Intentamos seleccionar la casa por defecto (conexión y carga de estudiantes)
        seleccionarCasa("Hogwarts");
    }

    // ----------------- Lógica de imágenes y estilos -----------------
    private void aplicarImagenesCasa(String casa) {
        String basePath = "/images/";
        String nombre = casa.toLowerCase();

        try {
            String escudoPath = basePath + nombre + "_escudo.png";
            Image escudo = new Image(getClass().getResourceAsStream(escudoPath));
            escudoCasa.setImage(escudo);

            String bannerPath = basePath + nombre + "_banner.png";
            Image banner = new Image(getClass().getResourceAsStream(bannerPath));
            bannerIzquierdo.setImage(banner);
            bannerDerecho.setImage(banner);
        } catch (Exception e) {
            // Si no existe la imagen, no fallamos la app; dejamos imágenes previamente cargadas o vacías
            System.err.println("No se encontraron imágenes para " + casa + ": " + e.getMessage());
        }
    }

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

    private String getCasaColorStyle(String casa) {
        return switch (casa) {
            case "Gryffindor" -> "-fx-background-color: #7F0909; -fx-text-fill: #FFC500;";
            case "Slytherin" -> "-fx-background-color: #1A472A; -fx-text-fill: #AAAAAA;";
            case "Ravenclaw" -> "-fx-background-color: #0E1A40; -fx-text-fill: #e6e9f8;";
            case "Hufflepuff" -> "-fx-background-color: #EEE117; -fx-text-fill: #000000;";
            case "Hogwarts" -> "-fx-background-color: #000000; -fx-text-fill: #FFD700;";
            default -> "-fx-background-color: white; -fx-text-fill: black;";
        };
    }

    private void aplicarColorVentana(String casa) {
        if (rootPane == null) return;
        rootPane.getStyleClass().removeAll("gryffindor", "slytherin", "ravenclaw", "hufflepuff", "hogwarts");
        switch (casa) {
            case "Gryffindor" -> rootPane.getStyleClass().add("gryffindor");
            case "Slytherin" -> rootPane.getStyleClass().add("slytherin");
            case "Ravenclaw" -> rootPane.getStyleClass().add("ravenclaw");
            case "Hufflepuff" -> rootPane.getStyleClass().add("hufflepuff");
            default -> rootPane.getStyleClass().add("hogwarts");
        }
    }

    // ----------------- Selección de casa y conexión/DAO -----------------
    private void seleccionarCasa(String casa) {
        casaActual = casa;
        if (lblCasaSeleccionada != null) lblCasaSeleccionada.setText("Casa seleccionada: " + casa);
        if (txtCasa != null) txtCasa.setText(casa);

        try (Connection conn = ConexionBD.conectarCasa(casa)) {
            if (conn != null) {
                System.out.println("Conectado a " + casa);

                // Elegir DAO dinámicamente según la casa
                switch (casa) {
                    case "Gryffindor" -> daoActual = new DerbyDAO();
                    case "Hufflepuff" -> daoActual = new H2DAO();
                    case "Slytherin" -> daoActual = new HSQLDBDAO();
                    case "Ravenclaw" -> daoActual = new OracleDAO();
                    case "Hogwarts" -> daoActual = new MariaDBDAO();
                    default -> daoActual = null;
                }

                // Si es Hogwarts, permitimos edición del campo txtCasa para introducir destino
                if ("Hogwarts".equalsIgnoreCase(casa)) {
                    if (txtCasa != null) {
                        txtCasa.setDisable(false);
                        txtCasa.setEditable(true);
                        txtCasa.clear();
                        txtCasa.setPromptText("Introduce la casa destino");
                    }
                } else {
                    if (txtCasa != null) {
                        txtCasa.setDisable(true);
                        txtCasa.setEditable(false);
                        txtCasa.setText(casa);
                    }
                }

                // Cargar los estudiantes en la tabla
                cargarEstudiantes();

            } else {
                mostrarError("No se pudo conectar con la base de datos de " + casa);
            }
        } catch (Exception e) {
            mostrarError("Error crítico al conectar con " + casa + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ----------------- CRUD (botones) -----------------
    @FXML
    void clickOnAdd(ActionEvent event) {
        if (daoActual == null) {
            mostrarError("Selecciona una casa antes de añadir un estudiante.");
            return;
        }

        try {
            Modelo_Estudiante nuevo = new Modelo_Estudiante(
                    null,
                    txtNombre.getText(),
                    txtApellidos.getText(),
                    txtCasa.getText(),
                    Integer.parseInt(txtCurso.getText()),
                    txtPatronus.getText()
            );

            boolean resultado = false;
            if (daoActual instanceof DerbyDAO dao) resultado = dao.aniadirAsync(nuevo).get();
            else if (daoActual instanceof H2DAO dao) resultado = dao.aniadirAsync(nuevo).get();
            else if (daoActual instanceof HSQLDBDAO dao) resultado = dao.aniadirAsync(nuevo).get();
            else if (daoActual instanceof OracleDAO dao) resultado = dao.aniadirAsync(nuevo).get();
            else if (daoActual instanceof MariaDBDAO dao) resultado = dao.aniadirAsync(nuevo).get();

            if (resultado) {
                mostrarInfo("Estudiante añadido correctamente a " + casaActual + ".");
                limpiarCampos();
                cargarEstudiantes();
            } else {
                mostrarError("No se pudo añadir el estudiante.");
            }

        } catch (Exception e) {
            mostrarError("Error al añadir estudiante: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void clickOnBorrar(ActionEvent event) {
        if (daoActual == null) {
            mostrarError("Selecciona una casa antes de borrar un estudiante.");
            return;
        }

        Modelo_Estudiante seleccionado = tablaEstudiantes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Selecciona un estudiante de la tabla para borrar.");
            return;
        }

        try {
            boolean resultado = false;
            if (daoActual instanceof DerbyDAO dao) resultado = dao.borrarAsync(seleccionado.getId()).get();
            else if (daoActual instanceof H2DAO dao) resultado = dao.borrarAsync(seleccionado.getId()).get();
            else if (daoActual instanceof HSQLDBDAO dao) resultado = dao.borrarAsync(seleccionado.getId()).get();
            else if (daoActual instanceof OracleDAO dao) resultado = dao.borrarAsync(seleccionado.getId()).get();
            else if (daoActual instanceof MariaDBDAO dao) resultado = dao.borrarAsync(seleccionado.getId()).get();

            if (resultado) {
                mostrarInfo("Estudiante eliminado correctamente de " + casaActual + ".");
                cargarEstudiantes();
            } else {
                mostrarError("No se pudo eliminar el estudiante.");
            }

        } catch (Exception e) {
            mostrarError("Error al borrar estudiante: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void clickOnEditar(ActionEvent event) {
        if (daoActual == null) {
            mostrarError("Selecciona una casa antes de editar un estudiante.");
            return;
        }

        Modelo_Estudiante seleccionado = tablaEstudiantes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Selecciona un estudiante de la tabla para editar.");
            return;
        }

        try {
            seleccionado.setNombre(txtNombre.getText());
            seleccionado.setApellidos(txtApellidos.getText());
            seleccionado.setCurso(Integer.parseInt(txtCurso.getText()));
            seleccionado.setPatronus(txtPatronus.getText());

            boolean resultado = false;
            if (daoActual instanceof DerbyDAO dao) resultado = dao.editarAsync(seleccionado).get();
            else if (daoActual instanceof H2DAO dao) resultado = dao.editarAsync(seleccionado).get();
            else if (daoActual instanceof HSQLDBDAO dao) resultado = dao.editarAsync(seleccionado).get();
            else if (daoActual instanceof OracleDAO dao) resultado = dao.editarAsync(seleccionado).get();
            else if (daoActual instanceof MariaDBDAO dao) resultado = dao.editarAsync(seleccionado).get();

            if (resultado) {
                mostrarInfo("Estudiante editado correctamente en " + casaActual + ".");
                cargarEstudiantes();
            } else {
                mostrarError("No se pudo editar el estudiante.");
            }

        } catch (Exception e) {
            mostrarError("Error al editar estudiante: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ----------------- Cargar estudiantes -----------------
    private void cargarEstudiantes() {
        if (daoActual == null) return;

        try {
            List<Modelo_Estudiante> estudiantes = null;
            if (daoActual instanceof DerbyDAO dao) estudiantes = dao.getAllAsync().get();
            else if (daoActual instanceof H2DAO dao) estudiantes = dao.getAllAsync().get();
            else if (daoActual instanceof HSQLDBDAO dao) estudiantes = dao.getAllAsync().get();
            else if (daoActual instanceof OracleDAO dao) estudiantes = dao.getAllAsync().get();
            else if (daoActual instanceof MariaDBDAO dao) estudiantes = dao.getAllAsync().get();

            if (estudiantes != null) {
                tablaEstudiantes.getItems().setAll(estudiantes);
            }

        } catch (InterruptedException | ExecutionException e) {
            mostrarError("Error al cargar estudiantes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ----------------- Utilidades -----------------
    private void limpiarCampos() {
        txtNombre.clear();
        txtApellidos.clear();
        txtCurso.clear();
        txtPatronus.clear();
    }

    private void mostrarError(String mensaje) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(mensaje);
            alert.showAndWait();
        });
    }

    private void mostrarInfo(String mensaje) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Información");
            alert.setHeaderText(null);
            alert.setContentText(mensaje);
            alert.showAndWait();
        });
    }

    // ----------------- Menú superior -----------------
    @FXML
    void clickOnFile(ActionEvent event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cerrar aplicación");
        confirm.setHeaderText("¿Deseas cerrar Hogwarts Manager?");
        confirm.setContentText("Se cerrarán todas las conexiones activas.");

        ButtonType btnSi = new ButtonType("Sí", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnNo = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirm.getButtonTypes().setAll(btnSi, btnNo);

        confirm.showAndWait().ifPresent(response -> {
            if (response == btnSi) {
                // Cierra los pools de hilos de todos los DAO (si implementan shutdown)
                try {
                    DerbyDAO.shutdown();
                } catch (Throwable ignored) {}
                try {
                    H2DAO.shutdown();
                } catch (Throwable ignored) {}
                try {
                    HSQLDBDAO.shutdown();
                } catch (Throwable ignored) {}
                try {
                    OracleDAO.shutdown();
                } catch (Throwable ignored) {}
                try {
                    MariaDBDAO.shutdown();
                } catch (Throwable ignored) {}
                Platform.exit();
            }
        });
    }

    @FXML
    void clickOnHelp(ActionEvent event) {
        Alert help = new Alert(Alert.AlertType.INFORMATION);
        help.setTitle("Ayuda de Hogwarts Manager");
        help.setHeaderText("¿Necesitas ayuda?");
        help.setContentText("""
                🧙‍♂️ Guía rápida:
                • Selecciona una casa en el menú desplegable.
                • Añade, edita o elimina estudiantes.
                • Los cambios se sincronizan con la base central (MariaDB).
                
                📦 Bases de datos:
                Gryffindor → Derby
                Hufflepuff → H2
                Slytherin  → HSQLDB
                Ravenclaw  → Oracle
                Hogwarts   → MariaDB
                """);
        help.showAndWait();
    }

    @FXML
    void clickOnAbout(ActionEvent event) {
        Alert about = new Alert(Alert.AlertType.INFORMATION);
        about.setTitle("Acerca de");
        about.setHeaderText("Hogwarts Database Manager");
        about.setContentText("""
                🏰 Proyecto desarrollado por:
                • Diego
                • Rubén
                • Unai
                • Gaizka

                ⚙️ Tecnologías:
                • JavaFX 23
                • JDBC
                • Maven
                • MariaDB / Oracle / H2 / Derby / HSQLDB
                """);
        about.showAndWait();
    }

    /**
     * El menú "Edit" en tu FXML original usaba onAction="#clickOnEdit".
     * Para compatibilidad, clickOnEdit delega en borrar el seleccionado (comportamiento esperado).
     */
    @FXML
    void clickOnEdit(ActionEvent event) {
        // Delegamos al borrado (el menú "Edit" tenía la opción "Delete" en tu FXML original).
        clickOnBorrar(event);
    }
}
