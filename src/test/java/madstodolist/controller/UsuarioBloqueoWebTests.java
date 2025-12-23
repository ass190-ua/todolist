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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
public class UsuarioBloqueoWebTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UsuarioService usuarioService;

    Long adminId;
    Long victimaId;

    @BeforeEach
    void setup() {
        // Asegura un admin
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

        // Asegura un usuario víctima
        UsuarioData v = usuarioService.findByEmail("victima@ua.es");
        if (v == null) {
            v = new UsuarioData();
            v.setEmail("victima@ua.es");
            v.setPassword("12345678");
            v.setNombre("Victima");
            v = usuarioService.registrar(v);
        }
        victimaId = v.getId();
    }

    @Test
    void admin_puede_bloquear_usuario_y_login_falla() throws Exception {
        // Admin bloquea a la víctima
        mockMvc.perform(post("/registrados/{id}/bloqueo", victimaId)
                        .sessionAttr("idUsuarioLogeado", adminId))
                .andExpect(status().is3xxRedirection());

        // Login de víctima bloqueada
        mockMvc.perform(post("/login")
                        .param("eMail", "victima@ua.es")
                        .param("password", "12345678"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Tu cuenta está bloqueada")));
    }
}
