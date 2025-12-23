package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    private ManagerUserSession managerUserSession;

    @Autowired
    private UsuarioService usuarioService; // Necesario para recuperar datos del usuario

    @GetMapping("/")
    public String home(Model model) {
        // Si ya está logueado, al dashboard
        if (managerUserSession.usuarioLogeado() != null) {
            return "redirect:/dashboard";
        }
        // Si no, a la landing page
        return "homepage";
    }

    @GetMapping("/about")
    public String about(Model model) {
        // Logica para que la Navbar se pinte bien (Login vs Logout)
        Long idUsuario = managerUserSession.usuarioLogeado();
        if (idUsuario != null) {
            UsuarioData usuario = usuarioService.findById(idUsuario);
            model.addAttribute("usuarioSesion", usuario);
            model.addAttribute("logeado", true);
        } else {
            model.addAttribute("logeado", false);
        }
        return "about";
    }
}