package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.controller.exception.UsuarioNoAutorizadoException;
import madstodolist.controller.exception.UsuarioNoLogeadoException;
import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import madstodolist.dto.PasswordChangeData;
import madstodolist.service.UsuarioServiceException;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class UsuarioController {

    @Autowired UsuarioService usuarioService;
    @Autowired ManagerUserSession managerUserSession;

    private void requireLogin() {
        if (managerUserSession.usuarioLogeado() == null) {
            throw new UsuarioNoLogeadoException();
        }
    }

    private void requireAdmin() {
        Long id = managerUserSession.usuarioLogeado();
        UsuarioData u = usuarioService.findById(id);
        if (u == null || !Boolean.TRUE.equals(u.getAdmin())) {
            throw new UsuarioNoAutorizadoException();
        }
    }

    @GetMapping({"/usuarios", "/registrados"})
    public String listadoUsuarios(Model model) {
        requireLogin();
        requireAdmin();
        List<UsuarioData> usuarios = usuarioService.findAll();
        model.addAttribute("usuarios", usuarios);
        return "listaUsuarios";
    }

    @GetMapping("/registrados/{id}")
    public String descripcionUsuario(@PathVariable Long id, Model model) {
        requireLogin();
        requireAdmin(); // <- sólo admin
        UsuarioData usuario = usuarioService.findById(id);
        if (usuario == null) throw new RuntimeException("Usuario no encontrado");
        model.addAttribute("usuario", usuario);
        return "descripcionUsuario";
    }

    @PostMapping("/registrados/{id}/bloqueo")
    public String toggleBloqueo(@PathVariable Long id, RedirectAttributes ra) {
        // Reutilizamos tus helpers
        requireLogin();
        requireAdmin();

        // Evita que el admin se bloquee a sí mismo (opcional pero recomendable)
        Long idSesion = managerUserSession.usuarioLogeado();
        if (idSesion != null && idSesion.equals(id)) {
            ra.addFlashAttribute("mensaje", "No puedes bloquear tu propia cuenta.");
            return "redirect:/registrados"; // o /usuarios si usas ese alias
        }

        UsuarioData actualizado = usuarioService.toggleBloqueo(id);
        if (actualizado == null) {
            ra.addFlashAttribute("mensaje", "Usuario no encontrado.");
        } else {
            boolean bloqueado = Boolean.TRUE.equals(actualizado.getBloqueado());
            ra.addFlashAttribute("mensaje",
                    "Usuario " + actualizado.getEmail() + (bloqueado ? " bloqueado" : " habilitado"));
        }
        return "redirect:/registrados"; // o /usuarios si prefieres
    }

    @GetMapping("/perfil")
    public String verPerfil(Model model) {
        requireLogin();
        Long idLogeado = managerUserSession.usuarioLogeado();
        UsuarioData usuario = usuarioService.findById(idLogeado);

        if (usuario == null) {
            throw new UsuarioNoAutorizadoException();
        }

        model.addAttribute("usuario", usuario);
        model.addAttribute("passwordForm", new PasswordChangeData());
        return "perfilUsuario";
    }

    @PostMapping("/perfil")
    public String actualizarPerfil(@ModelAttribute("usuario") UsuarioData usuarioForm,
                                   RedirectAttributes ra) {
        requireLogin();
        Long idLogeado = managerUserSession.usuarioLogeado();

        usuarioService.actualizarPerfil(idLogeado, usuarioForm);
        ra.addFlashAttribute("msgPerfilOk", "Datos de usuario actualizados correctamente.");
        return "redirect:/perfil";
    }

    @PostMapping("/perfil/password")
    public String cambiarPassword(@ModelAttribute("passwordForm") PasswordChangeData form,
                                  RedirectAttributes ra) {
        requireLogin();
        Long idLogeado = managerUserSession.usuarioLogeado();

        if (form.getPasswordNueva() == null || !form.getPasswordNueva().equals(form.getPasswordRepetida())) {
            ra.addFlashAttribute("msgPasswordError", "Las contraseñas nuevas no coinciden.");
            return "redirect:/perfil";
        }

        try {
            usuarioService.cambiarPassword(idLogeado,
                    form.getPasswordActual(),
                    form.getPasswordNueva());
            ra.addFlashAttribute("msgPasswordOk", "Contraseña actualizada correctamente.");
        } catch (UsuarioServiceException e) {
            ra.addFlashAttribute("msgPasswordError", e.getMessage());
        }

        return "redirect:/perfil";
    }

    @PostMapping("/usuarios/{id}/eliminar")
    public String eliminarUsuario(@PathVariable Long id, javax.servlet.http.HttpSession session) {
        requireLogin();

        // SEGURIDAD: Solo el propio usuario puede borrar su cuenta
        Long idLogeado = managerUserSession.usuarioLogeado();
        if (!id.equals(idLogeado)) {
            throw new UsuarioNoAutorizadoException();
        }

        // Borramos usuario y sus datos
        usuarioService.borrarUsuario(id);

        // Cerramos sesión manualmente
        managerUserSession.logout();
        session.invalidate(); // Destruye la sesión de Spring

        return "redirect:/login?deleted=true";
    }
}
