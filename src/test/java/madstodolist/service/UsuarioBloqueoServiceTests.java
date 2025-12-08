package madstodolist.service;

import madstodolist.dto.UsuarioData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
public class UsuarioBloqueoServiceTests {

    @Autowired
    UsuarioService usuarioService;

    @Test
    void login_devuelve_BLOCKED_si_usuario_bloqueado() {
        // Crear o asegurar usuario
        UsuarioData u = usuarioService.findByEmail("bloq@ua.es");
        if (u == null) {
            u = new UsuarioData();
            u.setEmail("bloq@ua.es");
            u.setPassword("12345678");
            u.setNombre("Bloqueable");
            u = usuarioService.registrar(u);
        }

        // Bloquear
        usuarioService.toggleBloqueo(u.getId());

        // Login debe devolver BLOCKED
        UsuarioService.LoginStatus status = usuarioService.login("bloq@ua.es", "12345678");
        assertEquals(UsuarioService.LoginStatus.BLOCKED, status);

        // Habilitar de nuevo
        usuarioService.toggleBloqueo(u.getId());
        status = usuarioService.login("bloq@ua.es", "12345678");
        assertEquals(UsuarioService.LoginStatus.LOGIN_OK, status);
    }
}
