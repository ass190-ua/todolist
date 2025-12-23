package madstodolist.repository;

import madstodolist.model.Equipo;
import madstodolist.model.Proyecto;
import madstodolist.model.Tarea;
import madstodolist.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Sql(scripts = "/clean-db.sql")
public class ProyectoTest {

    @Autowired
    private ProyectoRepository proyectoRepository;
    @Autowired
    private EquipoRepository equipoRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private TareaRepository tareaRepository;

    @Test
    @Transactional
    public void crearProyectoYGuardar() {
        // GIVEN: Un equipo existente
        Equipo equipo = new Equipo("Equipo A");
        equipoRepository.save(equipo);

        // WHEN: Creamos un proyecto asociado
        Proyecto proyecto = new Proyecto("Proyecto Alpha", equipo);
        proyectoRepository.save(proyecto);

        // THEN: Se genera ID y se guarda correctamente
        assertThat(proyecto.getId()).isNotNull();
        Proyecto proyectoBD = proyectoRepository.findById(proyecto.getId()).orElse(null);
        assertThat(proyectoBD).isNotNull();
        assertThat(proyectoBD.getNombre()).isEqualTo("Proyecto Alpha");
        assertThat(proyectoBD.getEquipo().getNombre()).isEqualTo("Equipo A");
    }

    @Test
    @Transactional
    public void relacionProyectoTareas() {
        // GIVEN: Usuario, Equipo y Proyecto
        Usuario usuario = new Usuario("user@ua");
        usuarioRepository.save(usuario);
        Equipo equipo = new Equipo("Equipo X");
        equipoRepository.save(equipo);
        Proyecto proyecto = new Proyecto("Proyecto X", equipo);
        proyectoRepository.save(proyecto);

        // WHEN: Creamos tareas asociadas al proyecto
        Tarea t1 = new Tarea(usuario, "Tarea 1", null);
        t1.setProyecto(proyecto);
        tareaRepository.save(t1);

        Tarea t2 = new Tarea(usuario, "Tarea 2", null);
        t2.setProyecto(proyecto);
        tareaRepository.save(t2);

        // THEN: Podemos recuperar las tareas desde el repositorio buscando por Proyecto
        List<Tarea> tareasProyecto = tareaRepository.findByProyectoId(proyecto.getId());
        assertThat(tareasProyecto).hasSize(2);
        assertThat(tareasProyecto).extracting(Tarea::getTitulo).contains("Tarea 1", "Tarea 2");
    }
}