package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.UsuarioData;
import madstodolist.service.EquipoService;
import madstodolist.service.TareaService;
import madstodolist.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class DashboardAndHomeWebTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ManagerUserSession managerUserSession;

    @MockBean
    private UsuarioService usuarioService;

    // AÑADIDO: Necesitamos mockear estos servicios porque el DashboardController los usa
    @MockBean
    private TareaService tareaService;

    @MockBean
    private EquipoService equipoService;

    // --- TESTS DE LA HOME (/) ---

    @Test
    public void home_sinLoguear_muestraLandingPage() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(null);

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("homepage"));
    }

    @Test
    public void home_logueado_redirigeADashboard() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(1L);

        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    // --- TESTS DEL DASHBOARD (/dashboard) ---

    @Test
    public void dashboard_sinLoguear_redirigeALogin() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(null);

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void dashboard_logueado_muestraVista() throws Exception {
        Long idUsuario = 1L;
        UsuarioData usuario = new UsuarioData();
        usuario.setId(idUsuario);
        usuario.setNombre("Test User");
        usuario.setEmail("test@ua.es");

        // Configuramos los mocks
        when(managerUserSession.usuarioLogeado()).thenReturn(idUsuario);
        when(usuarioService.findById(idUsuario)).thenReturn(usuario);

        // AÑADIDO: Configuramos los servicios para que devuelvan listas vacías y no null
        when(tareaService.allTareasUsuario(idUsuario)).thenReturn(Collections.emptyList());
        when(equipoService.findAllOrdenadoPorNombre()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attributeExists("usuario"))
                .andExpect(model().attributeExists("totalTareas"))
                // Comprobamos que el usuario llega a la vista
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Test User")));
    }

    @Test
    public void dashboard_muestraActividadReciente() throws Exception {
        Long idUsuario = 1L;
        UsuarioData usuario = new UsuarioData();
        usuario.setId(idUsuario);
        usuario.setNombre("Test User");
        usuario.setEmail("test@ua.es");

        // Crear tareas de prueba con diferentes estados
        java.util.List<madstodolist.dto.TareaData> tareas = new java.util.ArrayList<>();

        madstodolist.dto.TareaData tarea1 = new madstodolist.dto.TareaData();
        tarea1.setId(1L);
        tarea1.setTitulo("Tarea Pendiente");
        tarea1.setEstado("PENDIENTE");
        tareas.add(tarea1);

        madstodolist.dto.TareaData tarea2 = new madstodolist.dto.TareaData();
        tarea2.setId(2L);
        tarea2.setTitulo("Tarea En Curso");
        tarea2.setEstado("EN_CURSO");
        tareas.add(tarea2);

        madstodolist.dto.TareaData tarea3 = new madstodolist.dto.TareaData();
        tarea3.setId(3L);
        tarea3.setTitulo("Tarea Terminada");
        tarea3.setEstado("TERMINADA");
        tareas.add(tarea3);

        when(managerUserSession.usuarioLogeado()).thenReturn(idUsuario);
        when(usuarioService.findById(idUsuario)).thenReturn(usuario);
        when(tareaService.allTareasUsuario(idUsuario)).thenReturn(tareas);
        when(equipoService.findAllOrdenadoPorNombre()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attributeExists("ultimasTareas"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Tarea Pendiente")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Tarea En Curso")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Tarea Terminada")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("badge-info"))); // Badge de EN_CURSO
    }

    @Test
    public void dashboard_muestraFechasLimite() throws Exception {
        Long idUsuario = 1L;
        UsuarioData usuario = new UsuarioData();
        usuario.setId(idUsuario);
        usuario.setNombre("Test User");

        java.util.List<madstodolist.dto.TareaData> tareas = new java.util.ArrayList<>();

        madstodolist.dto.TareaData tareaConFecha = new madstodolist.dto.TareaData();
        tareaConFecha.setId(1L);
        tareaConFecha.setTitulo("Tarea con fecha");
        tareaConFecha.setEstado("PENDIENTE");
        tareaConFecha.setFechaLimite(java.time.LocalDate.of(2025, 12, 31));
        tareas.add(tareaConFecha);

        when(managerUserSession.usuarioLogeado()).thenReturn(idUsuario);
        when(usuarioService.findById(idUsuario)).thenReturn(usuario);
        when(tareaService.allTareasUsuario(idUsuario)).thenReturn(tareas);
        when(equipoService.findAllOrdenadoPorNombre()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("31/12/2025")));
    }

    @Test
    public void dashboard_muestraTareasSinFecha() throws Exception {
        Long idUsuario = 1L;
        UsuarioData usuario = new UsuarioData();
        usuario.setId(idUsuario);
        usuario.setNombre("Test User");

        java.util.List<madstodolist.dto.TareaData> tareas = new java.util.ArrayList<>();

        madstodolist.dto.TareaData tareaSinFecha = new madstodolist.dto.TareaData();
        tareaSinFecha.setId(1L);
        tareaSinFecha.setTitulo("Tarea sin fecha");
        tareaSinFecha.setEstado("PENDIENTE");
        tareaSinFecha.setFechaLimite(null);
        tareas.add(tareaSinFecha);

        when(managerUserSession.usuarioLogeado()).thenReturn(idUsuario);
        when(usuarioService.findById(idUsuario)).thenReturn(usuario);
        when(tareaService.allTareasUsuario(idUsuario)).thenReturn(tareas);
        when(equipoService.findAllOrdenadoPorNombre()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Sin fecha")));
    }

    @Test
    public void dashboard_muestraBotonesDeAccion() throws Exception {
        Long idUsuario = 1L;
        UsuarioData usuario = new UsuarioData();
        usuario.setId(idUsuario);
        usuario.setNombre("Test User");

        java.util.List<madstodolist.dto.TareaData> tareas = new java.util.ArrayList<>();

        madstodolist.dto.TareaData tarea = new madstodolist.dto.TareaData();
        tarea.setId(1L);
        tarea.setTitulo("Tarea Test");
        tarea.setEstado("PENDIENTE");
        tareas.add(tarea);

        when(managerUserSession.usuarioLogeado()).thenReturn(idUsuario);
        when(usuarioService.findById(idUsuario)).thenReturn(usuario);
        when(tareaService.allTareasUsuario(idUsuario)).thenReturn(tareas);
        when(equipoService.findAllOrdenadoPorNombre()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("bi-eye"))) // Icono de ver detalles
                .andExpect(content().string(org.hamcrest.Matchers.containsString("bi-pencil"))); // Icono de editar
    }

    @Test
    public void dashboard_muestraEstadisticasDeTareas() throws Exception {
        Long idUsuario = 1L;
        UsuarioData usuario = new UsuarioData();
        usuario.setId(idUsuario);
        usuario.setNombre("Test User");

        java.util.List<madstodolist.dto.TareaData> tareas = new java.util.ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            madstodolist.dto.TareaData tarea = new madstodolist.dto.TareaData();
            tarea.setId((long) i);
            tarea.setTitulo("Tarea " + i);
            tarea.setEstado(i <= 3 ? "TERMINADA" : "PENDIENTE");
            tareas.add(tarea);
        }

        when(managerUserSession.usuarioLogeado()).thenReturn(idUsuario);
        when(usuarioService.findById(idUsuario)).thenReturn(usuario);
        when(tareaService.allTareasUsuario(idUsuario)).thenReturn(tareas);
        when(equipoService.findAllOrdenadoPorNombre()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("totalTareas", 5L))
                .andExpect(model().attribute("tareasPendientes", 2L));
    }
}