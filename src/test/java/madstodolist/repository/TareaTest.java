package madstodolist.repository;


import madstodolist.model.Equipo;
import madstodolist.model.Tarea;
import madstodolist.model.Usuario;
import madstodolist.model.Proyecto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Sql(scripts = "/clean-db.sql")
public class TareaTest {

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    TareaRepository tareaRepository;

    //
    // Tests modelo Tarea en memoria, sin la conexión con la BD
    //

    @Test
    public void crearTareaUsuario() {
        // GIVEN
        // Un usuario nuevo creado en memoria, sin conexión con la BD,

        Usuario usuario = new Usuario("juan.gutierrez@gmail.com");

        // WHEN
        // se crea una nueva tarea con ese usuario,

        Tarea tarea = new Tarea(usuario, "Práctica 1 de MADS", null);

        // THEN
        // el título y el usuario de la tarea son los correctos.

        assertThat(tarea.getTitulo()).isEqualTo("Práctica 1 de MADS");
        assertThat(tarea.getUsuario()).isEqualTo(usuario);
    }

    @Test
    public void crearTareaConFechaLimite() {
        Usuario usuario = new Usuario("user@ua");

        LocalDate fecha = LocalDate.of(2025, 12, 15);
        Tarea tarea = new Tarea(usuario, "Práctica MADS", null);
        tarea.setFechaLimite(fecha);

        assertThat(tarea.getFechaLimite()).isEqualTo(fecha);
    }


    @Test
    public void crearTareaEquipo() {
        // GIVEN
        // Un equipo nuevo creado en memoria, sin conexión con la BD,

        Equipo equipo = new Equipo("Equipo A");

        // WHEN
        // se crea una nueva tarea con ese usuario,
        Tarea tarea = new Tarea(equipo, "Revisar informes", null);

        // THEN
        // el título y atributos de la tarea son los correctos.

        assertThat(tarea.getTitulo()).isEqualTo("Revisar informes");
        assertThat(tarea.getUsuario()).isNull();
        assertThat(tarea.getProyecto()).isNull();
    }

    @Test
    public void crearTareaProyecto() {
        // GIVEN
        // Un proyecto nuevo creado en memoria y el equipo, sin conexión con la BD,
        Equipo equipo = new Equipo("Equipo A");
        Proyecto proyecto = new Proyecto("Proyecto X", equipo);

        // WHEN
        // se crea una nueva tarea con ese proyecto,
        Tarea tarea = new Tarea(proyecto, "Preparar presentación", null);

        // THEN
        // el título y atributos de la tarea son los correctos.

        assertThat(tarea.getTitulo()).isEqualTo("Preparar presentación");
        assertThat(tarea.getEquipo()).isEqualTo(equipo);
        assertThat(tarea.getUsuario()).isNull();
    }

    @Test
    public void asignarUsuarioNoEliminaEquipoNiProyecto() {

        Usuario u = new Usuario("user@ua");
        Equipo e = new Equipo("Equipo A");
        Proyecto p = new Proyecto("Proyecto X", e);

        Tarea tarea = new Tarea(e, "Revisar informes", null);
        tarea.setProyecto(p);

        // Confirmación del estado inicial
        assertThat(tarea.getEquipo()).isEqualTo(e);
        assertThat(tarea.getProyecto()).isEqualTo(p);
        assertThat(tarea.getUsuario()).isNull();

        // Ahora asignamos usuario
        tarea.setUsuario(u);

        // asignar usuario NO borra equipo ni proyecto
        assertThat(tarea.getUsuario()).isEqualTo(u);
        assertThat(tarea.getEquipo()).isEqualTo(e);  // El equipo sigue
        assertThat(tarea.getProyecto()).isEqualTo(p); // El proyecto sigue
    }

    @Test
    public void setProyectoActualizaEquipoSinEliminarUsuario() {

        Equipo e = new Equipo("Equipo A");
        Proyecto p = new Proyecto("Proyecto X", e);

        Usuario u = new Usuario("user@ua");
        Tarea tarea = new Tarea(u, "Tarea personal", null);

        // Estado inicial
        assertThat(tarea.getUsuario()).isEqualTo(u);
        assertThat(tarea.getEquipo()).isNull();
        assertThat(tarea.getProyecto()).isNull();

        // Asignamos proyecto
        tarea.setProyecto(p);

        //comprobamos q todos los valores son validos
        assertThat(tarea.getProyecto()).isEqualTo(p);
        assertThat(tarea.getEquipo()).isEqualTo(e);
        assertThat(tarea.getUsuario()).isEqualTo(u);
    }

    @Test
    public void laListaDeTareasDeUnUsuarioSeActualizaEnMemoriaConUnaNuevaTarea() {
        // GIVEN
        // Un usuario nuevo creado en memoria, sin conexión con la BD,

        Usuario usuario = new Usuario("juan.gutierrez@gmail.com");
        Set<Tarea> tareas = usuario.getTareas();

        // WHEN
        // se crea una nueva tarea y se añade al usuario,
        Tarea tarea = new Tarea(usuario, "Práctica 1 de MADS", null);
        usuario.addTarea(tarea);

        // THEN
        // la tarea creada se ha añadido a la lista de tareas del usuario.

        assertThat(usuario.getTareas()).contains(tarea);
        assertThat(tareas).contains(tarea);
    }

    @Test
    public void comprobarIgualdadTareasSinId() {
        // GIVEN
        // Creadas tres tareas sin identificador, y dos de ellas con
        // la misma descripción

        Usuario usuario = new Usuario("juan.gutierrez@gmail.com");
        Tarea tarea1 = new Tarea(usuario, "Práctica 1 de MADS", null);
        Tarea tarea2 = new Tarea(usuario, "Práctica 1 de MADS", null);
        Tarea tarea3 = new Tarea(usuario, "Pagar el alquiler", null);

        // THEN
        // son iguales (Equal) las tareas que tienen la misma descripción.

        assertThat(tarea1).isEqualTo(tarea2);
        assertThat(tarea1).isNotEqualTo(tarea3);
    }

    @Test
    public void comprobarIgualdadTareasConId() {
        // GIVEN
        // Creadas tres tareas con distintas descripciones y dos de ellas
        // con el mismo identificador,

        Usuario usuario = new Usuario("juan.gutierrez@gmail.com");
        Tarea tarea1 = new Tarea(usuario, "Práctica 1 de MADS", null);
        Tarea tarea2 = new Tarea(usuario, "Lavar la ropa", null);
        Tarea tarea3 = new Tarea(usuario, "Pagar el alquiler", null);
        tarea1.setId(1L);
        tarea2.setId(2L);
        tarea3.setId(1L);

        // THEN
        // son iguales (Equal) las tareas que tienen el mismo identificador.

        assertThat(tarea1).isEqualTo(tarea3);
        assertThat(tarea1).isNotEqualTo(tarea2);
    }

    //
    // Tests TareaRepository.
    // El código que trabaja con repositorios debe
    // estar en un entorno transactional, para que todas las peticiones
    // estén en la misma conexión a la base de datos, las entidades estén
    // conectadas y sea posible acceder a colecciones LAZY.
    //

    @Test
    @Transactional
    public void guardarTareaEnBaseDatos() {
        // GIVEN
        // Un usuario en la base de datos.

        Usuario usuario = new Usuario("user@ua");
        usuarioRepository.save(usuario);

        Tarea tarea = new Tarea(usuario, "Práctica 1 de MADS", null);

        // WHEN
        // salvamos la tarea en la BD,

        tareaRepository.save(tarea);

        // THEN
        // se actualiza el id de la tarea,

        assertThat(tarea.getId()).isNotNull();

        // y con ese identificador se recupera de la base de datos la tarea
        // con los valores correctos de las propiedades y la relación con
        // el usuario actualizado también correctamente (la relación entre tarea
        // y usuario es EAGER).

        Tarea tareaBD = tareaRepository.findById(tarea.getId()).orElse(null);
        assertThat(tareaBD.getTitulo()).isEqualTo(tarea.getTitulo());
        assertThat(tareaBD.getUsuario()).isEqualTo(usuario);
    }

    @Test
    @Transactional
    public void salvarTareaConUsuarioNoBDNoPersisteUsuario() {
        Usuario usuario = new Usuario("juan@example.com");
        Tarea tarea = new Tarea(usuario, "Práctica 1 de MADS", null);

        tareaRepository.save(tarea);

        assertThat(tarea.getId()).isNotNull();
        assertThat(usuario.getId()).isNull();  // usuario sigue sin persistir
    }


    @Test
    @Transactional
    public void unUsuarioTieneUnaListaDeTareas() {
        Usuario usuario = new Usuario("user@ua");
        usuarioRepository.save(usuario);

        Tarea tarea1 = new Tarea(usuario, "T1", null);
        Tarea tarea2 = new Tarea(usuario, "T2", null);

        tareaRepository.save(tarea1);
        tareaRepository.save(tarea2);

        Usuario usuarioBD = usuarioRepository.findById(usuario.getId()).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Debemos hacer explícita la consistencia inversa
        usuarioBD.addTarea(tarea1);
        usuarioBD.addTarea(tarea2);

        assertThat(usuarioBD.getTareas()).hasSize(2);
    }


    @Test
    @Transactional
    public void añadirUnaTareaAUnUsuarioEnBD() {
        Usuario usuario = new Usuario("user@ua");
        usuarioRepository.save(usuario);

        Tarea tarea = new Tarea(usuario, "Práctica 1", null);
        tareaRepository.save(tarea);

        Usuario usuarioBD = usuarioRepository.findById(usuario.getId()).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // La relación inversa NO es automática
        // Debemos mantenerla explícitamente en el dominio
        usuarioBD.addTarea(tarea);

        assertThat(usuarioBD.getTareas()).contains(tarea);
    }

    @Test
    @Transactional
    public void cambioEnLaEntidadEnTransactionalModificaLaBD() {
        // GIVEN
        // Un usuario y una tarea en la base de datos
        Usuario usuario = new Usuario("user@ua");
        usuarioRepository.save(usuario);
        Tarea tarea = new Tarea(usuario, "Práctica 1 de MADS", null);
        tareaRepository.save(tarea);

        // Recuperamos la tarea
        Long tareaId = tarea.getId();
        tarea = tareaRepository.findById(tareaId).orElse(null);

        // WHEN
        // modificamos la descripción de la tarea

        tarea.setTitulo("Esto es una prueba");

        // THEN
        // la descripción queda actualizada en la BD.

        Tarea tareaBD = tareaRepository.findById(tareaId).orElse(null);
        assertThat(tareaBD.getTitulo()).isEqualTo(tarea.getTitulo());
    }
}
