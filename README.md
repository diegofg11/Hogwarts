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
    git clone https://github.com/diegofg11/hogwarts.git
    ```
2.  **Abrir el proyecto en tu IDE de preferencia (IntelliJ, Eclipse, etc.).**
3.  **Asegurarte de que tienes todas las dependencias de Maven descargadas.**
4.  **Ejecutar la clase `Lanzador.java`.**
5.  **Otra opción sería usar el ejecutable otorgado por los desarrolladores con las bases ya dockerizadas.**
6.  **En ese caso, simplemente ejecutariamos los archivos con los scripts pertinentes y abririamos la app**

## Funcionalidades

* **Selección de Casa:** Permite cambiar entre las diferentes casas de Hogwarts y la vista general de la escuela.
* **Gestión de Estudiantes:** Permite añadir, editar y eliminar estudiantes de la base de datos seleccionada.
* **Sincronización:** Los cambios realizados en las bases de datos de las casas se sincronizan automáticamente con la base de datos central de Hogwarts.
* **Interfaz Personalizada:** La interfaz de la aplicación cambia de color y muestra el escudo de la casa seleccionada.

## Requisitos
1.  **Software:**
    * **Java 24** (JDK).
    * **Maven** (para compilar el proyecto).

2.  **Bases de Datos (DBs):**
    * Necesitas **5 motores de bases de datos** activos: MariaDB, Apache Derby, H2, Oracle y HSQLDB.
    * Debes tener las tablas `Estudiantes` creadas en cada una de estas DBs (puedes usar los scripts de la carpeta ejecutable otorgada para la demo por los desarrolladores).

3.  **Configuración (¡El paso más importante!):**
    * Debes crear manualmente un archivo llamado `config.properties`.
    * Colócalo en la carpeta `src/main/resources/` (el archivo está en el `.gitignore`, por eso no se incluye en el repositorio).
    * Este archivo debe contener las URLs, usuarios y contraseñas para las 5 bases de datos.

4. **Puedes copiar esto en tu config.properties si te es mas comodo**
```
# Base central (MariaDB)
mariadb.url=
mariadb.user=
mariadb.password=


# Hufflepuff (H2)
hufflepuff.url=
hufflepuff.user=
hufflepuff.password=


# Ravenclaw (Oracle)
ravenclaw.url=
ravenclaw.user=
ravenclaw.password=


# Gryffindor (Derby)
gryffindor.url=
gryffindor.user=
gryffindor.password=


# Slytherin (HSQLDB)
slytherin.url=
slytherin.user=
slytherin.password=
```
## Cómo Ejecutar la Aplicación

1.  **Desde el IDE (IntelliJ, Eclipse, etc.):**
    * Asegúrate de tener la configuración de `config.properties` lista.
    * Ejecuta el método `main` de la clase `Lanzador.java`.

2.  **Mediante Scripts (Línea de Comandos):**
    * **Paso 1: Compilar (Crear el JAR)**
    * Usar la funcion de maven package

    * **Paso 2: Ejecutar la aplicación**
        ```bash
        java -jar target/Hogwarts-1.0-SNAPSHOT.jar
        ```
      (El nombre exacto del `.jar` depende de la versión en el `pom.xml`).

## Estructura

```
.
├── .gitignore
├── .idea
│   ├── .gitignore
│   ├── encodings.xml
│   ├── misc.xml
│   └── vcs.xml
├── README.md
├── nbactions.xml
├── pom.xml
└── src
    └── main
        ├── java
        │   ├── module-info.java
        │   └── org
        │       └── equiporon
        │           ├── App.java
        │           ├── Conexion
        │           │   ├── ConexionBD.java
        │           │   └── Config.java
        │           ├── Controlador
        │           │   └── Controlador.java
        │           ├── DAO
        │           │   ├── BaseDAO.java
        │           │   ├── DerbyDAO.java
        │           │   ├── H2DAO.java
        │           │   ├── HSQLDBDAO.java
        │           │   ├── MariaDBDAO.java
        │           │   ├── OracleDAO.java
        │           │   └── SQLiteDAO.java
        │           ├── Lanzador.java
        │           ├── Modelo
        │           │   └── Modelo_Estudiante.java
        │           └── Utils
        │               ├── Alertas.java
        │               └── I18n.java
        └── resources
            ├── META-INF
            │   └── MANIFEST.MF
            ├── SQL
            │   ├── derby_gryffindor.sql
            │   ├── h2_hufflepuff.sql
            │   ├── hsqldb_slytherin.sql
            │   ├── mariadb.sql
            │   ├── oracle_ravenclaw
            │   └── sqlite.sql
            ├── fxml
            │   ├── casas.fxml
            │   └── primary.fxml
            ├── i18n
            │   ├── messages_en.properties
            │   ├── messages_es.properties
            │   └── messages_la.properties
            ├── images
            │   ├── gryffindor_banner.png
            │   ├── gryffindor_escudo.png
            │   ├── hogwarts_banner.png
            │   ├── hogwarts_escudo.png
            │   ├── hufflepuff_banner.png
            │   ├── hufflepuff_escudo.png
            │   ├── ravenclaw_banner.png
            │   ├── ravenclaw_escudo.png
            │   ├── slytherin_banner.png
            │   └── slytherin_escudo.png
            ├── logback.xml
            └── styles
                └── Styles.css
```

## Autores

* Diego
* Rubén
* Unai
* Gaizka
* Igor
* Xiker