package madstodolist.controller;

import madstodolist.dto.EquipoData;
import madstodolist.dto.UsuarioData;
import madstodolist.service.EquipoService;
import madstodolist.service.EquipoServiceException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false) // desactiva login/csrf en tests
public class EquipoControllerWebTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    EquipoService equipoService;

    // GET /equipos
    @Test
    void listarEquipos_ok() throws Exception {
        EquipoData a = new EquipoData(); a.setId(1L); a.setNombre("Proyecto AAA");
        EquipoData b = new EquipoData(); b.setId(2L); b.setNombre("Proyecto BBB");
        Mockito.when(equipoService.findAllOrdenadoPorNombre())
                .thenReturn(Arrays.asList(a, b));

        mockMvc.perform(get("/equipos"))
                .andExpect(status().isOk())
                .andExpect(view().name("equipos"))
                .andExpect(model().attributeExists("equipos"))
                .andExpect(model().attribute("equipos", hasSize(2)))
                .andExpect(model().attribute("equipos",
                        hasItem(hasProperty("nombre", is("Proyecto AAA")))));
    }

    // GET /equipos/{id}
    @Test
    void detalleEquipo_ok() throws Exception {
        EquipoData equipo = new EquipoData(); equipo.setId(1L); equipo.setNombre("Proyecto 1");
        UsuarioData u = new UsuarioData(); u.setId(10L); u.setEmail("ana@ua.es");

        Mockito.when(equipoService.recuperarEquipo(1L)).thenReturn(equipo);
        Mockito.when(equipoService.usuariosEquipo(1L)).thenReturn(Collections.singletonList(u));

        mockMvc.perform(get("/equipos/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("equipo"))
                .andExpect(model().attribute("equipo", hasProperty("nombre", is("Proyecto 1"))))
                .andExpect(model().attribute("usuarios", hasSize(1)));
    }

    @Test
    void detalleEquipo_noExiste_devuelve404() throws Exception {
        Mockito.when(equipoService.recuperarEquipo(anyLong()))
                .thenThrow(new EquipoServiceException("Equipo no encontrado: 99"));

        mockMvc.perform(get("/equipos/99"))
                .andExpect(status().isNotFound());
    }

    // POST /equipos/nuevo
    @Test
    void crearEquipo_ok_conUsuarioLogueado() throws Exception {
        // simulamos sesión con usuario
        UsuarioData usuarioSesion = new UsuarioData();
        usuarioSesion.setId(42L);
        usuarioSesion.setEmail("test@ua.es");

        mockMvc.perform(post("/equipos/nuevo")
                        .param("nombre", "Nuevo Equipo")
                        .sessionAttr("usuarioSesion", usuarioSesion))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/equipos"));

        // se llama al servicio con el id del actor
        verify(equipoService, times(1)).crearEquipo(eq("Nuevo Equipo"), eq(42L));
    }

    @Test
    void crearEquipo_forbidden_sinSesion() throws Exception {
        mockMvc.perform(post("/equipos/nuevo").param("nombre", "X"))
                .andExpect(status().isForbidden());
    }

    // POST /equipos/{id}/miembro
    @Test
    void unirmeAEquipo_ok() throws Exception {
        UsuarioData usuarioSesion = new UsuarioData();
        usuarioSesion.setId(7L);
        usuarioSesion.setEmail("yo@ua.es");

        // el controlador exige ser admin según la versión actual -> stubearlo
        Mockito.when(equipoService.esAdminDeEquipo(5L, 7L)).thenReturn(true);

        mockMvc.perform(post("/equipos/5/miembro")
                        .sessionAttr("usuarioSesion", usuarioSesion))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/equipos/5"));

        verify(equipoService, times(1)).añadirUsuarioAEquipo(eq(5L), eq(7L));
    }

    @Test
    void unirmeAEquipo_forbidden_sinSesion() throws Exception {
        mockMvc.perform(post("/equipos/5/miembro"))
                .andExpect(status().isForbidden());
    }

    // POST /equipos/{id}/miembro/eliminar
    @Test
    void quitarmeDeEquipo_ok() throws Exception {
        UsuarioData usuarioSesion = new UsuarioData();
        usuarioSesion.setId(7L);
        usuarioSesion.setEmail("yo@ua.es");

        // el controlador exige ser admin según la versión actual -> stubearlo
        Mockito.when(equipoService.esAdminDeEquipo(5L, 7L)).thenReturn(true);

        mockMvc.perform(post("/equipos/5/miembro/eliminar")
                        .sessionAttr("usuarioSesion", usuarioSesion))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/equipos/5"));

        verify(equipoService, times(1)).quitarUsuarioDeEquipo(eq(5L), eq(7L));
    }

    @Test
    void quitarmeDeEquipo_forbidden_sinSesion() throws Exception {
        mockMvc.perform(post("/equipos/5/miembro/eliminar"))
                .andExpect(status().isForbidden());
    }

    @Test
    void editarEquipo_form_ok_admin() throws Exception {
        UsuarioData admin = new UsuarioData(); admin.setId(1L); admin.setAdmin(true);
        EquipoData equipo = new EquipoData(); equipo.setId(5L); equipo.setNombre("P1");

        Mockito.when(equipoService.recuperarEquipo(5L)).thenReturn(equipo);
        Mockito.when(equipoService.esAdminDeEquipo(5L, 1L)).thenReturn(true);

        mockMvc.perform(get("/equipos/5/editar").sessionAttr("usuarioSesion", admin))
                .andExpect(status().isOk())
                .andExpect(view().name("equipo-editar"));
    }

    @Test
    void editarEquipo_form_forbidden_noAdmin() throws Exception {
        UsuarioData user = new UsuarioData(); user.setId(2L); user.setAdmin(false);

        mockMvc.perform(get("/equipos/5/editar").sessionAttr("usuarioSesion", user))
                .andExpect(status().isForbidden());
    }

    @Test
    void actualizarEquipo_ok_admin() throws Exception {
        UsuarioData admin = new UsuarioData(); admin.setId(1L); admin.setAdmin(true);

        // asegurar que el controlador permite la operación (esAdmin)
        Mockito.when(equipoService.esAdminDeEquipo(5L, 1L)).thenReturn(true);

        mockMvc.perform(post("/equipos/5/editar")
                        .param("nombre", "Nuevo")
                        .sessionAttr("usuarioSesion", admin))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/equipos/5"));

        Mockito.verify(equipoService).actualizarNombreEquipo(5L, "Nuevo", 1L);
    }

    @Test
    void eliminarEquipo_ok_admin() throws Exception {
        UsuarioData admin = new UsuarioData(); admin.setId(1L); admin.setAdmin(true);

        Mockito.when(equipoService.esAdminDeEquipo(5L, 1L)).thenReturn(true);

        mockMvc.perform(post("/equipos/5/eliminar")
                        .sessionAttr("usuarioSesion", admin))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/equipos"));

        Mockito.verify(equipoService).eliminarEquipo(5L);
    }
}