
package org.equiporon.Controlador;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.equiporon.Conexion.ConexionBD;
import org.equiporon.DAO.*;
import org.equiporon.Modelo.Modelo_Estudiante;

import java.sql.Connection;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Controlador principal del proyecto Hogwarts.
 * Permite seleccionar una casa desde el menú y conectarse
 * a la base de datos correspondiente, con operaciones CRUD.
 *
 * @author
 * Diego, Rubén, Unai, Gaizka
 */
public class Controlador {

    // --- Elementos FXML ---
    @FXML private ComboBox<String> choiceCasas;
    @FXML private Button botAdd;
    @FXML private Button botBorrar;
    @FXML private Button botEditar;
    @FXML private TableView<Modelo_Estudiante> tablaEstudiantes;
    @FXML private TableColumn<Modelo_Estudiante, String> tableId;
    @FXML private TableColumn<Modelo_Estudiante, String> tableNombre;
    @FXML private TableColumn<Modelo_Estudiante, String> tableApellidos;
    @FXML private TableColumn<Modelo_Estudiante, String> tableCasa;
    @FXML private TableColumn<Modelo_Estudiante, Integer> tableCurso;
    @FXML private TableColumn<Modelo_Estudiante, String> tablePatronus;
    @FXML private TextField txtNombre;
    @FXML private TextField txtApellidos;
    @FXML private TextField txtCasa;
    @FXML private TextField txtCurso;
    @FXML private TextField txtPatronus;

    private String casaActual = null;
    private Object daoActual = null;

    // ==========================================================
    //                METODO DE INICIALIZACIÓN
    // ==========================================================
    @FXML
    private void initialize() {
        choiceCasas.getItems().addAll("Hogwarts", "Gryffindor", "Hufflepuff", "Ravenclaw", "Slytherin");
        choiceCasas.setValue("Hogwarts");
        txtCasa.setText("Hogwarts");
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
                    }
                }
        );
    }

    // ==========================================================
    //              CONEXIÓN Y SELECCIÓN DE CASA
    // ==========================================================
    private void seleccionarCasa(String casa) {
        casaActual = casa;
        txtCasa.setText(casa);

        try (Connection conn = ConexionBD.conectarCasa(casa)) {
            if (conn != null) {
                System.out.println("Conectado a " + casa);

                // DAO dinámico según la casa
                switch (casa) {
                    case "Gryffindor" -> daoActual = new DerbyDAO();
                    case "Hufflepuff" -> daoActual = new H2DAO();
                    case "Slytherin" -> daoActual = new HSQLDBDAO();
                    case "Ravenclaw" -> daoActual = new OracleDAO();
                    case "Hogwarts" -> daoActual = new MariaDBDAO();
                    default -> daoActual = null;
                }
                if ("Hogwarts".equalsIgnoreCase(casa)) {
                    txtCasa.setDisable(false); // Activar el campo
                    txtCasa.setEditable(true);
                    txtCasa.clear();
                    txtCasa.setPromptText("Introduce la casa destino");
                } else {
                    txtCasa.setDisable(true);
                    txtCasa.setEditable(false);
                    txtCasa.setText(casa);
                }
                // Cargar estudiantes automáticamente
                cargarEstudiantes();

            } else {
                mostrarError("No se pudo conectar con la base de datos de " + casa);
            }
        } catch (Exception e) {
            mostrarError("Error crítico al conectar con " + casa + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==========================================================
    //                     BOTÓN: AÑADIR
    // ==========================================================
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

    // ==========================================================
    //                     BOTÓN: BORRAR
    // ==========================================================
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

    // ==========================================================
    //                     BOTÓN: EDITAR
    // ==========================================================
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

    // ==========================================================
    //                CARGAR ESTUDIANTES EN TABLA
    // ==========================================================
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

    // ==========================================================
    //                  MÉTODOS DE UTILIDAD
    // ==========================================================
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
    // ==========================================================
//                  MENÚ SUPERIOR (File / Edit / Help)
// ==========================================================

    /**
     * Opción del menú "File" → Cierra la aplicación.
     */
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
                // Cierra los pools de hilos de todos los DAO
                DerbyDAO.shutdown();
                H2DAO.shutdown();
                HSQLDBDAO.shutdown();
                OracleDAO.shutdown();
                // Finaliza la aplicación
                Platform.exit();
            }
        });
    }
    /**
     * Opción del menú "Help" → Muestra información del programa.
     */
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

    /**
     * Opción del menú "About" → Muestra créditos del proyecto.
     */
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

}

