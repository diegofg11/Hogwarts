# 🧙‍♂️ Hogwarts Database Manager

**Proyecto académico desarrollado en JavaFX con conexión a múltiples bases de datos (MariaDB, Oracle, Derby, H2 y HSQLDB).**
Permite gestionar estudiantes de Hogwarts, sincronizando datos entre las casas y la base central de Hogwarts.

---

## 🧾 ÍNDICE
1. [Descripción general](#descripción-general)
2. [Requisitos previos](#requisitos-previos)
3. [Instalación y configuración de Docker](#instalación-y-configuración-de-docker)
4. [Estructura del proyecto](#estructura-del-proyecto)
5. [Configuración de `config.properties`](#configuración-de-configproperties)
6. [Ejecución del proyecto](#ejecución-del-proyecto)
7. [Uso de la aplicación](#uso-de-la-aplicación)
8. [Internacionalización (i18n)](#internacionalización-i18n)
9. [Posibles errores y soluciones](#posibles-errores-y-soluciones)
10. [Créditos](#créditos)

---

## 🪄 Descripción general

**Hogwarts Database Manager** es una aplicación de escritorio JavaFX que permite:
- Visualizar, añadir, editar y eliminar estudiantes.
- Conectarse a diferentes bases de datos, una por cada casa de Hogwarts.
- Sincronizar automáticamente los cambios con la base de datos central (MariaDB).
- Cambiar de idioma desde el menú superior.

**Casas y motores:**

| Casa | Motor de BD | Puerto | Propósito |
|------|--------------|--------|------------|
| Gryffindor | Apache Derby | 1527 | Casa de Gryffindor |
| Hufflepuff | H2 Database | 9092 | Casa de Hufflepuff |
| Slytherin | HSQLDB | 9001 | Casa de Slytherin |
| Ravenclaw | Oracle Free | 1521 | Casa de Ravenclaw |
| Hogwarts | MariaDB | 3306 | Base central |

---

## 🧰 Requisitos previos

### 🔹 Software necesario
| Herramienta | Versión recomendada |
|--------------|--------------------|
| **Java JDK** | 23 o superior (probado con Valhalla EA 23) |
| **Maven** | 3.9+ |
| **Docker + Docker Compose** | Última versión |
| **IntelliJ IDEA** | Community o Ultimate |
| **Git** | Última versión |

---

## 🐳 Instalación y configuración de Docker

### 1️⃣ Instalación de Docker
Descarga e instala **Docker Desktop** desde su página oficial:  
🔗 [https://www.docker.com/products/docker-desktop/](https://www.docker.com/products/docker-desktop/)

Durante la instalación:
- Acepta la opción *“Use WSL 2 instead of Hyper-V”* si usas Windows 10 o superior.
- Reinicia el sistema cuando lo solicite.

Una vez instalado, **abre Docker Desktop** y asegúrate de que esté **en ejecución** (icono de ballena visible en la barra de tareas).

Puedes verificarlo desde una terminal con:
```bash
docker version
```
Si responde correctamente, Docker está listo.

---

### 2️⃣ Iniciar las bases de datos del proyecto

Dentro de la carpeta del proyecto existe una subcarpeta `docker/` con un archivo `docker-compose.yml` que contiene todas las configuraciones necesarias.

Para iniciar todos los contenedores, abre una terminal en esa carpeta y ejecuta:
```bash
docker-compose up -d
```
Esto levantará automáticamente los servicios de:
- MariaDB (Hogwarts)
- Oracle (Ravenclaw)
- Derby (Gryffindor)
- H2 (Hufflepuff)
- HSQLDB (Slytherin)

Puedes comprobar que están activos con:
```bash
docker ps
```

Cada contenedor debe mostrarse con su puerto asignado (1521, 1527, 9092, 9001, 3306).

---

## 📁 Estructura del proyecto

```plaintext
📦 Hogwarts_Entrega
 ┣ 📂 Hogwarts
 ┃ ┣ 📂 src
 ┃ ┃ ┣ 📂 main
 ┃ ┃ ┃ ┣ 📂 java/org/equiporon/...       → Código fuente principal
 ┃ ┃ ┃ ┣ 📂 resources/fxml               → Archivos FXML
 ┃ ┃ ┃ ┣ 📂 resources/images             → Iconos y banners
 ┃ ┃ ┃ ┣ 📂 resources/i18n               → Archivos de idioma
 ┃ ┃ ┃ ┣ 📂 resources/styles             → CSS de la interfaz
 ┃ ┃   ┗ 📜 resources/config.properties  → Configuración de conexiones JDBC
 ┃ ┣ 📜 pom.xml                     → Configuración de Maven
 ┃          → Configuración de conexiones JDBC
 ┣ 📂 docker                        → Archivos Docker y scripts
 ┗ 📜 BD-Hogwarts.bat               → Archivo para ejecutar la aplicación
```

---

## ⚙️ Configuración de `config.properties`

Este archivo contiene las rutas JDBC para todas las bases de datos.
Debe encontrarse en la carpeta resources del proyecto (`/Hogwarts/src/main/resources`).
Teneis una plantilla como la de aqui abajo en la carpeta config dentro de la carpeta Hogwarts_Entrega

Ejemplo de configuración:
```properties
# Base central (MariaDB)
mariadb.url=jdbc:mariadb://localhost:3306/hogwarts  #Cambiar localhost si no esta en vustro ordenador por la ip correspondiente
mariadb.user=tuUsuario
mariadb.password=Tucontraseña

# Hufflepuff (H2)
hufflepuff.url=jdbc:h2:tcp://localhost:9092/hufflepuff;DB_CLOSE_ON_EXIT=FALSE  #Cambiar localhost si no esta en vustro ordenador por la ip correspondiente
hufflepuff.user=tuUsuario
hufflepuff.password=Tucontraseña

# Ravenclaw (Oracle)
ravenclaw.url=jdbc:oracle:thin:@localhost:1521/FREEPDB1   #Cambiar localhost si no esta en vustro ordenador por la ip correspondiente
ravenclaw.user=tuUsuario
ravenclaw.password=Tucontraseña

# Gryffindor (Derby)
gryffindor.url=jdbc:derby://localhost:1527/gryffindorDB;create=false   #Cambiar localhost si no esta en vustro ordenador por la ip correspondiente
gryffindor.user=tuUsuario
gryffindor.password=Tucontraseña

# Slytherin (HSQLDB)
slytherin.url=jdbc:hsqldb:hsql://localhost:9002/slytherin   #Cambiar localhost si no esta en vustro ordenador por la ip correspondiente
slytherin.user=tuUsuario
slytherin.password=Tucontraseña
```

---

## 🚀 Ejecución del proyecto

> ⚠️ **Importante:** antes de ejecutar la aplicación asegúrate de que **Docker Desktop esté abierto y los contenedores estén activos** (`docker ps`).

La aplicación **solo puede ejecutarse mediante el archivo `BD-Hogwarts.bat`** incluido en la carpeta raíz del proyecto.  
Este archivo se encarga de lanzar la aplicación en modo gráfico **sin abrir la consola de Windows**.

Ejemplo del contenido de `BD-Hogwarts.bat`:
```bat
@echo off
echo Iniciando Hogwarts Manager...
start javaw -jar "%~dp0Hogwarts/target/Hogwarts.jar"
exit
```

Para ejecutar el programa:
1. Asegúrate de que Docker esté en funcionamiento.  
2. Abre la carpeta del proyecto.  
3. Haz doble clic en **BD-Hogwarts.vbs**.  
4. Espera unos segundos y se abrirá la aplicación JavaFX.

---

## 🧑‍💻 Uso de la aplicación

1. Selecciona una **casa** desde el menú desplegable.  
2. Añade o edita estudiantes mediante los campos de texto o doble clic en la tabla.  
3. Usa los botones:
   - ➕ **Añadir** → Crea un nuevo estudiante.
   - 🗑️ **Borrar** → Elimina el estudiante seleccionado.
   - 🔄 **Deshacer** → Restaura los datos a su estado anterior.  
4. Los cambios se sincronizan automáticamente con la base central (MariaDB).

---

## 🌍 Internacionalización (i18n)

La aplicación soporta varios idiomas mediante `ResourceBundle`:

| Idioma | Archivo | Código |
|---------|----------|---------|
| Español | messages_es.properties | es_ES |
| Inglés | messages_en.properties | en_US |
| Parsel | messages_la.properties | la |

El idioma puede cambiarse desde el menú superior **Language** dentro de la interfaz.

---

## ⚠️ Posibles errores y soluciones

| Error | Causa | Solución |
|-------|--------|-----------|
| `No resources specified` | Falta el archivo FXML o mal ubicado | Verifica la ruta `/resources/fxml/primary.fxml` |
| `ORA-02289: no existe la secuencia` | Secuencia no creada en Oracle | Crear la secuencia o usar IDs automáticos |
| `Connection refused` | Docker no iniciado o puertos incorrectos | Ejecuta `docker ps` y revisa que los contenedores estén activos |
| `No suitable driver` | Faltan drivers JDBC | Revisa dependencias en `pom.xml` |
| La aplicación no abre | Se ejecutó sin Docker activo | Inicia Docker Desktop antes de abrir `BD-Hogwarts.bat` |

---

## 👨‍💻 Créditos

**Desarrolladores:**  
- Diego  
- Rubén  
- Unai  
- Gaizka  
- Igor  
- Xiker
---

🪄 "Trabajar duro es importante, pero hay algo que importa aún más: creer en uno mismo." — Harry Potter