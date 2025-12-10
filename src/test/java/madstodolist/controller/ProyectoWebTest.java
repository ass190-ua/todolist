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
        // Crear usuarios reales en la BD
        UsuarioData admin = new UsuarioData();
        admin.setEmail("admin@ua");
        admin.setPassword("123");
        admin.setNombre("Admin");
        admin = usuarioService.registrar(admin);
        adminId = admin.getId();

        UsuarioData user = new UsuarioData();
        user.setEmail("user@ua");
        user.setPassword("123");
        user.setNombre("User");
        user = usuarioService.registrar(user);
        usuarioNormalId = user.getId();

        // Admin crea equipo y proyecto (Ahora funcionará porque adminId existe en BD)
        EquipoData equipo = equipoService.crearEquipo("Equipo A", adminId);

        ProyectoData p = new ProyectoData();
        p.setNombre("Proyecto A");
        p.setEquipoId(equipo.getId());
        p = proyectoService.crearProyecto(p);
        proyectoId = p.getId();

        // Crear tarea en el proyecto
        tareaService.nuevaTareaProyecto(proyectoId, adminId, "Tarea Test");
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

        TareaData tarea = tareaService.getTareasProyecto(proyectoId).get(0);
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
}