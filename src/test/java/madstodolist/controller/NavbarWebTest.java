package madstodolist.controller;

import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
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
public class NavbarWebTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    UsuarioService usuarioService;

    @Test
    void about_sinLogin_muestraLoginYRegistro() throws Exception {
        mockMvc.perform(get("/about"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Login")))
                .andExpect(content().string(containsString("Registro")));
    }

    @Test
    void about_conLogin_muestraDropdownUsuarioYCerrarSesion() throws Exception {
        // Crear usuario de prueba con DTO
        UsuarioData dto = new UsuarioData();
        dto.setEmail("test@ua.es");
        dto.setNombre("Test");
        dto.setPassword("12345678");

        // registrar devuelve UsuarioData
        UsuarioData creado = usuarioService.registrar(dto);

        // En tu versión el id del DTO es muy probablemente 'getId()'
        Long id = creado.getId();

        mockMvc.perform(get("/about").sessionAttr("idUsuarioLogeado", id))
               .andExpect(status().isOk())
               .andExpect(content().string(containsString("Cerrar sesión")));
    }
}
