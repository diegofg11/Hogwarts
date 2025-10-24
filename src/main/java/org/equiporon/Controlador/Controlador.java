package org.equiporon.Controlador;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class Controlador {

    private static final Logger logger = LoggerFactory.getLogger(Controlador.class);

    // --- FXML ---
    @FXML private AnchorPane rootPane;
    @FXML private Label lblCasaSeleccionada;
    @FXML private ComboBox<String> choiceCasas;
    @FXML private Button botAdd;
    @FXML private Button botBorrar;
    @FXML private Button botDeshacer;
    @FXML private Label lblCasa;
    @FXML private Label lblId;
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

    // Menú e idioma
    @FXML private Menu menuFile;
    @FXML private Menu menuHelp;
    @FXML private Menu menuLanguage;
    @FXML private MenuItem menuItemClose;
    @FXML private MenuItem menuItemHelp;
    @FXML private MenuItem menuItemAbout;
    @FXML private MenuItem menuEspanol;
    @FXML private MenuItem menuIngles;
    @FXML private MenuItem menuParsel;
    @FXML private Label lblNombre;
    @FXML private Label lblApellidos;
    @FXML private Label lblCurso;
    @FXML private Label lblPatronus;

    // Estado
    private String casaActual = null;
    private BaseDAO daoActual = null;

    // ----------------- Inicialización -----------------
    @FXML
    private void initialize() {
        ResourceBundle bundle = ResourceBundle.getBundle("i18n.messages", Locale.getDefault());

        tablaEstudiantes.setEditable(true);
        tableNombre.setCellFactory(TextFieldTableCell.forTableColumn());
        tableApellidos.setCellFactory(TextFieldTableCell.forTableColumn());
        tablePatronus.setCellFactory(TextFieldTableCell.forTableColumn());
        tableCurso.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));

        // Encabezados de tabla i18n
        tableId.setText(bundle.getString("label.id"));
        tableNombre.setText(bundle.getString("label.nombre"));
        tableApellidos.setText(bundle.getString("label.apellidos"));
        tableCasa.setText(bundle.getString("label.casa"));
        tableCurso.setText(bundle.getString("label.curso"));
        tablePatronus.setText(bundle.getString("label.patronus"));

        // Factories
        tableId.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getId()));
        tableNombre.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNombre()));
        tableApellidos.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getApellidos()));
        tableCasa.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getCasa()));
        tableCurso.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getCurso()).asObject());
        tablePatronus.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getPatronus()));

        // Edición inline
        tableNombre.setOnEditCommit(ev -> { ev.getRowValue().setNombre(ev.getNewValue()); actualizarEnBDAsync(ev.getRowValue()); });
        tableApellidos.setOnEditCommit(ev -> { ev.getRowValue().setApellidos(ev.getNewValue()); actualizarEnBDAsync(ev.getRowValue()); });
        tablePatronus.setOnEditCommit(ev -> { ev.getRowValue().setPatronus(ev.getNewValue()); actualizarEnBDAsync(ev.getRowValue()); });
        tableCurso.setOnEditCommit(ev -> {
            Integer nuevoCurso = ev.getNewValue();
            if (nuevoCurso == null || nuevoCurso <= 0) {
                mostrarError(bundle.getString("alert.error.invalid_course"));
                ev.getRowValue().setCurso(ev.getOldValue());
                tablaEstudiantes.refresh();
                return;
            }
            ev.getRowValue().setCurso(nuevoCurso);
            actualizarEnBDAsync(ev.getRowValue());
        });

        // Casas + UI
        choiceCasas.getItems().addAll("Hogwarts", "Gryffindor", "Hufflepuff", "Ravenclaw", "Slytherin");
        choiceCasas.setValue("Hogwarts");
        choiceCasas.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                seleccionarCasa(newV);
                aplicarColorVentana(newV);
                aplicarImagenesCasa(newV);
            }
        });

        setupComboBoxColors();
        aplicarColorVentana("Hogwarts");
        aplicarImagenesCasa("Hogwarts");
        seleccionarCasa("Hogwarts");

        // Backup inicial (si lo usas)
        try {
            SQLiteDAO sqlite = new SQLiteDAO();
            sqlite.hacerBackupCompleto();
        } catch (Exception ex) {
            mostrarError(bundle.getString("alert.error.backup") + ex.getMessage());
        }
    }

    // ----------------- Selección de casa -----------------
    private void seleccionarCasa(String casa) {
        ResourceBundle bundle = ResourceBundle.getBundle("i18n.messages", Locale.getDefault());
        casaActual = casa;
        txtCasa.setText(casa);

        try (Connection conn = ConexionBD.conectarCasa(casa)) {
            if (conn == null) {
                mostrarError(bundle.getString("alert.error.connect_db") + casa);
                return;
            }

            switch (casa) {
                case "Gryffindor" -> daoActual = new DerbyDAO();
                case "Hufflepuff" -> daoActual = new H2DAO();
                case "Slytherin" -> daoActual = new HSQLDBDAO();
                case "Ravenclaw" -> daoActual = new OracleDAO();
                case "Hogwarts" -> daoActual = new MariaDBDAO();
            }

            txtCasa.setEditable("Hogwarts".equalsIgnoreCase(casa));
            if ("Hogwarts".equalsIgnoreCase(casa)) txtCasa.setPromptText(bundle.getString("label.casa"));

            cargarEstudiantesAsync();
        } catch (Exception e) {
            mostrarError(bundle.getString("alert.error.connect_db") + casa + ": " + e.getMessage());
        }
    }

    // ----------------- CRUD (ASÍNCRONO) -----------------
    @FXML
    void clickOnAdd(ActionEvent event) {
        ResourceBundle bundle = ResourceBundle.getBundle("i18n.messages", Locale.getDefault());
        if (daoActual == null) { mostrarError(bundle.getString("alert.error.no_house_selected")); return; }

        try {
            Modelo_Estudiante nuevo = new Modelo_Estudiante(
                    null,
                    txtNombre.getText(),
                    txtApellidos.getText(),
                    txtCasa.getText(),
                    Integer.parseInt(txtCurso.getText()),
                    txtPatronus.getText()
            );

            new SQLiteDAO().hacerBackupInstantaneo();

            daoActual.insertarAsync(nuevo)
                    .thenAccept(ok -> Platform.runLater(() -> {
                        if (ok) {
                            mostrarInfo(bundle.getString("alert.info.student_added") + casaActual);
                            limpiarCampos();
                            cargarEstudiantesAsync();
                        } else {
                            mostrarError(bundle.getString("alert.error.add_student"));
                        }
                    }))
                    .exceptionally(ex -> {
                        Platform.runLater(() -> mostrarError(bundle.getString("alert.error.add_student") + ex.getMessage()));
                        return null;
                    });

        } catch (Exception e) {
            mostrarError(bundle.getString("alert.error.add_student") + e.getMessage());
        }
    }

    private void actualizarEnBDAsync(Modelo_Estudiante est) {
        ResourceBundle bundle = ResourceBundle.getBundle("i18n.messages", Locale.getDefault());
        if (daoActual == null) return;

        new SQLiteDAO().hacerBackupInstantaneo();

        daoActual.editarAsync(est)
                .thenAccept(ok -> Platform.runLater(() -> {
                    if (ok) {
                        logger.info("✅ {}", bundle.getString("alert.info.student_updated") + " " + casaActual);
                    } else {
                        mostrarError(bundle.getString("alert.error.update_student"));
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> mostrarError(bundle.getString("alert.error.update_student") + ": " + ex.getMessage()));
                    return null;
                });
    }

    @FXML
    void clickOnBorrar(ActionEvent event) {
        ResourceBundle bundle = ResourceBundle.getBundle("i18n.messages", Locale.getDefault());
        if (daoActual == null) { mostrarError(bundle.getString("alert.error.no_house_selected")); return; }

        Modelo_Estudiante sel = tablaEstudiantes.getSelectionModel().getSelectedItem();
        if (sel == null) { mostrarError(bundle.getString("alert.error.no_student_selected")); return; }

        new SQLiteDAO().hacerBackupInstantaneo();

        daoActual.borrarAsync(sel.getId())
                .thenAccept(ok -> Platform.runLater(() -> {
                    if (ok) {
                        mostrarInfo(bundle.getString("alert.info.student_deleted") + casaActual);
                        cargarEstudiantesAsync();
                    } else {
                        mostrarError(bundle.getString("alert.error.delete_student"));
                    }
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> mostrarError(bundle.getString("alert.error.delete_student") + ": " + ex.getMessage()));
                    return null;
                });
    }

    @FXML
    private void clickOnUndo(ActionEvent event) {
        ResourceBundle bundle = ResourceBundle.getBundle("i18n.messages", Locale.getDefault());
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, bundle.getString("alert.confirm.restore_backup"));
        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                SQLiteDAO sqlite = new SQLiteDAO();
                sqlite.restaurarBackupEnHogwarts(casaActual);
                mostrarInfo(bundle.getString("alert.info.restored_backup"));
                cargarEstudiantesAsync();
            }
        });
    }

    // ----------------- Carga (ASÍNCRONA) -----------------
    private void cargarEstudiantesAsync() {
        if (daoActual == null) return;

        daoActual.obtenerTodosAsync()
                .thenAccept(lista -> Platform.runLater(() -> tablaEstudiantes.getItems().setAll(lista)))
                .exceptionally(ex -> {
                    Platform.runLater(() -> mostrarError("Error al cargar estudiantes: " + ex.getMessage()));
                    return null;
                });
    }

    // ----------------- Utilidades UI -----------------
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
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); }
                else { setText(item); setStyle(getCasaColorStyle(item)); }
            }
        });
        choiceCasas.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); }
                else { setText(item); setStyle(getCasaColorStyle(item)); }
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
        ResourceBundle bundle = ResourceBundle.getBundle("i18n.messages", Locale.getDefault());
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(bundle.getString("file.title"));
        confirm.setHeaderText(bundle.getString("file.header"));
        confirm.setContentText(bundle.getString("file.content"));

        ButtonType btnSi = new ButtonType(bundle.getString("file.yes"), ButtonBar.ButtonData.OK_DONE);
        ButtonType btnNo = new ButtonType(bundle.getString("file.no"), ButtonBar.ButtonData.CANCEL_CLOSE);
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
        ResourceBundle bundle = ResourceBundle.getBundle("i18n.messages", Locale.getDefault());
        Alert help = new Alert(Alert.AlertType.INFORMATION);
        help.setTitle(bundle.getString("help.title"));
        help.setHeaderText(bundle.getString("help.header"));
        help.setContentText(bundle.getString("help.content"));
        help.showAndWait();
    }

    @FXML
    void clickOnAbout(ActionEvent event) {
        ResourceBundle bundle = ResourceBundle.getBundle("i18n.messages", Locale.getDefault());
        Alert about = new Alert(Alert.AlertType.INFORMATION);
        about.setTitle(bundle.getString("about.title"));
        about.setHeaderText(bundle.getString("about.header"));
        about.setContentText(bundle.getString("about.content"));
        about.showAndWait();
    }

    // ----------------- Cambiar idioma -----------------
    @FXML
    private void cambiarIdioma(ActionEvent event) {
        MenuItem source = (MenuItem) event.getSource();
        Locale locale = switch (source.getId()) {
            case "menuEspanol" -> new Locale("es", "ES");
            case "menuIngles" -> new Locale("en", "US");
            case "menuParsel" -> new Locale("la");
            default -> Locale.getDefault();
        };

        Locale.setDefault(locale);

        try {
            ResourceBundle bundle = ResourceBundle.getBundle("i18n.messages", locale);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/primary.fxml"), bundle);
            Parent newRoot = loader.load();
            Scene scene = rootPane.getScene();
            scene.setRoot(newRoot);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
