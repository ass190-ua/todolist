package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.EquipoData;
import madstodolist.dto.UsuarioData;
import madstodolist.service.EquipoService;
import madstodolist.service.EquipoServiceException;
import madstodolist.service.ProyectoService;
import madstodolist.service.UsuarioService;
import madstodolist.service.TareaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/clean-db.sql")
public class EquipoControllerWebTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    EquipoService equipoService;

    @MockBean
    ProyectoService proyectoService;

    @MockBean
    UsuarioService usuarioService;

    @MockBean
    ManagerUserSession managerUserSession;

    @MockBean
    TareaService tareaService;

    // Configuración común para que la navbar no falle en ningún test
    @BeforeEach
    void setupUser() {
        UsuarioData usuarioDummy = new UsuarioData();
        usuarioDummy.setId(1L);
        usuarioDummy.setNombre("Usuario Test");
        // Cuando se pida cualquier usuario, devolvemos este dummy para evitar NPE en la vista
        when(usuarioService.findById(anyLong())).thenReturn(usuarioDummy);
    }

    @Test
    void listarEquipos_ok() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(1L);

        EquipoData a = new EquipoData(); a.setId(1L); a.setNombre("Proyecto AAA");
        EquipoData b = new EquipoData(); b.setId(2L); b.setNombre("Proyecto BBB");

        when(equipoService.equiposUsuario(1L)).thenReturn(Arrays.asList(a, b));

        mockMvc.perform(get("/equipos"))
                .andExpect(status().isOk())
                .andExpect(view().name("equipos"))
                .andExpect(model().attribute("equipos", hasSize(2)));
    }

    @Test
    void listarEquipos_soloMisEquipos() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(1L);

        EquipoData equipo1 = new EquipoData();
        equipo1.setId(1L);
        equipo1.setNombre("Mi Equipo");
        equipo1.setAdminUserId(1L);

        when(equipoService.equiposUsuario(1L)).thenReturn(Collections.singletonList(equipo1));

        mockMvc.perform(get("/equipos"))
                .andExpect(status().isOk())
                .andExpect(view().name("equipos"))
                .andExpect(model().attribute("equipos", hasSize(1)))
                .andExpect(model().attribute("equipos", hasItem(hasProperty("nombre", is("Mi Equipo")))));
    }

    @Test
    void listarEquipos_sinEquipos_muestraMensaje() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(1L);

        when(equipoService.equiposUsuario(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/equipos"))
                .andExpect(status().isOk())
                .andExpect(view().name("equipos"))
                .andExpect(model().attribute("equipos", hasSize(0)));
    }

    @Test
    void listarEquipos_muestraIndicadorAdmin() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(1L);

        EquipoData equipoComoAdmin = new EquipoData();
        equipoComoAdmin.setId(1L);
        equipoComoAdmin.setNombre("Equipo Admin");
        equipoComoAdmin.setAdminUserId(1L); // El usuario es admin

        EquipoData equipoComoMiembro = new EquipoData();
        equipoComoMiembro.setId(2L);
        equipoComoMiembro.setNombre("Equipo Miembro");
        equipoComoMiembro.setAdminUserId(2L); // Otro usuario es admin

        when(equipoService.equiposUsuario(1L)).thenReturn(Arrays.asList(equipoComoAdmin, equipoComoMiembro));

        mockMvc.perform(get("/equipos"))
                .andExpect(status().isOk())
                .andExpect(view().name("equipos"))
                .andExpect(model().attribute("equipos", hasSize(2)));
    }

    @Test
    void listarEquipos_sinLogin_muestraListaVacia() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(null);

        mockMvc.perform(get("/equipos"))
                .andExpect(status().isOk())
                .andExpect(view().name("equipos"))
                .andExpect(model().attribute("equipos", hasSize(0)))
                .andExpect(model().attribute("logeado", is(false)));
    }

    @Test
    void detalleEquipo_ok() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(1L);

        EquipoData equipo = new EquipoData(); equipo.setId(1L); equipo.setNombre("Proyecto 1");
        UsuarioData u = new UsuarioData(); u.setId(1L); u.setEmail("ana@ua.es");

        when(equipoService.recuperarEquipo(1L)).thenReturn(equipo);
        when(equipoService.usuariosEquipo(1L)).thenReturn(Collections.singletonList(u));
        when(tareaService.allTareasEquipo(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/equipos/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("equipo"))
                .andExpect(model().attribute("equipo", hasProperty("nombre", is("Proyecto 1"))));
    }

    @Test
    void detalleEquipo_noExiste_devuelve404() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(1L);

        when(equipoService.recuperarEquipo(anyLong()))
                .thenThrow(new EquipoServiceException("Equipo no encontrado"));

        mockMvc.perform(get("/equipos/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void crearEquipo_ok_conUsuarioLogueado() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(1L);

        mockMvc.perform(post("/equipos/nuevo")
                        .param("nombre", "Nuevo Equipo"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/equipos"));

        verify(equipoService).crearEquipo(eq("Nuevo Equipo"), eq(1L));
    }

    @Test
    void crearEquipo_redirect_sinSesion() throws Exception {
        // Aseguramos que devuelve null explícitamente
        when(managerUserSession.usuarioLogeado()).thenReturn(null);

        mockMvc.perform(post("/equipos/nuevo").param("nombre", "X"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void unirmeAEquipo_ok() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(1L);

        mockMvc.perform(post("/equipos/5/miembro"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/equipos/5"));

        verify(equipoService).añadirUsuarioAEquipo(eq(5L), eq(1L));
    }

    @Test
    void unirmeAEquipo_redirect_sinSesion() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(null);

        mockMvc.perform(post("/equipos/5/miembro"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void quitarmeDeEquipo_ok() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(1L);

        mockMvc.perform(post("/equipos/5/miembro/eliminar"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/equipos"));

        verify(equipoService).quitarUsuarioDeEquipo(eq(5L), eq(1L));
    }

    @Test
    void quitarmeDeEquipo_redirect_sinSesion() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(null);

        mockMvc.perform(post("/equipos/5/miembro/eliminar"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void editarEquipo_form_ok_admin() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(1L);

        EquipoData equipo = new EquipoData(); equipo.setId(5L); equipo.setNombre("P1");

        when(equipoService.recuperarEquipo(5L)).thenReturn(equipo);
        when(equipoService.esAdminDeEquipo(5L, 1L)).thenReturn(true);

        mockMvc.perform(get("/equipos/5/editar"))
                .andExpect(status().isOk())
                .andExpect(view().name("equipo-editar"));
    }

    @Test
    void editarEquipo_form_forbidden_noAdmin() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(2L);
        // Simulamos que el usuario 2 NO es admin del equipo 5
        when(equipoService.esAdminDeEquipo(5L, 2L)).thenReturn(false);

        mockMvc.perform(get("/equipos/5/editar"))
                .andExpect(status().isForbidden()) // Esperamos 403
                .andExpect(view().name("error")); // Y que cargue la vista de error
    }

    @Test
    void actualizarEquipo_ok_admin() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(1L);
        when(equipoService.esAdminDeEquipo(5L, 1L)).thenReturn(true);

        mockMvc.perform(post("/equipos/5/editar")
                        .param("nombre", "Nuevo"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/equipos/5"));

        verify(equipoService).actualizarNombreEquipo(5L, "Nuevo", 1L);
    }

    @Test
    void eliminarEquipo_ok_admin() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(1L);
        when(equipoService.esAdminDeEquipo(5L, 1L)).thenReturn(true);

        mockMvc.perform(post("/equipos/5/eliminar"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/equipos"));

        verify(equipoService).eliminarEquipo(5L);
    }
}