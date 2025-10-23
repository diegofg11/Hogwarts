package org.equiporon.Utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;

import java.util.Optional;

/**
 * Clase utilitaria para mostrar alertas reutilizables en toda la aplicación.
 * Puede ser llamada desde cualquier DAO, controlador o clase lógica.
 *
 * Ejemplo de uso:
 *   Alertas.mostrarInfo("Conexión exitosa", "Conectado correctamente a Gryffindor.");
 *   Alertas.mostrarError("Error de conexión", "No se pudo conectar a la base de datos.");
 *   @author Gaizka
 */
public class Alertas {

    /** Muestra un mensaje de información */
    public static void mostrarInfo(String titulo, String mensaje) {
        mostrar(Alert.AlertType.INFORMATION, titulo, mensaje);
    }

    /** Muestra un mensaje de advertencia */
    public static void mostrarWarning(String titulo, String mensaje) {
        mostrar(Alert.AlertType.WARNING, titulo, mensaje);
    }

    /** Muestra un mensaje de error */
    public static void mostrarError(String titulo, String mensaje) {
        mostrar(Alert.AlertType.ERROR, titulo, mensaje);
    }

    /** Muestra una alerta genérica del tipo indicado */
    private static void mostrar(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    /**
     * Muestra una confirmación (Sí/No) y devuelve true si el usuario confirma.
     */
    public static boolean mostrarConfirmacion(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);

        Optional<ButtonType> resultado = alerta.showAndWait();
        return resultado.isPresent() && resultado.get() == ButtonType.OK;
    }
}

