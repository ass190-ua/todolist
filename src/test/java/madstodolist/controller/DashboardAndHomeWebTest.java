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

import static org.mockito.ArgumentMatchers.anyLong;
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
}