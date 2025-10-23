package org.equiporon.Utils;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Clase utilitaria para internacionalización (i18n).
 * <p>
 * Permite cambiar el idioma de la aplicación y obtener los textos traducidos
 * desde los archivos de propiedades (messages_xx.properties).
 * </p>
 *
 * Autores:
 *  - Unai
 *  - Xiker (modificador)
 */
public class I18n {
    private static Locale locale = Locale.getDefault();
    private static ResourceBundle bundle = ResourceBundle.getBundle("i18n/messages", locale);

    /**
     * Obtiene el texto correspondiente a la clave en el idioma actual.
     *
     * @param key la clave del texto en el archivo de propiedades.
     * @return el texto traducido.
     */
    public static String t(String key) {
        return bundle.getString(key);
    }

    /**
     * Cambia el idioma actual y recarga el ResourceBundle correspondiente.
     *
     * @param l el {@link Locale} que se desea usar.
     */
    public static void setLocale(Locale l) {
        locale = l;
        bundle = ResourceBundle.getBundle("i18n/messages", locale);
    }

    /**
     * Obtiene el ResourceBundle actual (para pasar al FXMLLoader).
     *
     * @return el {@link ResourceBundle} actual.
     */
    public static ResourceBundle getBundle() {
        return bundle;
    }
}
