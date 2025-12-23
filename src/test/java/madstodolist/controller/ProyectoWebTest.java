package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.EquipoData;
import madstodolist.dto.ProyectoData;
import madstodolist.dto.TareaData;
import madstodolist.dto.UsuarioData;
import madstodolist.service.EquipoService;
import madstodolist.service.ProyectoService;
import madstodolist.service.TareaService;
import madstodolist.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/clean-db.sql")
public class ProyectoWebTest {

    @Autowired MockMvc mockMvc;

    // Usamos el servicio REAL para que los usuarios existan en la BD
    @Autowired UsuarioService usuarioService;

    @Autowired EquipoService equipoService;
    @Autowired ProyectoService proyectoService;
    @Autowired TareaService tareaService;

    @MockBean ManagerUserSession managerUserSession;

    Long adminId;
    Long usuarioNormalId;
    Long proyectoId;

    @BeforeEach
    void setup() {

        // -----------------------------
        // 1) Crear usuario admin
        // -----------------------------
        UsuarioData admin = new UsuarioData();
        admin.setEmail("admin@ua");
        admin.setPassword("123");
        admin.setNombre("Admin");
        admin = usuarioService.registrar(admin);
        adminId = admin.getId();

        // -----------------------------
        // 2) Crear usuario normal
        // -----------------------------
        UsuarioData user = new UsuarioData();
        user.setEmail("user@ua");
        user.setPassword("123");
        user.setNombre("User");
        user = usuarioService.registrar(user);
        usuarioNormalId = user.getId();

        // -----------------------------
        // 3) Admin crea equipo
        // -----------------------------
        EquipoData equipo = equipoService.crearEquipo("Equipo A", adminId);

        // -----------------------------
        // 4) Admin crea proyecto (NUEVA API)
        // -----------------------------
        ProyectoData proyecto = proyectoService.crearProyecto(
                "Proyecto A",      // nombre
                null,              // descripción opcional
                equipo.getId()     // equipo vinculado
        );
        proyectoId = proyecto.getId();

        // -----------------------------
        // 5) Crear tarea en proyecto (NUEVA API)
        // -----------------------------
        tareaService.nuevaTareaProyecto(
                proyectoId,
                "Tarea Test",
                null
        );
    }

    @Test
    void tableroKanban_accesoPermitidoMiembro() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(adminId);

        mockMvc.perform(get("/proyectos/" + proyectoId))
                .andExpect(status().isOk())
                .andExpect(view().name("proyecto"))
                .andExpect(content().string(containsString("Proyecto A")))
                .andExpect(content().string(containsString("Pendiente")))
                .andExpect(content().string(containsString("Tarea Test")));
    }

    @Test
    void tableroKanban_accesoDenegadoNoMiembro() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioNormalId);

        mockMvc.perform(get("/proyectos/" + proyectoId))
                .andExpect(status().isForbidden()) // 403
                .andExpect(view().name("error")); // Vista de error
    }

    @Test
    void moverTareaAJAX_funciona() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(adminId);

        // Buscamos la tarea creada en el setup
        TareaData tarea = tareaService.allTareasProyecto(proyectoId).get(0);
        Long tareaId = tarea.getId();

        String jsonBody = "{\"estado\": \"EN_CURSO\"}";

        mockMvc.perform(patch("/tareas/" + tareaId + "/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(content().string("OK"));

        TareaData tareaActualizada = tareaService.findById(tareaId);
        assertThat(tareaActualizada.getEstado()).isEqualTo("EN_CURSO");
    }

    @Test
    void moverTareaEntreTodosLosEstados() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(adminId);

        TareaData tarea = tareaService.allTareasProyecto(proyectoId).get(0);
        Long tareaId = tarea.getId();

        // PENDIENTE -> EN_CURSO
        mockMvc.perform(patch("/tareas/" + tareaId + "/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\": \"EN_CURSO\"}"))
                .andExpect(status().isOk());

        assertThat(tareaService.findById(tareaId).getEstado()).isEqualTo("EN_CURSO");

        // EN_CURSO -> TERMINADA
        mockMvc.perform(patch("/tareas/" + tareaId + "/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\": \"TERMINADA\"}"))
                .andExpect(status().isOk());

        assertThat(tareaService.findById(tareaId).getEstado()).isEqualTo("TERMINADA");

        // TERMINADA -> PENDIENTE (revertir)
        mockMvc.perform(patch("/tareas/" + tareaId + "/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\": \"PENDIENTE\"}"))
                .andExpect(status().isOk());

        assertThat(tareaService.findById(tareaId).getEstado()).isEqualTo("PENDIENTE");
    }

    @Test
    void kanbanMuestraTareasEnDiferentesColumnas() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(adminId);

        // Crear tareas en diferentes estados
        TareaData t1 = tareaService.nuevaTareaProyecto(proyectoId, "Tarea Pendiente", null);
        TareaData t2 = tareaService.nuevaTareaProyecto(proyectoId, "Tarea En Curso", null);
        TareaData t3 = tareaService.nuevaTareaProyecto(proyectoId, "Tarea Terminada", null);

        tareaService.cambiarEstadoTarea(t2.getId(), madstodolist.model.EstadoTarea.EN_CURSO);
        tareaService.cambiarEstadoTarea(t3.getId(), madstodolist.model.EstadoTarea.TERMINADA);

        mockMvc.perform(get("/proyectos/" + proyectoId))
                .andExpect(status().isOk())
                .andExpect(view().name("proyecto"))
                .andExpect(content().string(containsString("Tarea Pendiente")))
                .andExpect(content().string(containsString("Tarea En Curso")))
                .andExpect(content().string(containsString("Tarea Terminada")))
                .andExpect(content().string(containsString("columna-pendiente")))
                .andExpect(content().string(containsString("columna-en-curso")))
                .andExpect(content().string(containsString("columna-terminada")));
    }

    @Test
    void crearNuevaTareaDesdeFormulario() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(adminId);

        mockMvc.perform(get("/proyectos/" + proyectoId + "/tareas/nueva"))
                .andExpect(status().isOk())
                .andExpect(view().name("formNuevaTareaProyecto"))
                .andExpect(content().string(containsString("Proyecto A")));
    }

    @Test
    void crearNuevaTareaDesdeFormularioNoMiembro() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioNormalId);

        mockMvc.perform(get("/proyectos/" + proyectoId + "/tareas/nueva"))
                .andExpect(status().isForbidden());
    }

    @Test
    void moverTareaSinPermiso() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioNormalId);

        TareaData tarea = tareaService.allTareasProyecto(proyectoId).get(0);

        mockMvc.perform(patch("/tareas/" + tarea.getId() + "/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\": \"EN_CURSO\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void visualizarProyectoNoExistente() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(adminId);

        mockMvc.perform(get("/proyectos/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void kanbanConTareasSinFechaLimite() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(adminId);

        tareaService.nuevaTareaProyecto(proyectoId, "Tarea sin fecha", null);

        mockMvc.perform(get("/proyectos/" + proyectoId))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Tarea sin fecha")))
                .andExpect(content().string(containsString("—")));
    }

    @Test
    void kanbanConTareasConFechaLimite() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(adminId);

        tareaService.nuevaTareaProyecto(proyectoId, "Tarea con fecha", java.time.LocalDate.of(2025, 12, 31));

        mockMvc.perform(get("/proyectos/" + proyectoId))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Tarea con fecha")))
                .andExpect(content().string(containsString("31/12/2025")));
    }
}