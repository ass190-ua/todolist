package madstodolist.controller;

import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import madstodolist.service.UsuarioServiceException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.mockito.ArgumentMatchers.eq;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
//
// A diferencia de los tests web de tarea, donde usábamos los datos
// de prueba de la base de datos, aquí vamos a practicar otro enfoque:
// moquear el usuarioService.
public class UsuarioWebTest {

    @Autowired
    private MockMvc mockMvc;

    // Moqueamos el usuarioService.
    // En los tests deberemos proporcionar el valor devuelto por las llamadas
    // a los métodos de usuarioService que se van a ejecutar cuando se realicen
    // las peticiones a los endpoint.
    @MockBean
    private UsuarioService usuarioService;

    @Test
    public void servicioLoginUsuarioOK() throws Exception {
        // GIVEN
        // Moqueamos la llamada a usuarioService.login para que
        // devuelva un LOGIN_OK y la llamada a usuarioServicie.findByEmail
        // para que devuelva un usuario determinado.

        UsuarioData anaGarcia = new UsuarioData();
        anaGarcia.setNombre("Ana García");
        anaGarcia.setId(1L);

        when(usuarioService.login("ana.garcia@gmail.com", "12345678"))
                .thenReturn(UsuarioService.LoginStatus.LOGIN_OK);
        when(usuarioService.findByEmail("ana.garcia@gmail.com"))
                .thenReturn(anaGarcia);

        // WHEN, THEN
        // Realizamos una petición POST al login pasando los datos
        // esperados en el mock, la petición devolverá una redirección a la
        // URL con las tareas del usuario

        this.mockMvc.perform(post("/login")
                        .param("eMail", "ana.garcia@gmail.com")
                        .param("password", "12345678"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    public void servicioLoginUsuarioNotFound() throws Exception {
        // GIVEN
        // Moqueamos el método usuarioService.login para que devuelva
        // USER_NOT_FOUND
        when(usuarioService.login("pepito.perez@gmail.com", "12345678"))
                .thenReturn(UsuarioService.LoginStatus.USER_NOT_FOUND);

        // WHEN, THEN
        // Realizamos una petición POST con los datos del usuario mockeado y
        // se debe devolver una página que contenga el mensaja "No existe usuario"
        this.mockMvc.perform(post("/login")
                        .param("eMail","pepito.perez@gmail.com")
                        .param("password","12345678"))
                .andExpect(content().string(containsString("No existe usuario")));
    }

    @Test
    public void servicioLoginUsuarioErrorPassword() throws Exception {
        // GIVEN
        // Moqueamos el método usuarioService.login para que devuelva
        // ERROR_PASSWORD
        when(usuarioService.login("ana.garcia@gmail.com", "000"))
                .thenReturn(UsuarioService.LoginStatus.ERROR_PASSWORD);

        // WHEN, THEN
        // Realizamos una petición POST con los datos del usuario mockeado y
        // se debe devolver una página que contenga el mensaja "Contraseña incorrecta"
        this.mockMvc.perform(post("/login")
                        .param("eMail","ana.garcia@gmail.com")
                        .param("password","000"))
                .andExpect(content().string(containsString("Contraseña incorrecta")));
    }

    @Test
    public void verPerfilMuestraDatosDelUsuarioLogeado() throws Exception {
        // GIVEN
        UsuarioData usuario = new UsuarioData();
        usuario.setId(1L);
        usuario.setNombre("Usuario Ejemplo");
        usuario.setEmail("user@ua");

        // El servicio devolverá este usuario cuando se le pida el id 1
        when(usuarioService.findById(1L)).thenReturn(usuario);

        // WHEN, THEN
        // Hacemos GET a /perfil simulando que el usuario 1 está logeado
        this.mockMvc.perform(get("/perfil")
                        .sessionAttr("idUsuarioLogeado", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("perfilUsuario"))
                .andExpect(model().attributeExists("usuario"))
                .andExpect(content().string(containsString("Usuario Ejemplo")));
    }

    @Test
    public void actualizarPerfilRedirigeConMensajeFlash() throws Exception {
        // GIVEN
        // No necesitamos que el servicio devuelva nada en concreto porque el
        // controlador sólo usa el id y redirige
        UsuarioData actualizado = new UsuarioData();
        actualizado.setId(1L);
        actualizado.setNombre("Nuevo Nombre");
        actualizado.setEmail("nuevo@ua");

        when(usuarioService.actualizarPerfil(eq(1L), any(UsuarioData.class)))
                .thenReturn(actualizado);

        // WHEN, THEN
        this.mockMvc.perform(post("/perfil")
                        .sessionAttr("idUsuarioLogeado", 1L)
                        .param("nombre", "Nuevo Nombre")
                        .param("email", "nuevo@ua"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/perfil"))
                .andExpect(flash().attribute("msgPerfilOk",
                        "Datos de usuario actualizados correctamente."));
    }

    @Test
    public void cambiarPasswordCorrectaRedirigeConMensajeOk() throws Exception {
        // GIVEN
        // No hace falta moquear nada especial: si cambiarPassword no lanza excepción
        // el controlador debe añadir msgPasswordOk y redirigir a /perfil
        // (usuario con id 1 logeado)
        // WHEN, THEN
        this.mockMvc.perform(post("/perfil/password")
                        .sessionAttr("idUsuarioLogeado", 1L)
                        .param("passwordActual", "123")
                        .param("passwordNueva", "456")
                        .param("passwordRepetida", "456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/perfil"))
                .andExpect(flash().attribute("msgPasswordOk",
                        "Contraseña actualizada correctamente."));
    }

    @Test
    public void cambiarPasswordConPasswordActualIncorrectaMuestraError() throws Exception {
        // GIVEN
        // Simulamos que el servicio lanza excepción cuando la contraseña actual no es correcta
        doThrow(new UsuarioServiceException("La contraseña actual no es correcta"))
                .when(usuarioService)
                .cambiarPassword(1L, "123", "456");

        // WHEN, THEN
        this.mockMvc.perform(post("/perfil/password")
                        .sessionAttr("idUsuarioLogeado", 1L)
                        .param("passwordActual", "123")
                        .param("passwordNueva", "456")
                        .param("passwordRepetida", "456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/perfil"))
                .andExpect(flash().attribute("msgPasswordError",
                        "La contraseña actual no es correcta"));
    }
}
