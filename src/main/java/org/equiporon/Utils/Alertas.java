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

    /**
     * Muestra un mensaje de información al usuario en una ventana de diálogo.
     *
     * <p>Este metodo es un atajo para llamar a {@code mostrar(Alert.AlertType.INFORMATION, titulo, mensaje)}.
     * Es útil para notificar al usuario sobre el éxito de una operación,
     * proporcionar detalles informativos o cualquier otro tipo de aviso
     * que no sea un error o una advertencia.</p>
     *
     * @author Gaizka
     * @param titulo  El título de la ventana de diálogo (barra de título y encabezado de la alerta).
     * @param mensaje El contenido principal del mensaje a mostrar al usuario.
     */
    public static void mostrarInfo(String titulo, String mensaje) {
        mostrar(Alert.AlertType.INFORMATION, titulo, mensaje);
    }

    /**
     * Muestra un mensaje de advertencia al usuario en una ventana de diálogo. ⚠️
     *
     * <p>Este metodo es un atajo para llamar a {@code mostrar(Alert.AlertType.WARNING, titulo, mensaje)}.
     * Se utiliza para informar al usuario sobre posibles problemas, situaciones
     * que requieren atención pero no son errores críticos, o acciones que podrían
     * tener consecuencias no deseadas si continúan.</p>
     *
     * @author Gaizka
     * @param titulo  El título de la ventana de diálogo (barra de título y encabezado de la alerta).
     * @param mensaje El contenido principal del mensaje de advertencia a mostrar.
     */
    public static void mostrarWarning(String titulo, String mensaje) {
        mostrar(Alert.AlertType.WARNING, titulo, mensaje);
    }

    /**
     * Muestra un mensaje de error crítico al usuario en una ventana de diálogo. ❌
     *
     * <p>Este metodo es un atajo para llamar a {@code mostrar(Alert.AlertType.ERROR, titulo, mensaje)}.
     * Se utiliza para notificar al usuario que ha ocurrido un fallo irrecuperable,
     * que una operación ha terminado de forma inesperada o que ha habido un problema
     * que impide el normal funcionamiento de la aplicación o de la tarea en curso.</p>
     *
     *
     * @author Gaizka
     * @param titulo  El título de la ventana de diálogo (barra de título y encabezado de la alerta).
     * @param mensaje El contenido principal del mensaje de error a mostrar. Este debe
     * explicar la causa o la naturaleza del fallo.
     */
    public static void mostrarError(String titulo, String mensaje) {
        mostrar(Alert.AlertType.ERROR, titulo, mensaje);
    }

    /**
     * Muestra una ventana de diálogo ({@code Alert}) genérica del tipo especificado.
     *
     * <p>Este metodo es la implementación base utilizada por los métodos de conveniencia
     * como {@code mostrarInfo}, {@code mostrarWarning} y {@code mostrarError}.
     * Crea una nueva instancia de {@code Alert} con el tipo, título y contenido
     * proporcionados, y luego espera a que el usuario interactúe con ella.</p>
     *
     * @param tipo    El {@code Alert.AlertType} que define el icono y la naturaleza de la alerta
     * (p. ej., {@code INFORMATION}, {@code WARNING}, {@code ERROR}).
     * @param titulo  El texto que se mostrará en la barra de título de la ventana y
     * como encabezado de la alerta.
     * @param mensaje El texto principal del contenido de la alerta.
     * @see #mostrarInfo(String, String)
     * @see #mostrarWarning(String, String)
     * @see #mostrarError(String, String)
     * @author Gaizka
     */
    private static void mostrar(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    /**
     * Muestra una ventana de diálogo de confirmación (Sí/No) al usuario. ✅
     *
     * <p>Esta función presenta una alerta con opciones predeterminadas (típicamente "Aceptar" y "Cancelar")
     * y espera la interacción del usuario. Es ideal para solicitar la aprobación
     * del usuario antes de ejecutar una acción potencialmente destructiva o irreversible.</p>
     *
     * @param titulo  El título de la ventana de diálogo (barra de título).
     * @param mensaje El contenido principal de la pregunta de confirmación que se muestra al usuario.
     * @return {@code true} si el usuario selecciona el botón de **confirmación/aceptación**
     * (mapeado a {@code ButtonType.OK}); {@code false} en caso contrario (p. ej., si selecciona
     * "Cancelar" o cierra el diálogo).
     * @author Gaizka
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

