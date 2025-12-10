package madstodolist.controller;

import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
public class UsuariosProtegidosWebTests {

    @Autowired MockMvc mockMvc;
    @Autowired UsuarioService usuarioService;

    Long noAdminId;

    @BeforeEach
    void ensureUsers() {
        // Garantiza que existe un usuario NO admin para la prueba
        UsuarioData u = usuarioService.findByEmail("user@ua");
        if (u == null) {
            u = new UsuarioData();
            u.setEmail("user@ua");
            u.setPassword("12345678");
            u.setNombre("User");
            u.setAdmin(false);
            u = usuarioService.registrar(u);
        }
        noAdminId = u.getId();

        // Asegura que existe al menos un admin (útil para descripción de usuario)
        if (!usuarioService.existeAdministrador()) {
            UsuarioData a = new UsuarioData();
            a.setEmail("admin@ua.es");
            a.setPassword("12345678");
            a.setNombre("Admin");
            a.setAdmin(true);
            usuarioService.registrar(a);
        }
    }

    @Test
    public void listado_conAdmin_devuelveOK() throws Exception {
        UsuarioData admin = usuarioService.findByEmail("admin@ua.es");
        if (admin == null) {
            admin = new UsuarioData();
            admin.setEmail("admin@ua.es");
            admin.setPassword("123");
            admin.setAdmin(true);
            admin = usuarioService.registrar(admin);
        }

        mockMvc.perform(get("/registrados").sessionAttr("idUsuarioLogeado", admin.getId()))
                .andExpect(status().isOk())
                // Verificamos que aparece el título nuevo de la vista de administración
                .andExpect(content().string(containsString("Gestión de Usuarios")));
    }

    @Test
    public void listado_conNoAdmin_redirigeALogin() throws Exception {
        this.mockMvc.perform(get("/registrados"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    public void descripcion_conNoAdmin_redirigeALogin() throws Exception {
        this.mockMvc.perform(get("/registrados/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}
