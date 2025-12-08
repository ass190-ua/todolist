package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    ManagerUserSession managerUserSession;

    @Autowired
    UsuarioService usuarioService;

    @ModelAttribute("logeado")
    public boolean addLogeado() {
        return managerUserSession.usuarioLogeado() != null;
    }

    @ModelAttribute("usuarioSesion")
    public UsuarioData addUsuarioSesion() {
        Long id = managerUserSession.usuarioLogeado();
        return (id != null) ? usuarioService.findById(id) : null;
    }
}
