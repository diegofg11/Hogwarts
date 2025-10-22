package org.equiporon.Modelo;

/**
 * Clase de modelo que representa un estudiante dentro de la aplicación.
 *
 *
 * @author Rubén
 * @version 1.0
 */
public class Modelo_Estudiante {

    /** Identificador único de la persona (clave primaria en la base de datos). */
    private String id;

    /** Nombre de la persona. */
    private String nombre;

    /** Apellidos de la persona. */
    private String apellidos;

    /** Casa de la persona. */
    private String casa;

    /** Curso del alumno. */
    private int curso;

    /** Patronus del alumno. */
    private String patronus;


    /**
     * Crea una nueva instancia de {@code Modelo_Estudiante} con los datos especificados.
     *
     * @param id identificador único de la persona (usualmente asignado por la base de datos).
     * @param nombre nombre de la persona.
     * @param apellidos apellido de la persona.
     * @param casa casa del alumno de Hogwarts.
     * @param curso curso del alumno de Hogwarts.
     * @param patronus (animal espiritual) patronus del alumno de Hogwarts.
     *
     * @author Rubén
     */
    public Modelo_Estudiante(String id, String nombre, String apellidos, String casa, int curso, String patronus) {
        this.id = id;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.casa = casa;
        this.curso = curso;
        this.patronus = patronus;
    }

    //Setters

    /**
     * Establece o modifica el identificador único del objeto.
     * @param id El nuevo identificador numérico.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Establece o modifica el nombre.
     * @param nombre El nuevo nombre.
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Establece o modifica los apellidos.
     * @param apellidos Los nuevos apellidos.
     */
    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    /**
     * Establece o modifica la casa de Hogwarts a la que pertenece.
     * @param casa La nueva casa (ej. "Gryffindor", "Slytherin").
     */
    public void setCasa(String casa) {
        this.casa = casa;
    }

    /**
     * Establece o modifica el curso actual.
     * @param curso El nuevo número de curso.
     */
    public void setCurso(int curso) {
        this.curso = curso;
    }

    /**
     * Establece o modifica el Patronus.
     * @param patronus El nuevo Patronus.
     */
    public void setPatronus(String patronus) {
        this.patronus = patronus;
    }

//Getters

    /**
     * Obtiene el identificador único.
     * @return El identificador del objeto.
     */
    public String getId() {
        return id;
    }

    /**
     * Obtiene el nombre.
     * @return El nombre actual.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Obtiene los apellidos.
     * @return Los apellidos actuales.
     */
    public String getApellidos() {
        return apellidos;
    }

    /**
     * Obtiene la casa de Hogwarts.
     * @return La casa a la que pertenece.
     */
    public String getCasa() {
        return casa;
    }

    /**
     * Obtiene el curso actual.
     * @return El número del curso actual.
     */
    public Integer getCurso() {
        return curso;
    }

    /**
     * Obtiene el Patronus.
     * @return El Patronus actual.
     */
    public String getPatronus() {
        return patronus;
    }

    /**
     * Devuelve una representación legible del objeto {@code Estudiante}.
     *
     * @return cadena de texto con formato.
     *
     * @author Rubén
     */
    @Override
    public String toString() {
        return nombre + " " + apellidos + " ( casa: " + casa + " ,curso: " +  curso + " ,patronus: " + patronus + ")";
    }
}