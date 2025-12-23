package madstodolist.service;

import madstodolist.dto.EquipoData;
import madstodolist.dto.ProyectoData;
import madstodolist.dto.TareaData;
import madstodolist.dto.UsuarioData;
import madstodolist.model.EstadoTarea;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Sql(scripts = "/clean-db.sql")
public class ProyectoServiceTest {

    @Autowired ProyectoService proyectoService;
    @Autowired EquipoService equipoService;
    @Autowired UsuarioService usuarioService;
    @Autowired TareaService tareaService;

    @Test
    public void crearProyectoEnEquipo() {
        // GIVEN: Usuario y Equipo
        UsuarioData user = new UsuarioData();
        user.setEmail("user@ua");
        user.setPassword("123");
        user = usuarioService.registrar(user);

        // El usuario crea el equipo (es admin)
        EquipoData equipo = equipoService.crearEquipo("Equipo Dev", user.getId());

        // WHEN: Creamos un proyecto para ese equipo
        ProyectoData creado = proyectoService.crearProyecto(
                "Proyecto Web",
                null,                       // descripción opcional
                equipo.getId()
        );

        // THEN: El proyecto existe y está vinculado
        assertThat(creado.getId()).isNotNull();

        List<ProyectoData> proyectos = proyectoService.findAllProyectosByEquipo(equipo.getId());
        assertThat(proyectos).hasSize(1);
        assertThat(proyectos.get(0).getNombre()).isEqualTo("Proyecto Web");
    }


    @Test
    public void crearTareaEnProyectoYCambiarEstado() {
        // GIVEN: Un usuario y un proyecto ya creado
        UsuarioData user = new UsuarioData();
        user.setEmail("u@ua");
        user.setPassword("1");
        UsuarioData registrado = usuarioService.registrar(user);

        EquipoData equipo = equipoService.crearEquipo("E1", registrado.getId());

        ProyectoData proyecto = proyectoService.crearProyecto(
                "P1",
                null,
                equipo.getId()
        );

        // WHEN: Creamos una tarea en el proyecto (NUEVA API)
        TareaData tarea = tareaService.nuevaTareaProyecto(
                proyecto.getId(),
                "Tarea Kanban",
                null
        );

        // THEN: La tarea nace en estado PENDIENTE
        assertThat(tarea.getEstado()).isEqualTo(EstadoTarea.PENDIENTE.toString());

        // WHEN: Cambiamos el estado
        tareaService.cambiarEstadoTarea(tarea.getId(), EstadoTarea.EN_CURSO);

        // THEN: El estado se actualiza
        TareaData tareaModificada = tareaService.findById(tarea.getId());
        assertThat(tareaModificada.getEstado()).isEqualTo(EstadoTarea.EN_CURSO.toString());
    }

}