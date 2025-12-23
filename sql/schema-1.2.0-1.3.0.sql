--
-- Migración de esquema: versión 1.2.0 → 1.3.0
-- Este script actualiza la base de datos de producción sin borrar datos.
--

------------------------------------------------------------
-- 1. Añadir columna admin_user_id a la tabla equipos
------------------------------------------------------------
ALTER TABLE equipos
    ADD COLUMN admin_user_id bigint;

-- Añadir la foreign key hacia usuarios.id
ALTER TABLE equipos
    ADD CONSTRAINT fk_equipos_admin_user
        FOREIGN KEY (admin_user_id)
            REFERENCES usuarios(id);

------------------------------------------------------------
-- 2. Añadir columna terminada a la tabla tareas
------------------------------------------------------------
ALTER TABLE tareas
    ADD COLUMN terminada boolean NOT NULL DEFAULT false;

-- Opcional
ALTER TABLE tareas
    ALTER COLUMN terminada DROP DEFAULT;
