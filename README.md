# 📝 ToDoList - Gestión de Tareas y Equipos

Una aplicación web completa desarrollada con **Spring Boot** para la gestión eficiente de tareas personales, organización de equipos y seguimiento de proyectos mediante tableros Kanban.

## 🚀 Descripción

Este proyecto es una solución integral para la productividad. Permite a los usuarios gestionar sus propias listas de tareas, pero su verdadero potencial radica en la **colaboración**. Los usuarios pueden formar equipos, crear proyectos y gestionar el flujo de trabajo visualmente.

### ✨ Características Principales

* **👤 Gestión Personal:**
    * Creación, edición y eliminación de tareas personales.
    * Vista de **Calendario** para visualizar fechas límite.
    * Listado de tareas con estados (Pendiente, En Curso, Terminada).

* **🤝 Colaboración en Equipos:**
    * Creación y administración de equipos de trabajo.
    * Gestión de miembros (invitar, expulsar, roles de administrador).
    * Calendario compartido del equipo.

* **📊 Proyectos y Kanban:**
    * Organización de tareas dentro de proyectos específicos.
    * **Tablero Kanban Interactivo:** Mueve tareas entre columnas (Pendiente, En Curso, Hecho) arrastrando y soltando (Drag & Drop).
    * **Checklists:** Sub-tareas dentro de cada tarea para un control más granular.

* **🛡️ Administración y Seguridad:**
    * Sistema de Login y Registro seguro.
    * Panel de administración para gestionar usuarios registrados (bloquear/desbloquear acceso).

## 🛠️ Stack Tecnológico

El proyecto está construido utilizando tecnologías modernas y robustas:

* **Backend:** Java 11, Spring Boot 2.7.x
* **Frontend:** Thymeleaf (Motor de plantillas), Bootstrap 4 (Estilos), JavaScript (Lógica cliente).
* **Base de Datos:**
    * *Desarrollo:* H2 (Base de datos en memoria).
    * *Producción/Despliegue:* PostgreSQL.
* **Herramientas de Construcción:** Maven.
* **Contenedores:** Docker y Docker Compose.
* **Testing:** JUnit 5, Mockito, Spring Boot Test.

## ⚙️ Instalación y Ejecución

Tienes dos formas de ejecutar la aplicación: localmente con Java o utilizando Docker.

### Opción A: Ejecución Local (Requiere JDK 11)

1.  **Clonar el repositorio:**
    ```bash
    git clone https://github.com/mads-ua-25-26/todolist-final-equipo-04
    cd todolist-final-equipo-04
    ```

2.  **Ejecutar con Maven Wrapper:**
    Esto utilizará la base de datos H2 en memoria por defecto.
    ```bash
    ./mvnw spring-boot:run
    ```

3.  **Acceder a la aplicación:**
    Abre tu navegador en `http://localhost:8080`.

### Opción B: Despliegue con Docker (Recomendado)

Esta opción levanta la aplicación junto con una base de datos PostgreSQL real.

1.  **Asegúrate de tener Docker y Docker Compose instalados.**

2.  **Construir y levantar los contenedores:**
    ```bash
    docker-compose up --build
    ```

3.  **Acceder a la aplicación:**
    La aplicación estará disponible en `http://localhost:8080`.

## 🧪 Ejecución de Tests

El proyecto cuenta con una batería de tests unitarios y de integración para asegurar la calidad del código.

Para ejecutar todos los tests:

```bash
./mvnw test
```

## 📂 Estructura del Proyecto

* `src/main/java`: Código fuente Java (Controladores, Servicios, Modelos, Repositorios).
* `src/main/resources/templates`: Vistas HTML (Thymeleaf).
* `src/main/resources/static`: Archivos estáticos (CSS, JS, Imágenes).
* `src/test`: Tests unitarios y de integración.
* `docker-compose.yml`: Orquestación de contenedores para la App y PostgreSQL.

## 👥 Usuarios de Prueba (Datos Iniciales)

Si la base de datos se inicializa con datos de prueba (InitDbService), puedes probar con:

* **Admin:** admin@ua.es / admin  
* **Usuario:** user@ua.es / 1234 

---

Desarrollado para la asignatura de **MADS - Universidad de Alicante**.
