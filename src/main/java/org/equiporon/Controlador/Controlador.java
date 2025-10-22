package org.equiporon.Controlador;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.util.converter.IntegerStringConverter;
import org.equiporon.Conexion.ConexionBD;
import org.equiporon.DAO.*;
import org.equiporon.Modelo.Modelo_Estudiante;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.List;

public class Controlador {

    private static final Logger logger = LoggerFactory.getLogger(Controlador.class);

    // --- Elementos FXML ---
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

    // Estado
    private String casaActual = null;
    private BaseDAO daoActual = null;

    // ----------------- Inicialización -----------------
    @FXML
    private void initialize() {
        tablaEstudiantes.setEditable(true);

        tableNombre.setCellFactory(TextFieldTableCell.forTableColumn());
        tableApellidos.setCellFactory(TextFieldTableCell.forTableColumn());
        tablePatronus.setCellFactory(TextFieldTableCell.forTableColumn());
        tableCurso.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));

        tableNombre.setOnEditCommit(e -> e.getRowValue().setNombre(e.getNewValue()));
        tableApellidos.setOnEditCommit(e -> e.getRowValue().setApellidos(e.getNewValue()));
        tablePatronus.setOnEditCommit(e -> e.getRowValue().setPatronus(e.getNewValue()));
        tableCurso.setOnEditCommit(e -> {
            if (e.getNewValue() != null && e.getNewValue() > 0)
                e.getRowValue().setCurso(e.getNewValue());
            else {
                mostrarError("El curso debe ser un número positivo.");
                tablaEstudiantes.refresh();
            }
        });

        choiceCasas.getItems().addAll("Hogwarts", "Gryffindor", "Hufflepuff", "Ravenclaw", "Slytherin");
        choiceCasas.setValue("Hogwarts");

        tableId.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getId()));
        tableNombre.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNombre()));
        tableApellidos.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getApellidos()));
        tableCasa.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getCasa()));
        tableCurso.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getCurso()).asObject());
        tablePatronus.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getPatronus()));

        choiceCasas.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                seleccionarCasa(newVal);
                aplicarColorVentana(newVal);
                aplicarImagenesCasa(newVal);
            }
        });

        setupComboBoxColors();
        aplicarColorVentana("Hogwarts");
        aplicarImagenesCasa("Hogwarts");
        seleccionarCasa("Hogwarts");
    }

    // ----------------- Selección de casa -----------------
    private void seleccionarCasa(String casa) {
        casaActual = casa;
        txtCasa.setText(casa);

        try (Connection conn = ConexionBD.conectarCasa(casa)) {
            if (conn == null) {
                mostrarError("No se pudo conectar con la base de datos de " + casa);
                return;
            }

            switch (casa) {
                case "Gryffindor" -> daoActual = new DerbyDAO();
                case "Hufflepuff" -> daoActual = new H2DAO();
                case "Slytherin" -> daoActual = new HSQLDBDAO();
                case "Ravenclaw" -> daoActual = new OracleDAO();
                case "Hogwarts" -> daoActual = new MariaDBDAO();
            }

            if ("Hogwarts".equalsIgnoreCase(casa)) {
                txtCasa.setEditable(true);
                txtCasa.setPromptText("Introduce la casa destino");
            } else {
                txtCasa.setEditable(false);
            }

            cargarEstudiantes();
        } catch (Exception e) {
            mostrarError("Error al conectar con " + casa + ": " + e.getMessage());
        }
    }

    // ----------------- CRUD -----------------
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

            if (daoActual.insertarEstudiante(nuevo, false)) {
                mostrarInfo("✅ Estudiante añadido correctamente a " + casaActual + ".");
                limpiarCampos();
                cargarEstudiantes();
            } else mostrarError("No se pudo añadir el estudiante.");

        } catch (Exception e) {
            mostrarError("Error al añadir estudiante: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void actualizarBD(ActionEvent event) {
        if (daoActual == null) {
            mostrarError("Selecciona una casa antes de actualizar.");
            return;
        }

        Modelo_Estudiante seleccionado = tablaEstudiantes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError("Selecciona un estudiante para actualizar.");
            return;
        }

        if (daoActual.editarEstudiante(seleccionado, false))
            mostrarInfo("✏️ Cambios guardados correctamente en " + casaActual + ".");
        else mostrarError("No se pudo actualizar el estudiante.");
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

        if (daoActual.borrarEstudiante(seleccionado.getId(), false)) {
            mostrarInfo("🗑️ Estudiante eliminado correctamente de " + casaActual + ".");
            cargarEstudiantes();
        } else mostrarError("No se pudo eliminar el estudiante.");
    }

    // ----------------- Cargar estudiantes -----------------
    private void cargarEstudiantes() {
        if (daoActual == null) return;

        try {
            List<Modelo_Estudiante> estudiantes = daoActual.obtenerTodos();
            tablaEstudiantes.getItems().setAll(estudiantes);
        } catch (Exception e) {
            mostrarError("Error al cargar estudiantes: " + e.getMessage());
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

    // ----------------- Imagen y estilo (igual que antes) -----------------
    private void aplicarImagenesCasa(String casa) {
        String basePath = "/images/";
        String nombre = casa.toLowerCase();

        try {
            Image escudo = new Image(getClass().getResourceAsStream(basePath + nombre + "_escudo.png"));
            Image banner = new Image(getClass().getResourceAsStream(basePath + nombre + "_banner.png"));
            escudoCasa.setImage(escudo);
            bannerIzquierdo.setImage(banner);
            bannerDerecho.setImage(banner);
        } catch (Exception ignored) {}
    }

    private void aplicarColorVentana(String casa) {
        if (rootPane == null) return;
        rootPane.getStyleClass().removeAll("gryffindor", "slytherin", "ravenclaw", "hufflepuff", "hogwarts");
        rootPane.getStyleClass().add(casa.toLowerCase());
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
            default -> "-fx-background-color: #000000; -fx-text-fill: #FFD700;";
        };
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
                try { DerbyDAO.shutdown(); } catch (Throwable ignored) {}
                try { H2DAO.shutdown(); } catch (Throwable ignored) {}
                try { HSQLDBDAO.shutdown(); } catch (Throwable ignored) {}
                try { OracleDAO.shutdown(); } catch (Throwable ignored) {}
                try { MariaDBDAO.shutdown(); } catch (Throwable ignored) {}
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
            Gryffindor       → Derby
            Hufflepuff       → H2
            Slytherin        → HSQLDB
            Ravenclaw        → Oracle
            Hogwarts         → MariaDB
            Hogwarts (local) → SQLite
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
            • Xiker
            • Igor

            ⚙️ Tecnologías:
            • JavaFX 23
            • JDBC
            • Maven
            • MariaDB / Oracle / H2 / Derby / HSQLDB / SQLite
            """);
        about.showAndWait();
    }

}
