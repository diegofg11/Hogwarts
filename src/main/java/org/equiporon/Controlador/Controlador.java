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

import java.io.IOException;
import java.sql.Connection;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Controlador principal de la aplicación Hogwarts Manager.
 * Gestiona la interfaz, CRUD de estudiantes y sincronización entre bases de datos.
 */
public class Controlador {

    private static final Logger logger = LoggerFactory.getLogger(Controlador.class);

    // --- Elementos FXML ---
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

    // Contador para backups
    private int contadorOperaciones = 0;

    // ----------------- Inicialización -----------------

    /**
     * Inicializa la interfaz: tablas editables, ComboBox de casas,
     * colores, imágenes y conexión inicial a la base de datos.
     */
    @FXML
    private void initialize() {
        tablaEstudiantes.setEditable(true);
        ResourceBundle bundle = ResourceBundle.getBundle("i18n.messages", Locale.getDefault());

        tableNombre.setCellFactory(TextFieldTableCell.forTableColumn());
        tableApellidos.setCellFactory(TextFieldTableCell.forTableColumn());
        tablePatronus.setCellFactory(TextFieldTableCell.forTableColumn());
        tableCurso.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));

        tableNombre.setOnEditCommit(event -> {
            Modelo_Estudiante est = event.getRowValue();
            est.setNombre(event.getNewValue());
            actualizarEnBD(est);
        });

        tableApellidos.setOnEditCommit(event -> {
            Modelo_Estudiante est = event.getRowValue();
            est.setApellidos(event.getNewValue());
            actualizarEnBD(est);
        });

        tablePatronus.setOnEditCommit(event -> {
            Modelo_Estudiante est = event.getRowValue();
            est.setPatronus(event.getNewValue());
            actualizarEnBD(est);
        });

        tableCurso.setOnEditCommit(event -> {
            Modelo_Estudiante est = event.getRowValue();
            Integer nuevoCurso = event.getNewValue();

            if (nuevoCurso == null || nuevoCurso <= 0) {
                mostrarError(bundle.getString("alert.error.invalid_course"));
                event.getRowValue().setCurso(event.getOldValue());
                tablaEstudiantes.refresh();
                return;
            }

            est.setCurso(nuevoCurso);
            actualizarEnBD(est);
        });

        choiceCasas.getItems().addAll("Hogwarts", "Gryffindor", "Hufflepuff", "Ravenclaw", "Slytherin");
        choiceCasas.setValue("Hogwarts");

// Actualizar los encabezados de la tabla
        tableId.setText(bundle.getString("label.id"));
        tableNombre.setText(bundle.getString("label.nombre"));
        tableApellidos.setText(bundle.getString("label.apellidos"));
        tableCasa.setText(bundle.getString("label.casa"));
        tableCurso.setText(bundle.getString("label.curso"));
        tablePatronus.setText(bundle.getString("label.patronus"));


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

        try {
            SQLiteDAO sqlite = new SQLiteDAO();
            sqlite.hacerBackupCompleto();
        } catch (Exception ex) {
            mostrarError(bundle.getString("alert.error.backup") + ex.getMessage());
        }
    }

    // ----------------- Selección de casa -----------------

    /**
     * Selecciona la casa indicada, conecta con la base de datos correspondiente
     * y actualiza la tabla y campos de la interfaz.
     *
     * @param casa nombre de la casa seleccionada
     */
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

            cargarEstudiantes();
        } catch (Exception e) {
            mostrarError(bundle.getString("alert.error.connect_db") + casa + ": " + e.getMessage());
        }
    }


    // ----------------- CRUD -----------------

    /**
     * Añade un nuevo estudiante a la base de datos de la casa seleccionada.
     *
     * @param event evento de botón
     */
    @FXML
    void clickOnAdd(ActionEvent event) {
        ResourceBundle bundle = ResourceBundle.getBundle("i18n.messages", Locale.getDefault());

        if (daoActual == null) {
            mostrarError(bundle.getString("alert.error.no_house_selected"));
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

            contadorOperaciones++;
            if (contadorOperaciones % 2 == 0) {
                SQLiteDAO sqlite = new SQLiteDAO();
                sqlite.hacerBackupCompleto();
            }

            if (daoActual.insertarEstudiante(nuevo, false)) {
                mostrarInfo(bundle.getString("alert.info.student_added") + casaActual);
                limpiarCampos();
                cargarEstudiantes();
            } else mostrarError(bundle.getString("alert.error.add_student"));
        } catch (Exception e) {
            mostrarError(bundle.getString("alert.error.add_student") + e.getMessage());
        }
    }


    /**
     * Actualiza un estudiante editado en la tabla y sincroniza con la base de datos.
     * Muestra mensajes de éxito o error usando los textos del ResourceBundle.
     *
     * @param est estudiante a actualizar
     */
    private void actualizarEnBD(Modelo_Estudiante est) {
        ResourceBundle bundle = ResourceBundle.getBundle("i18n.messages", Locale.getDefault());
        if (daoActual == null) return;

        try {
            boolean resultado = false;
            if (daoActual instanceof DerbyDAO dao) resultado = dao.editarEstudiante(est, false);
            else if (daoActual instanceof H2DAO dao) resultado = dao.editarEstudiante(est, false);
            else if (daoActual instanceof HSQLDBDAO dao) resultado = dao.editarEstudiante(est, false);
            else if (daoActual instanceof OracleDAO dao) resultado = dao.editarEstudiante(est, false);
            else if (daoActual instanceof MariaDBDAO dao) resultado = dao.editarEstudiante(est, false);

            if (resultado) {
                contadorOperaciones++;
                if (contadorOperaciones % 2 == 0) {
                    SQLiteDAO sqlite = new SQLiteDAO();
                    sqlite.hacerBackupCompleto();
                }
                // Mensaje de éxito usando properties
                mostrarInfo(bundle.getString("alert.info.student_updated") + " " + casaActual);
            } else {
                mostrarError(bundle.getString("alert.error.update_student"));
            }
        } catch (Exception e) {
            mostrarError(bundle.getString("alert.error.update_student") + ": " + e.getMessage());
        }
    }


    /**
     * Deshace los cambios restaurando Hogwarts desde el último backup de SQLite.
     *
     * @param event evento de botón
     */
    @FXML
    private void clickOnUndo(javafx.event.ActionEvent event) {
        ResourceBundle bundle = ResourceBundle.getBundle("i18n.messages", Locale.getDefault());

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                bundle.getString("alert.confirm.restore_backup"));
        confirm.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                SQLiteDAO sqlite = new SQLiteDAO();
                sqlite.restaurarBackupEnHogwarts();
                mostrarInfo(bundle.getString("alert.info.restored_backup"));
                cargarEstudiantes();
            }
        });
    }


    /**
     * Borra el estudiante seleccionado de la base de datos.
     *
     * @param event evento de botón
     */
    @FXML
    void clickOnBorrar(ActionEvent event) {
        ResourceBundle bundle = ResourceBundle.getBundle("i18n.messages", Locale.getDefault());

        if (daoActual == null) {
            mostrarError(bundle.getString("alert.error.no_house_selected"));
            return;
        }

        Modelo_Estudiante seleccionado = tablaEstudiantes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarError(bundle.getString("alert.error.no_student_selected"));
            return;
        }

        if (daoActual.borrarEstudiante(seleccionado.getId(), false)) {
            mostrarInfo(bundle.getString("alert.info.student_deleted") + casaActual);
            cargarEstudiantes();

            contadorOperaciones++;
            if (contadorOperaciones % 2 == 0) {
                SQLiteDAO sqlite = new SQLiteDAO();
                sqlite.hacerBackupCompleto();
            }
        } else mostrarError(bundle.getString("alert.error.delete_student"));
    }

    // ----------------- Utilidades -----------------

    /** Carga todos los estudiantes de la base actual en la tabla. */
    private void cargarEstudiantes() {
        if (daoActual == null) return;

        try {
            List<Modelo_Estudiante> estudiantes = daoActual.obtenerTodos();
            tablaEstudiantes.getItems().setAll(estudiantes);
        } catch (Exception e) {
            mostrarError("Error al cargar estudiantes: " + e.getMessage());
        }
    }

    /** Limpia los campos de texto de la interfaz. */
    private void limpiarCampos() {
        txtNombre.clear();
        txtApellidos.clear();
        txtCurso.clear();
        txtPatronus.clear();
    }

    /** Muestra un mensaje de error en un Alert. */
    private void mostrarError(String mensaje) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(mensaje);
            alert.showAndWait();
        });
    }

    /** Muestra un mensaje informativo en un Alert. */
    private void mostrarInfo(String mensaje) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Información");
            alert.setHeaderText(null);
            alert.setContentText(mensaje);
            alert.showAndWait();
        });
    }

    /** Aplica las imágenes (escudo y banner) según la casa seleccionada. */
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

    /** Cambia el color de fondo de la ventana según la casa. */
    private void aplicarColorVentana(String casa) {
        if (rootPane == null) return;
        rootPane.getStyleClass().removeAll("gryffindor", "slytherin", "ravenclaw", "hufflepuff", "hogwarts");
        rootPane.getStyleClass().add(casa.toLowerCase());
    }

    /**
     * Configura el ComboBox de casas para mostrar colores personalizados.
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

    /** Devuelve el estilo CSS según la casa. */
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

    /** Maneja el cierre de la aplicación desde el menú File. */
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


    /** Muestra la ayuda de la aplicación desde el menú Help. */
    @FXML
    void clickOnHelp(ActionEvent event) {
        ResourceBundle bundle = ResourceBundle.getBundle("i18n.messages", Locale.getDefault());
        Alert help = new Alert(Alert.AlertType.INFORMATION);
        help.setTitle(bundle.getString("help.title"));          // <-- cambiado
        help.setHeaderText(bundle.getString("help.header"));
        help.setContentText(bundle.getString("help.content"));
        help.showAndWait();
    }


    /** Muestra información acerca de la aplicación desde el menú About. */
    @FXML
    void clickOnAbout(ActionEvent event) {
        ResourceBundle bundle = ResourceBundle.getBundle("i18n.messages", Locale.getDefault());
        Alert about = new Alert(Alert.AlertType.INFORMATION);
        about.setTitle(bundle.getString("about.title"));        // <-- cambiado
        about.setHeaderText(bundle.getString("about.header"));
        about.setContentText(bundle.getString("about.content"));
        about.showAndWait();
    }


    // ----------------- Gestión de idioma -----------------

    /**
     * Cambia el idioma de la interfaz según el menú seleccionado.
     *
     * @param event evento del menú
     */
    @FXML
    private void cambiarIdioma(ActionEvent event) {
        MenuItem source = (MenuItem) event.getSource();
        Locale locale;

        switch (source.getId()) {
            case "menuEspanol" -> locale = new Locale("es", "ES");
            case "menuIngles" -> locale = new Locale("en", "US");
            case "menuParsel" -> locale = new Locale("la");
            default -> locale = Locale.getDefault();
        }

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



    /** Actualiza todos los textos de la interfaz con el ResourceBundle proporcionado. */
    private void actualizarTextos(ResourceBundle bundle) {
        // --- Labels principales ---
        lblNombre.setText(bundle.getString("label.nombre"));
        lblApellidos.setText(bundle.getString("label.apellidos"));
        lblCurso.setText(bundle.getString("label.curso"));
        lblPatronus.setText(bundle.getString("label.patronus"));
        lblCasa.setText(bundle.getString("label.casa"));
        lblId.setText(bundle.getString("label.id"));

        // --- Botones ---
        botAdd.setText(bundle.getString("button.add"));
        botBorrar.setText(bundle.getString("button.delete"));
        botDeshacer.setText(bundle.getString("button.undo"));

        // --- Menús ---
        menuFile.setText(bundle.getString("menu.file"));
        menuHelp.setText(bundle.getString("menu.help"));
        menuLanguage.setText(bundle.getString("menu.language"));

        menuItemClose.setText(bundle.getString("menuitem.close"));
        menuItemHelp.setText(bundle.getString("menuitem.help"));
        menuItemAbout.setText(bundle.getString("menuitem.about"));
        menuEspanol.setText(bundle.getString("menuitem.espanol"));
        menuIngles.setText(bundle.getString("menuitem.english"));
        menuParsel.setText(bundle.getString("menuitem.parsel"));

        // --- Encabezados de la tabla ---
        tableId.setText(bundle.getString("label.id"));
        tableNombre.setText(bundle.getString("label.nombre"));
        tableApellidos.setText(bundle.getString("label.apellidos"));
        tableCasa.setText(bundle.getString("label.casa"));
        tableCurso.setText(bundle.getString("label.curso"));
        tablePatronus.setText(bundle.getString("label.patronus"));
    }


}
