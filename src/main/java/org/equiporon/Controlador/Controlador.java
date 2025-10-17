package org.equiporon.Controlador;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
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

    @FXML private Label lblCasaSeleccionada;

    private String casaActual = null;

    /**
     * Este metodo se llama automáticamente después de que el archivo fxml ha sido cargado.
     * Lo usamos para establecer un estado inicial, conectando a Hogwarts por defecto.
     */
    @FXML
    private void initialize() {
        // Llama al metodo para seleccionar Hogwarts al iniciar la aplicación
        seleccionarCasa("Hogwarts");
    }

    @FXML
    void clickGryffindor(ActionEvent event) {
        seleccionarCasa("Gryffindor");
    }

    @FXML
    void clickHufflepuff(ActionEvent event) {
        seleccionarCasa("Hufflepuff");
    }

    @FXML
    void clickRavenclaw(ActionEvent event) {
        seleccionarCasa("Ravenclaw");
    }

    @FXML
    void clickSlytherin(ActionEvent event) {
        seleccionarCasa("Slytherin");
    }

    @FXML
    void clickHogwarts(ActionEvent event) {
        seleccionarCasa("Hogwarts");
    }

    /**
     * Lógica común para conectarse a la base de datos según la casa seleccionada.
     */
    private void seleccionarCasa(String casa) {
        casaActual = casa;
        lblCasaSeleccionada.setText("Casa seleccionada: " + casa);

        try (Connection conn = ConexionBD.conectarCasa(casa)) {
            if (conn != null) {
                mostrarInfo("Conectado correctamente a " + casa + " 🧙‍♂️");
                System.out.println("Conectado a " + casa);
            } else {
                mostrarError("Error al conectar con " + casa);
            }
        } catch (Exception e) {
            mostrarError("Error al conectar con " + casa + ": " + e.getMessage());
        }
    }

    private void mostrarInfo(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Conexión exitosa");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error de conexión");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}