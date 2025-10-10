package org.equiporon.Modelo;

/**
 * Clase de modelo que representa un estudiante dentro de la aplicación.
 *
 *
 * @author Rubén
 * @version 1.0
 */
public class Estudiante {

    /** Identificador único de la persona (clave primaria en la base de datos). */
    private int id;

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
     */
    public Estudiante(int id, String nombre, String apellidos, String casa, int curso,String patronus) {
        this.id = id;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.casa = casa;
        this.curso = curso;
        this.patronus = patronus;
    }

    //Setters
    public void setId(int id) {
        this.id = id;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }
    public void setCasa(String casa) {
        this.casa = casa;
    }
    public void setCurso(int curso) {
        this.curso = curso;
    }
    public void setPatronus(String patronus) {
        this.patronus = patronus;
    }

    //Getters
    public Integer getId() {
        return id;
    }
    public String getNombre() {
        return nombre;
    }
    public String getApellidos() {
        return apellidos;
    }
    public String getCasa() {
        return casa;
    }
    public Integer getCurso() {
        return curso;
    }
    public String getPatronus() {
        return patronus;
    }


    //TODO: Getters y Setters

    /**
     * Devuelve una representación legible del objeto {@code Estudiante}.
     *
     * @return cadena de texto con formato.
     */
    @Override
    public String toString() {
        //TODO: To string de los campos
        return nombre + " " + apellidos + " ( casa: " + casa + " ,curso: " +  curso + " ,patronus: " + patronus + ")";
    }
}