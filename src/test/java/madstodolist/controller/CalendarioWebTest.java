package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.TareaData;
import madstodolist.dto.UsuarioData;
import madstodolist.dto.EquipoData;
import madstodolist.dto.ProyectoData;
import madstodolist.service.TareaService;
import madstodolist.service.UsuarioService;
import madstodolist.service.EquipoService;
import madstodolist.service.ProyectoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/clean-db.sql")
public class CalendarioWebTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TareaService tareaService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private EquipoService equipoService;

    @Autowired
    private ProyectoService proyectoService;

    @MockBean
    private ManagerUserSession managerUserSession;

    private UsuarioData crearUsuario(String email) {
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail(email);
        usuario.setPassword("123");
        return usuarioService.registrar(usuario);
    }

    @Test
    public void calendarioDevuelveVistaCorrecta() throws Exception {
        UsuarioData usuario = crearUsuario("user@ua");
        when(managerUserSession.usuarioLogeado()).thenReturn(usuario.getId());

        String url = "/usuarios/" + usuario.getId() + "/calendario";

        this.mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(view().name("calendario"));
    }

    @Test
    public void calendarioMuestraTituloYElementosBasicos() throws Exception {
        UsuarioData usuario = crearUsuario("user@ua");
        when(managerUserSession.usuarioLogeado()).thenReturn(usuario.getId());

        String url = "/usuarios/" + usuario.getId() + "/calendario";

        this.mockMvc.perform(get(url))
                .andExpect(content().string(containsString("Calendario de Tareas")))
                .andExpect(content().string(containsString("id=\"calendar\"")))
                .andExpect(content().string(containsString("prevMonth")))
                .andExpect(content().string(containsString("nextMonth")))
                .andExpect(content().string(containsString("currentMonth")));
    }

    @Test
    public void calendarioMuestraLeyendaDeEstados() throws Exception {
        UsuarioData usuario = crearUsuario("user@ua");
        when(managerUserSession.usuarioLogeado()).thenReturn(usuario.getId());

        String url = "/usuarios/" + usuario.getId() + "/calendario";

        this.mockMvc.perform(get(url))
                .andExpect(content().string(containsString("Leyenda de Estados")))
                .andExpect(content().string(containsString("Terminada")))
                .andExpect(content().string(containsString("En Progreso")))
                .andExpect(content().string(containsString("Pendiente")));
    }

    @Test
    public void calendarioMuestraTiposDeTarea() throws Exception {
        UsuarioData usuario = crearUsuario("user@ua");
        when(managerUserSession.usuarioLogeado()).thenReturn(usuario.getId());

        String url = "/usuarios/" + usuario.getId() + "/calendario";

        this.mockMvc.perform(get(url))
                .andExpect(content().string(containsString("Tipos de Tarea")))
                .andExpect(content().string(containsString("Personal")))
                .andExpect(content().string(containsString("Equipo")))
                .andExpect(content().string(containsString("Proyecto")));
    }

    @Test
    public void calendarioContieneModalParaTareas() throws Exception {
        UsuarioData usuario = crearUsuario("user@ua");
        when(managerUserSession.usuarioLogeado()).thenReturn(usuario.getId());

        String url = "/usuarios/" + usuario.getId() + "/calendario";

        this.mockMvc.perform(get(url))
                .andExpect(content().string(containsString("id=\"tareasModal\"")))
                .andExpect(content().string(containsString("id=\"tareaDetalleModal\"")))
                .andExpect(content().string(containsString("modalTitle")))
                .andExpect(content().string(containsString("modalBody")));
    }

    @Test
    public void calendarioIncluyeTareasEnJavaScript() throws Exception {
        UsuarioData usuario = crearUsuario("user@ua");
        tareaService.nuevaTareaUsuario(usuario.getId(), "Tarea con fecha", LocalDate.of(2025, 3, 15));
        when(managerUserSession.usuarioLogeado()).thenReturn(usuario.getId());

        String url = "/usuarios/" + usuario.getId() + "/calendario";

        this.mockMvc.perform(get(url))
                .andExpect(content().string(containsString("const tareas =")))
                .andExpect(content().string(containsString("Tarea con fecha")));
    }

    @Test
    public void calendarioIncluyeUsuarioEnJavaScript() throws Exception {
        UsuarioData usuario = crearUsuario("user@ua");
        when(managerUserSession.usuarioLogeado()).thenReturn(usuario.getId());

        String url = "/usuarios/" + usuario.getId() + "/calendario";

        this.mockMvc.perform(get(url))
                .andExpect(content().string(containsString("const usuario =")));
    }

    @Test
    public void calendarioConTareasPersonales() throws Exception {
        UsuarioData usuario = crearUsuario("user@ua");
        tareaService.nuevaTareaUsuario(usuario.getId(), "Tarea Personal", LocalDate.of(2025, 12, 25));
        when(managerUserSession.usuarioLogeado()).thenReturn(usuario.getId());

        String url = "/usuarios/" + usuario.getId() + "/calendario";

        this.mockMvc.perform(get(url))
                .andExpect(model().attributeExists("tareas"))
                .andExpect(model().attributeExists("usuario"))
                .andExpect(content().string(containsString("Tarea Personal")));
    }

    @Test
    public void calendarioConTareasDeEquipo() throws Exception {
        UsuarioData usuario = crearUsuario("equipouser@ua");
        EquipoData equipo = equipoService.crearEquipo("Equipo Test", usuario.getId());
        TareaData tareaEquipo = tareaService.nuevaTareaEquipo(equipo.getId(), "Tarea de Equipo", LocalDate.of(2025, 12, 20));
        tareaService.actualizarUsuario(tareaEquipo.getId(), usuario.getId());

        when(managerUserSession.usuarioLogeado()).thenReturn(usuario.getId());

        String url = "/usuarios/" + usuario.getId() + "/calendario";

        this.mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("tareas"))
                .andExpect(model().attributeExists("equipos"))
                .andExpect(content().string(containsString("Tarea de Equipo")))
                .andExpect(content().string(containsString("const equipos =")));
    }

    @Test
    public void calendarioConTareasDeProyecto() throws Exception {
        UsuarioData usuario = crearUsuario("proyectouser@ua");
        EquipoData equipo = equipoService.crearEquipo("Equipo Dev", usuario.getId());
        ProyectoData proyecto = proyectoService.crearProyecto("Proyecto Web", "Descripción", equipo.getId());

        TareaData tareaProyecto = tareaService.nuevaTareaProyecto(
            proyecto.getId(), "Tarea de Proyecto", LocalDate.of(2025, 12, 30)
        );
        tareaService.actualizarUsuario(tareaProyecto.getId(), usuario.getId());

        when(managerUserSession.usuarioLogeado()).thenReturn(usuario.getId());

        String url = "/usuarios/" + usuario.getId() + "/calendario";

        this.mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("tareas"))
                .andExpect(model().attributeExists("proyectos"))
                .andExpect(content().string(containsString("Tarea de Proyecto")))
                .andExpect(content().string(containsString("const proyectos =")));
    }

    @Test
    public void calendarioMuestraEquiposDelUsuario() throws Exception {
        UsuarioData usuario = crearUsuario("multiequipo@ua");
        equipoService.crearEquipo("Equipo Alpha", usuario.getId());
        equipoService.crearEquipo("Equipo Beta", usuario.getId());

        when(managerUserSession.usuarioLogeado()).thenReturn(usuario.getId());

        String url = "/usuarios/" + usuario.getId() + "/calendario";

        this.mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("equipos"))
                .andExpect(content().string(containsString("Equipo Alpha")))
                .andExpect(content().string(containsString("Equipo Beta")));
    }

    @Test
    public void calendarioMuestraProyectosDelUsuario() throws Exception {
        UsuarioData usuario = crearUsuario("multiproyecto@ua");
        EquipoData equipo = equipoService.crearEquipo("Equipo Proyectos", usuario.getId());
        proyectoService.crearProyecto("Proyecto A", null, equipo.getId());
        proyectoService.crearProyecto("Proyecto B", null, equipo.getId());

        when(managerUserSession.usuarioLogeado()).thenReturn(usuario.getId());

        String url = "/usuarios/" + usuario.getId() + "/calendario";

        this.mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("proyectos"))
                .andExpect(content().string(containsString("Proyecto A")))
                .andExpect(content().string(containsString("Proyecto B")));
    }

    @Test
    public void calendarioConTareasSinFecha() throws Exception {
        UsuarioData usuario = crearUsuario("user@ua");
        tareaService.nuevaTareaUsuario(usuario.getId(), "Tarea sin fecha", null);
        when(managerUserSession.usuarioLogeado()).thenReturn(usuario.getId());

        String url = "/usuarios/" + usuario.getId() + "/calendario";

        this.mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("tareas"));
    }

    @Test
    public void calendarioTieneLinkAListaDeTareas() throws Exception {
        UsuarioData usuario = crearUsuario("user@ua");
        when(managerUserSession.usuarioLogeado()).thenReturn(usuario.getId());

        String url = "/usuarios/" + usuario.getId() + "/calendario";

        this.mockMvc.perform(get(url))
                .andExpect(content().string(containsString("Ver Lista de Tareas")))
                .andExpect(content().string(containsString("/usuarios/" + usuario.getId() + "/tareas")));
    }

    @Test
    public void calendarioRequiereUsuarioLogeado() throws Exception {
        UsuarioData usuario = crearUsuario("user@ua");
        Long otroUsuarioId = usuario.getId() + 1;

        when(managerUserSession.usuarioLogeado()).thenReturn(otroUsuarioId);

        String url = "/usuarios/" + usuario.getId() + "/calendario";

        this.mockMvc.perform(get(url))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void calendarioConVariasTareasEnMismoDia() throws Exception {
        UsuarioData usuario = crearUsuario("user@ua");

        LocalDate fecha = LocalDate.of(2025, 12, 25);
        tareaService.nuevaTareaUsuario(usuario.getId(), "Tarea 1", fecha);
        tareaService.nuevaTareaUsuario(usuario.getId(), "Tarea 2", fecha);
        tareaService.nuevaTareaUsuario(usuario.getId(), "Tarea 3", fecha);
        tareaService.nuevaTareaUsuario(usuario.getId(), "Tarea 4", fecha);

        when(managerUserSession.usuarioLogeado()).thenReturn(usuario.getId());

        String url = "/usuarios/" + usuario.getId() + "/calendario";

        this.mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Tarea 1")))
                .andExpect(content().string(containsString("Tarea 2")))
                .andExpect(content().string(containsString("Tarea 3")))
                .andExpect(content().string(containsString("Tarea 4")));
    }

    @Test
    public void calendarioModalTieneBotonEliminar() throws Exception {
        UsuarioData usuario = crearUsuario("user@ua");
        when(managerUserSession.usuarioLogeado()).thenReturn(usuario.getId());

        String url = "/usuarios/" + usuario.getId() + "/calendario";

        this.mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("btnEliminarTarea")))
                .andExpect(content().string(containsString("Eliminar Tarea")))
                .andExpect(content().string(containsString("bi-trash")));
    }

    @Test
    public void calendarioModalTieneBotonEditar() throws Exception {
        UsuarioData usuario = crearUsuario("user@ua");
        when(managerUserSession.usuarioLogeado()).thenReturn(usuario.getId());

        String url = "/usuarios/" + usuario.getId() + "/calendario";

        this.mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("btnEditarTarea")))
                .andExpect(content().string(containsString("Editar Tarea")))
                .andExpect(content().string(containsString("bi-pencil")));
    }

    @Test
    public void eliminarTareaPersonalDesdeCalendario() throws Exception {
        UsuarioData usuario = crearUsuario("user@ua");
        TareaData tarea = tareaService.nuevaTareaUsuario(usuario.getId(), "Tarea a eliminar", LocalDate.of(2025, 12, 25));
        when(managerUserSession.usuarioLogeado()).thenReturn(usuario.getId());

        String url = "/tareas/" + tarea.getId();

        this.mockMvc.perform(delete(url))
                .andExpect(status().isOk());

        // Verificar que la tarea fue eliminada
        assert tareaService.findById(tarea.getId()) == null;
    }

    @Test
    public void eliminarTareaDeEquipoDesdeCalendario() throws Exception {
        UsuarioData usuario = crearUsuario("equipouser@ua");
        EquipoData equipo = equipoService.crearEquipo("Equipo Test", usuario.getId());
        TareaData tareaEquipo = tareaService.nuevaTareaEquipo(equipo.getId(), "Tarea de equipo a eliminar", LocalDate.of(2025, 12, 20));
        tareaService.actualizarUsuario(tareaEquipo.getId(), usuario.getId());

        when(managerUserSession.usuarioLogeado()).thenReturn(usuario.getId());

        String url = "/tareas/" + tareaEquipo.getId();

        this.mockMvc.perform(delete(url))
                .andExpect(status().isOk());

        // Verificar que la tarea fue eliminada
        assert tareaService.findById(tareaEquipo.getId()) == null;
    }

    @Test
    public void eliminarTareaDeProyectoDesdeCalendario() throws Exception {
        UsuarioData usuario = crearUsuario("proyectouser@ua");
        EquipoData equipo = equipoService.crearEquipo("Equipo Dev", usuario.getId());
        ProyectoData proyecto = proyectoService.crearProyecto("Proyecto Web", "Descripción", equipo.getId());

        TareaData tareaProyecto = tareaService.nuevaTareaProyecto(
            proyecto.getId(), "Tarea de proyecto a eliminar", LocalDate.of(2025, 12, 30)
        );
        tareaService.actualizarUsuario(tareaProyecto.getId(), usuario.getId());

        when(managerUserSession.usuarioLogeado()).thenReturn(usuario.getId());

        String url = "/tareas/" + tareaProyecto.getId();

        this.mockMvc.perform(delete(url))
                .andExpect(status().isOk());

        // Verificar que la tarea fue eliminada
        assert tareaService.findById(tareaProyecto.getId()) == null;
    }

    @Test
    public void noSePuedeEliminarTareaDeOtroUsuario() throws Exception {
        UsuarioData usuario1 = crearUsuario("user1@ua");
        UsuarioData usuario2 = crearUsuario("user2@ua");
        TareaData tarea = tareaService.nuevaTareaUsuario(usuario1.getId(), "Tarea de usuario1", LocalDate.of(2025, 12, 25));

        when(managerUserSession.usuarioLogeado()).thenReturn(usuario2.getId());

        String url = "/tareas/" + tarea.getId();

        this.mockMvc.perform(delete(url))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    public void calendarioEquipoDevuelveVistaCorrecta() throws Exception {
        UsuarioData usuario = crearUsuario("user@ua");
        EquipoData equipo = equipoService.crearEquipo("Equipo Test", usuario.getId());
        when(managerUserSession.usuarioLogeado()).thenReturn(usuario.getId());

        String url = "/equipos/" + equipo.getId() + "/calendario";

        this.mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(view().name("calendarioEquipo"));
    }

    @Test
    public void calendarioEquipoMuestraTituloYNombreEquipo() throws Exception {
        UsuarioData usuario = crearUsuario("user@ua");
        EquipoData equipo = equipoService.crearEquipo("Equipo Calendario", usuario.getId());
        when(managerUserSession.usuarioLogeado()).thenReturn(usuario.getId());

        String url = "/equipos/" + equipo.getId() + "/calendario";

        this.mockMvc.perform(get(url))
                .andExpect(content().string(containsString("Calendario de")))
                .andExpect(content().string(containsString("Equipo Calendario")))
                .andExpect(content().string(containsString("id=\"calendar\"")));
    }

    @Test
    public void calendarioEquipoMuestraTareasDelEquipo() throws Exception {
        UsuarioData usuario = crearUsuario("user@ua");
        EquipoData equipo = equipoService.crearEquipo("Equipo Test", usuario.getId());
        TareaData tareaEquipo = tareaService.nuevaTareaEquipo(equipo.getId(), "Tarea del Equipo", LocalDate.of(2025, 12, 25));
        tareaService.actualizarUsuario(tareaEquipo.getId(), usuario.getId());

        when(managerUserSession.usuarioLogeado()).thenReturn(usuario.getId());

        String url = "/equipos/" + equipo.getId() + "/calendario";

        this.mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("tareas"))
                .andExpect(content().string(containsString("Tarea del Equipo")));
    }

    @Test
    public void calendarioEquipoMuestraTareasDeProyectos() throws Exception {
        UsuarioData usuario = crearUsuario("user@ua");
        EquipoData equipo = equipoService.crearEquipo("Equipo Dev", usuario.getId());
        ProyectoData proyecto = proyectoService.crearProyecto("Proyecto X", "Descripción", equipo.getId());
        TareaData tareaProyecto = tareaService.nuevaTareaProyecto(proyecto.getId(), "Tarea del Proyecto", LocalDate.of(2025, 12, 20));
        tareaService.actualizarUsuario(tareaProyecto.getId(), usuario.getId());

        when(managerUserSession.usuarioLogeado()).thenReturn(usuario.getId());

        String url = "/equipos/" + equipo.getId() + "/calendario";

        this.mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("tareas"))
                .andExpect(model().attributeExists("proyectos"))
                .andExpect(content().string(containsString("Tarea del Proyecto")));
    }


    @Test
    public void calendarioEquipoModalTieneBotonEditar() throws Exception {
        UsuarioData usuario = crearUsuario("user@ua");
        EquipoData equipo = equipoService.crearEquipo("Equipo Test", usuario.getId());
        when(managerUserSession.usuarioLogeado()).thenReturn(usuario.getId());

        String url = "/equipos/" + equipo.getId() + "/calendario";

        this.mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("btnEditarTarea")))
                .andExpect(content().string(containsString("Editar Tarea")))
                .andExpect(content().string(containsString("bi-pencil")));
    }

    @Test
    public void calendarioEquipoTieneLinkVolverAEquipo() throws Exception {
        UsuarioData usuario = crearUsuario("user@ua");
        EquipoData equipo = equipoService.crearEquipo("Equipo Test", usuario.getId());
        when(managerUserSession.usuarioLogeado()).thenReturn(usuario.getId());

        String url = "/equipos/" + equipo.getId() + "/calendario";

        this.mockMvc.perform(get(url))
                .andExpect(content().string(containsString("Volver al Equipo")))
                .andExpect(content().string(containsString("/equipos/" + equipo.getId())));
    }

    @Test
    public void eliminarTareaDeEquipoMiembroDesdeCalendarioEquipo() throws Exception {
        UsuarioData adminUsuario = crearUsuario("admin@ua");
        UsuarioData miembroUsuario = crearUsuario("miembro@ua");
        EquipoData equipo = equipoService.crearEquipo("Equipo Test", adminUsuario.getId());
        equipoService.añadirUsuarioAEquipo(equipo.getId(), miembroUsuario.getId());

        TareaData tareaEquipo = tareaService.nuevaTareaEquipo(equipo.getId(), "Tarea a eliminar", LocalDate.of(2025, 12, 25));
        tareaService.actualizarUsuario(tareaEquipo.getId(), miembroUsuario.getId());

        when(managerUserSession.usuarioLogeado()).thenReturn(miembroUsuario.getId());

        String url = "/tareas/" + tareaEquipo.getId();

        this.mockMvc.perform(delete(url))
                .andExpect(status().isOk());

        // Verificar que la tarea fue eliminada
        assert tareaService.findById(tareaEquipo.getId()) == null;
    }

    @Test
    public void noSePuedeEliminarTareaDeEquipoSiNoEresMiembro() throws Exception {
        UsuarioData adminUsuario = crearUsuario("admin@ua");
        UsuarioData otroUsuario = crearUsuario("otro@ua");
        EquipoData equipo = equipoService.crearEquipo("Equipo Test", adminUsuario.getId());

        TareaData tareaEquipo = tareaService.nuevaTareaEquipo(equipo.getId(), "Tarea del equipo", LocalDate.of(2025, 12, 25));

        when(managerUserSession.usuarioLogeado()).thenReturn(otroUsuario.getId());

        String url = "/tareas/" + tareaEquipo.getId();

        this.mockMvc.perform(delete(url))
                .andExpect(status().is3xxRedirection());
    }
}
