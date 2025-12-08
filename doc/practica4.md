# Práctica 4 — Documentación técnica y migración

Este documento describe, de forma resumida, los cambios funcionales introducidos en la práctica, la evolución del
esquema de datos entre las versiones **1.2.0** y **1.3.0**, así como el script de migración y las indicaciones básicas
para el despliegue en producción. También se detallan las contribuciones realizadas por cada integrante del equipo.

---

## 1. Cambios funcionales introducidos

Se han incorporado tres cambios funcionales principales en la aplicación: finalización de tareas, gestión del perfil
de usuario y rol de administrador de equipo.

### 1.1. Finalización de tareas (botón **“Terminar tarea”**) — Esther

**Objetivo:** permitir al usuario marcar una tarea como terminada y visualizar primero las tareas pendientes.

- **Frontend**:
  - Se añade un botón **“Terminar”** en la vista de la lista de tareas.
  - La lista de tareas se muestra ahora ordenada por estado (`terminada`/no terminada) y, dentro de cada grupo, por `id`.

- **Modelo y DTO**:
  - Se añade el atributo `terminada` al modelo de tarea y a su DTO correspondiente.
  - Se implementan los métodos *getter* y *setter* asociados.

- **Repository (`TareaRepository`)**:
  - Se añade el método:
    - `findByUsuarioIdOrderByTerminadaAscIdAsc(Long usuarioId)`
  - Este método devuelve las tareas de un usuario ordenadas primero por estado (`terminada` ascendente: pendientes primero) y luego por `id`.

- **Service (`TareaService`)**:
  - Nuevos métodos:
    - `terminarTarea(Long idTarea)`: marca una tarea como terminada.
    - `allTareasUsuarioOrdenadas(Long usuarioId)`: devuelve las tareas de un usuario utilizando el nuevo orden.
  - Se sustituye el uso de `allTareasUsuario` en la lógica de la vista por `allTareasUsuarioOrdenadas`, de modo que la interfaz muestre siempre primero las tareas pendientes.

- **Controller (`TareaController`)**:
  - Se añade el método/endpoint `terminar(...)` que:
    - Invoca a `terminarTarea` en el servicio.
    - Redirige de nuevo a la vista de lista de tareas.

- **Tests**:
  - Se han añadido pruebas asociadas para verificar:
    - Que el atributo `terminada` se actualiza correctamente.
    - Que el orden de las tareas por usuario es el esperado.
    - Que el endpoint de finalización de tarea funciona correctamente.

---

### 1.2. Página de **Perfil de usuario** y edición de datos — Arturo

**Objetivo:** permitir a cada usuario gestionar sus datos personales (nombre, email, fecha de nacimiento) y su contraseña.

- **Navegación (Navbar)**:
  - Se añade un nuevo elemento de menú **“Cuenta”**, accesible tras el inicio de sesión.
  - Este enlace lleva a la vista de perfil del usuario.

- **Rutas y lógica de controlador (`UsuarioController`)**:
  - Nuevos métodos:
    - `verPerfil(...)`: muestra la información actual del usuario en el formulario de perfil.
    - `actualizarPerfil(...)`: procesa la actualización de los datos del usuario (nombre, correo, fecha de nacimiento).
    - `cambiarPassword(...)`: gestiona el cambio de contraseña, incluyendo verificación de la contraseña actual.
  - Si el usuario no seleccionó fecha de nacimiento al crear la cuenta:
    - El campo aparece vacío en el formulario, pero puede rellenarse en esta pantalla.

- **Servicio (`UsuarioService`)**:
  - Nuevos métodos:
    - `actualizarPerfil(...)`: actualiza los datos básicos del usuario aplicando las validaciones necesarias (unicidad de `email`, etc.).
    - `cambiarPassword(...)`: actualiza la contraseña del usuario:
      - Verifica la contraseña anterior.
      - Aplica hashing a la nueva contraseña antes de guardarla.

- **Vista (`perfilUsuario.html`)**:
  - Se crea la plantilla de Thymeleaf para el perfil de usuario:
    - Muestra nombre, correo electrónico y fecha de nacimiento.
    - Incluye un formulario específico para el cambio de contraseña con verificación.

- **Tests**:
  - Se han añadido pruebas que cubren:
    - La correcta actualización de los datos del perfil.
    - La lógica de cambio de contraseña (incluyendo el caso de contraseña actual incorrecta).
    - El correcto renderizado y acceso a la vista de perfil.

---

### 1.3. Rol **admin** en equipos — Hugo

**Objetivo:** introducir el rol de **administrador de equipo**, asignado automáticamente al usuario que crea el equipo y responsable de su gestión.

- **Concepto funcional**:
  - La persona que crea un equipo pasa automáticamente a ser su **administrador**.
  - El administrador puede:
    - Editar los datos del equipo.
    - Gestionar sus miembros (añadir y eliminar usuarios).
    - Eliminar el propio equipo.

- **Modelo y DTO**:
  - Se añade la propiedad `adminUserId` al modelo y al DTO de equipo, con sus métodos *getter* y *setter*.
  - Esta propiedad identifica al usuario que actúa como administrador del equipo (persistido en la columna `admin_user_id` de la tabla `equipos`).

- **Controller de equipo**:
  - Nuevos métodos orientados a la gestión por parte del administrador:
    - `añadirUsuarioAEquipoPorAdmin(...)`
    - `eliminarUsuarioDeEquipoPorAdmin(...)`
  - Ambos endpoints sólo deben ser accesibles para el administrador del equipo.

- **Servicio (`EquipoService`)**:
  - Nuevos métodos:
    - `esAdminDeEquipo(Long equipoId, Long usuarioId)`: comprueba si un usuario es administrador de un equipo.
    - `añadirUsuarioAEquipoComoAdmin(...)`: añade usuarios al equipo, validando que quien realiza la operación es administrador.
    - `quitarUsuarioDeEquipoComoAdmin(...)`: elimina usuarios del equipo, con la misma validación de permisos.
    - `usuariosNoMiembros(Long equipoId)`: obtiene usuarios que aún no pertenecen al equipo, útil para mostrarlos en la UI.
  - La lógica de negocio centraliza las comprobaciones de permisos, evitando duplicar validaciones en los controladores.

- **Vistas / HTML de equipos**:
  - Se han modificado las plantillas relacionadas con equipos para:
    - Mostrar los botones de gestión (añadir / quitar usuarios, editar equipo, borrar equipo) **solo al administrador**.
    - Mantener una vista simplificada para los miembros que no son administradores.

- **Tests**:
  - Se han añadido pruebas que validan:
    - Que el creador del equipo se establece como administrador.
    - Que sólo el administrador puede gestionar miembros o eliminar el equipo.
    - Que la interfaz refleja correctamente la visibilidad de los botones según el rol.

---

## 2. Estructura del código y archivos relevantes

- **Controladores y servicios**:
  - `src/main/java/madstodolist/controller`
  - `src/main/java/madstodolist/service`
- **Plantillas HTML (Thymeleaf)**:
  - `src/main/resources/templates`
  - Ejemplos:
    - `perfilUsuario.html`
    - `listaTareas.html`
- **Migraciones y esquemas SQL** (para Flyway/Liquibase o ejecución manual):
  - Directorio `sql/`:
    - `schema-1.2.0.sql`
    - `schema-1.3.0.sql`
    - `schema-1.2.0-1.3.0.sql`

---

## 3. Esquema de datos — versión 1.2.0

El esquema de la versión **1.2.0** se corresponde con el contenido del archivo `sql/schema-1.2.0.sql`. A nivel lógico, las
tablas principales son:

```sql
-- usuarios: información básica de cuenta
CREATE TABLE public.usuarios (
    id               bigint NOT NULL,
    admin            boolean NOT NULL,
    bloqueado        boolean NOT NULL,
    email            character varying(255) NOT NULL,
    fecha_nacimiento date,
    nombre           character varying(255),
    password         character varying(255)
);

-- tareas: tareas asociadas a un usuario
CREATE TABLE public.tareas (
    id         bigint NOT NULL,
    titulo     character varying(255) NOT NULL,
    usuario_id bigint NOT NULL
);

-- equipos: grupos de usuarios sin administrador de equipo explícito
CREATE TABLE public.equipos (
    id     bigint NOT NULL,
    nombre character varying(255) NOT NULL
);

-- relación N:M entre equipos y usuarios
CREATE TABLE public.equipos_usuarios (
    equipo_id  bigint NOT NULL,
    usuario_id bigint NOT NULL
);
```

Características relevantes de esta versión:

- No existe todavía la columna `terminada` en `tareas`.
- La tabla `equipos` no dispone del campo `admin_user_id`; todos los usuarios del equipo están “al mismo nivel”.
- La información de permisos se limita al campo `admin` de la tabla `usuarios` (administrador global), pero no hay
  administrador específico de equipo.

---

## 4. Esquema de datos — versión 1.3.0

El esquema de la versión **1.3.0** se recoge en `sql/schema-1.3.0.sql`. A partir de esta versión se añaden:

- Una columna para marcar tareas como terminadas.
- Una columna para identificar al administrador de cada equipo.

De forma resumida:

```sql
-- equipos: se añade admin_user_id
CREATE TABLE public.equipos (
    id            bigint NOT NULL,
    admin_user_id bigint,
    nombre        character varying(255) NOT NULL
);

-- tareas: se añade la columna terminada
CREATE TABLE public.tareas (
    id         bigint NOT NULL,
    terminada  boolean NOT NULL,
    titulo     character varying(255) NOT NULL,
    usuario_id bigint NOT NULL
);

-- usuarios y equipos_usuarios mantienen la misma estructura que en la 1.2.0
CREATE TABLE public.usuarios (
    id               bigint NOT NULL,
    admin            boolean NOT NULL,
    bloqueado        boolean NOT NULL,
    email            character varying(255) NOT NULL,
    fecha_nacimiento date,
    nombre           character varying(255),
    password         character varying(255)
);

CREATE TABLE public.equipos_usuarios (
    equipo_id  bigint NOT NULL,
    usuario_id bigint NOT NULL
);
```

Cambios clave respecto a la versión 1.2.0:

- **Nuevo campo `terminada` en `tareas`**:
  - Permite distinguir explícitamente entre tareas pendientes y terminadas.
  - Es `NOT NULL`, por lo que todas las filas deben tener un valor booleano definido.

- **Nuevo campo `admin_user_id` en `equipos`**:
  - Almacena el identificador del usuario administrador del equipo.
  - Se asocia a `usuarios.id` mediante una clave foránea (ver script de migración).

---

## 5. Script de migración de la base de datos (1.2.0 → 1.3.0)

La migración entre las versiones **1.2.0** y **1.3.0** se realiza mediante el script `sql/schema-1.2.0-1.3.0.sql`. Este
script está diseñado para aplicarse sobre una base de datos con el esquema 1.2.0 existente, sin borrar datos.

Contenido esencial del script:

```sql
-- 1. Añadir columna admin_user_id a la tabla equipos
ALTER TABLE equipos
    ADD COLUMN admin_user_id bigint;

-- Añadir la foreign key hacia usuarios.id
ALTER TABLE equipos
    ADD CONSTRAINT fk_equipos_admin_user
        FOREIGN KEY (admin_user_id)
            REFERENCES usuarios(id);

-- 2. Añadir columna terminada a la tabla tareas
ALTER TABLE tareas
    ADD COLUMN terminada boolean NOT NULL DEFAULT false;

-- Opcional: si se desea evitar un valor por defecto en futuras inserciones
ALTER TABLE tareas
    ALTER COLUMN terminada DROP DEFAULT;
```

Notas:

- La columna `admin_user_id` se añade permitiendo inicialmente valores `NULL`, de modo que no se rompe ningún registro
  existente. Posteriormente, la aplicación se encarga de rellenar este valor al crear nuevos equipos.
- La columna `terminada` se introduce con `DEFAULT false` para inicializar todas las tareas existentes como no
  terminadas. A continuación se elimina el valor por defecto para evitar dependencias implícitas en futuras inserciones.
- El script no elimina tablas ni columnas, por lo que es seguro aplicarlo sobre datos de producción.

---

## 6. Despliegue en producción

En esta sección se describen, por un lado, los pasos necesarios para desplegar y probar la aplicación
en un entorno con Docker y PostgreSQL, y por otro lado los pasos que se han seguido durante el desarrollo
para construir la imagen final y preparar la base de datos.

### 6.1. Pasos para el despliegue

A continuación se detallan los pasos mínimos para levantar la aplicación utilizando la imagen final
publicada en Docker Hub y una base de datos PostgreSQL en contenedor.

#### 6.1.1. Descargar la imagen Docker final

La imagen de la aplicación está publicada en:

- `https://hub.docker.com/r/estherps/mads-todolist-equipo04`

Descarga de la imagen:

```bash
docker pull estherps/mads-todolist-equipo04:1.3.0
```

#### 6.1.2. Crear la red Docker para aplicación y base de datos

```bash
docker network create network-equipo
```

#### 6.1.3. Levantar el contenedor de PostgreSQL

```bash
docker run -d --network network-equipo --network-alias postgres \
  -p 5432:5432 \
  -v ${PWD}:/mi-host \
  --name db-equipo \
  -e POSTGRES_USER=mads \
  -e POSTGRES_PASSWORD=mads \
  -e POSTGRES_DB=mads \
  postgres:13
```

#### 6.1.4. Importar el esquema y los datos de prueba

Esquema de la versión 1.3.0:

```bash
docker exec -it db-equipo bash
psql -U mads mads < /mi-host/sql/schema-1.3.0.sql
exit
```

Backup con datos de prueba (usuarios, tareas y equipos):

```bash
docker exec -it db-equipo bash
psql -U mads mads < /mi-host/sql/backup-1.3.0.sql
exit
```

#### 6.1.5. Ejecutar la aplicación

```bash
docker run --rm --network network-equipo -p 8080:8080 \
  estherps/mads-todolist-equipo04:1.3.0 \
  --spring.profiles.active=postgres \
  --POSTGRES_HOST=postgres
```

#### 6.1.6. Verificación básica

Con la aplicación en ejecución, acceder a:

- `http://localhost:8080`

Comprobar, entre otros aspectos:

- Acceso a la aplicación y login.
- Visualización y edición del perfil de usuario.
- Creación y finalización de tareas (botón **Terminar**).
- Creación y gestión de equipos, incluyendo el rol de administrador de equipo.
- Persistencia de los cambios en la base de datos PostgreSQL.

---

### 6.2. Pasos durante el desarrollo

En esta subsección se describen los pasos internos realizados por el equipo para construir el artefacto,
generar la imagen Docker y preparar los scripts de base de datos.

#### 6.2.1. Construcción del proyecto

Compilación del proyecto y generación del JAR ejecutable:

```bash
./mvnw clean package
```

Artefacto generado (nombre aproximado, según configuración de `pom.xml`):

- `target/todolist-equipo04-1.3.0.jar`

#### 6.2.2. Construcción y publicación de la imagen Docker

A partir del JAR generado, se construyó la imagen Docker:

```bash
docker build -t todolist-equipo04:1.3.0 .
```

Etiquetado y subida al registro de Docker Hub:

```bash
docker tag todolist-equipo04:1.3.0 estherps/mads-todolist-equipo04:1.3.0
docker push estherps/mads-todolist-equipo04:1.3.0
```

#### 6.2.3. Preparación de la base de datos y backup final

Durante el desarrollo se utilizaron los siguientes pasos para preparar la base de datos:

1. Levantar un contenedor de PostgreSQL de desarrollo.
2. Aplicar el esquema `schema-1.3.0.sql`.
3. Insertar datos de prueba (usuarios, tareas, equipos).
4. Generar un backup de referencia para la entrega:

```bash
docker exec -it db-equipo bash
pg_dump -U mads --clean mads > /mi-host/sql/backup-1.3.0.sql
```

Este backup es el que se utiliza posteriormente en los pasos de despliegue.

#### 6.2.4. Creación del script de migración 1.2.0 → 1.3.0

Para documentar la evolución del esquema y permitir la migración de una base de datos en versión 1.2.0
a la versión 1.3.0, se creó el script:

- `sql/schema-1.2.0-1.3.0.sql`

Contenido esencial del script:

```sql
-- Añadir columna admin_user_id a la tabla equipos
ALTER TABLE equipos
    ADD COLUMN admin_user_id bigint;

ALTER TABLE equipos
    ADD CONSTRAINT fk_equipos_admin_user
        FOREIGN KEY (admin_user_id)
            REFERENCES usuarios(id);

-- Añadir columna terminada a la tabla tareas
ALTER TABLE tareas
    ADD COLUMN terminada boolean NOT NULL DEFAULT false;

-- Opcionalmente, eliminar el valor por defecto para futuras inserciones
ALTER TABLE tareas
    ALTER COLUMN terminada DROP DEFAULT;
```

---

### 6.3. Resumen del resultado final

- Imagen Docker final publicada:
  - `estherps/mads-todolist-equipo04:1.3.0`
- Esquemas y scripts SQL incluidos en el repositorio:
  - `sql/schema-1.2.0.sql`
  - `sql/schema-1.3.0.sql`
  - `sql/schema-1.2.0-1.3.0.sql`
  - `sql/backup-1.2.1.sql`
  - `sql/backup-1.3.0.sql`
- Aplicación verificada con:
  - Gestión de tareas (incluyendo finalización).
  - Gestión de perfil de usuario.
  - Gestión de equipos con rol de administrador de equipo.
  - Persistencia de los datos en PostgreSQL tanto en local como en contenedor Docker.
