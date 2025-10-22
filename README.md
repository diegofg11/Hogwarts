# Hogwarts

Este proyecto es una aplicación de escritorio desarrollada en JavaFX que permite gestionar una base de datos de estudiantes de Hogwarts. La aplicación se conecta a diferentes bases de datos, cada una representando a una de las casas de Hogwarts (Gryffindor, Hufflepuff, Ravenclaw y Slytherin), y una base de datos central que representa a todo el colegio.

## Arquitectura

El proyecto utiliza una arquitectura de bases de datos distribuidas, donde cada casa de Hogwarts tiene su propia base de datos con un motor diferente:

* **Hogwarts (Central):** MariaDB
* **Gryffindor:** Apache Derby
* **Hufflepuff:** H2
* **Ravenclaw:** Oracle
* **Slytherin:** HSQLDB

## Tecnologías Utilizadas

* **JavaFX:** Para la interfaz gráfica de usuario.
* **Maven:** Para la gestión de dependencias y la construcción del proyecto.
* **JDBC:** Para la conexión con las diferentes bases de datos.
* **SLF4J & Logback:** Para el logging de la aplicación.

## Cómo Ejecutar la Aplicación

1.  **Clonar el repositorio:**
    ```bash
    git clone [https://github.com/diegofg11/hogwarts.git](https://github.com/diegofg11/hogwarts.git)
    ```
2.  **Abrir el proyecto en tu IDE de preferencia (IntelliJ, Eclipse, etc.).**
3.  **Asegurarte de que tienes todas las dependencias de Maven descargadas.**
4.  **Ejecutar la clase `Lanzador.java`.**

## Funcionalidades

* **Selección de Casa:** Permite cambiar entre las diferentes casas de Hogwarts y la vista general de la escuela.
* **Gestión de Estudiantes:** Permite añadir, editar y eliminar estudiantes de la base de datos seleccionada.
* **Sincronización:** Los cambios realizados en las bases de datos de las casas se sincronizan automáticamente con la base de datos central de Hogwarts.
* **Interfaz Personalizada:** La interfaz de la aplicación cambia de color y muestra el escudo de la casa seleccionada.

## Autores

* Diego
* Rubén
* Unai
* Gaizka
* Igor
* Xiker