package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.LoginData;
import madstodolist.dto.RegistroData;
import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@Controller
public class LoginController {

    @Autowired
    UsuarioService usuarioService;

    @Autowired
    ManagerUserSession managerUserSession;

    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("loginData", new LoginData());
        return "formLogin";
    }

    @PostMapping("/login")
    public String loginSubmit(@ModelAttribute LoginData loginData, Model model, HttpSession session) {

        // Llamamos al servicio para intentar loguear
        UsuarioService.LoginStatus loginStatus =
                usuarioService.login(loginData.geteMail(), loginData.getPassword());

        // CASO 1: Login Correcto
        if (loginStatus == UsuarioService.LoginStatus.LOGIN_OK) {
            UsuarioData usuario = usuarioService.findByEmail(loginData.geteMail());

            // Registramos el usuario en la sesión mediante nuestro Manager
            managerUserSession.logearUsuario(usuario.getId());

            // CAMBIO CLAVE: Redirigimos siempre al Dashboard
            // Ya no necesitamos 'session.setAttribute' manuales ni ifs de admin
            return "redirect:/dashboard";
        }

        // CASO 2: Errores de Login
        else if (loginStatus == UsuarioService.LoginStatus.USER_NOT_FOUND) {
            model.addAttribute("error", "No existe usuario");
            return "formLogin";
        }
        else if (loginStatus == UsuarioService.LoginStatus.ERROR_PASSWORD) {
            model.addAttribute("error", "Contraseña incorrecta");
            return "formLogin";
        }
        else if (loginStatus == UsuarioService.LoginStatus.BLOCKED) {
            model.addAttribute("error", "Tu cuenta está bloqueada. Contacta con un administrador.");
            return "formLogin";
        }

        // Fallback
        model.addAttribute("error", "Error desconocido de autenticación");
        return "formLogin";
    }

    @GetMapping("/registro")
    public String registroForm(Model model) {
        model.addAttribute("registroData", new RegistroData());
        model.addAttribute("mostrarCheckboxAdmin", !usuarioService.existeAdministrador());
        return "formRegistro";
    }

    @PostMapping("/registro")
    public String registroSubmit(@Valid RegistroData registroData,
                                 BindingResult result,
                                 Model model,
                                 javax.servlet.http.HttpSession session) {
        if (result.hasErrors()) return "formRegistro";

        if (usuarioService.findByEmail(registroData.getEmail()) != null) {
            model.addAttribute("registroData", registroData);
            model.addAttribute("error", "El usuario " + registroData.getEmail() + " ya existe");
            return "formRegistro";
        }

        UsuarioData usuario = new UsuarioData();
        usuario.setEmail(registroData.getEmail());
        usuario.setPassword(registroData.getPassword());
        usuario.setFechaNacimiento(registroData.getFechaNacimiento());
        usuario.setNombre(registroData.getNombre());
        usuario.setAdmin(registroData.getAdmin());

        UsuarioData creado = usuarioService.registrar(usuario);

        if (Boolean.TRUE.equals(creado.getAdmin())) {
            managerUserSession.logearUsuario(creado.getId());
            session.setAttribute("idUsuarioLogeado", creado.getId());
            session.setAttribute("usuarioSesion", creado);
            return "redirect:/usuarios";
        }
        return "redirect:/login";
    }

    @GetMapping("/logout")
   public String logout(HttpSession session) {
        managerUserSession.logout();
        session.removeAttribute("usuarioSesion");
        session.removeAttribute("idUsuarioLogeado");
        return "redirect:/login";
   }
}
