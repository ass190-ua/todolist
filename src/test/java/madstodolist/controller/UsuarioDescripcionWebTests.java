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
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
public class UsuarioDescripcionWebTests {

    @Autowired MockMvc mockMvc;
    @Autowired UsuarioService usuarioService;

    private Long adminId;
    private Long anyUserIdToShow; // un usuario cualquiera para ver su ficha

    @BeforeEach
    void ensureAdminAndAUser() {
        // 1) Asegura admin
        UsuarioData admin = usuarioService.findByEmail("admin@ua.es");
        if (admin == null) {
            admin = new UsuarioData();
            admin.setEmail("admin@ua.es");
            admin.setPassword("12345678");
            admin.setNombre("Admin");
            admin.setAdmin(true);
            admin = usuarioService.registrar(admin);
        }
        adminId = admin.getId();

        // 2) Asegura un usuario (no importa si es admin o no) cuya ficha podamos consultar
        UsuarioData user = usuarioService.findByEmail("user@ua");
        if (user == null) {
            user = new UsuarioData();
            user.setEmail("user@ua");
            user.setPassword("12345678");
            user.setNombre("User");
            user.setAdmin(false);
            user = usuarioService.registrar(user);
        }
        anyUserIdToShow = user.getId();
    }

    @Test
    void descripcion_conLogin_muestraDatosSinPassword() throws Exception {
        mockMvc.perform(get("/registrados/" + anyUserIdToShow)
                        .sessionAttr("idUsuarioLogeado", adminId))  // <-- sesión de ADMIN
                .andExpect(status().isOk())
                .andExpect(view().name("descripcionUsuario"))
                .andExpect(content().string(containsString("Detalle de Usuario")))
                .andExpect(content().string(containsString("user@ua")))
                .andExpect(content().string(not(containsString("Contraseña"))));
    }

    @Test
    void descripcion_sinLogin_redirigeALogin() throws Exception {
        mockMvc.perform(get("/registrados/" + anyUserIdToShow))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}