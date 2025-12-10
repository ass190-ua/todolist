package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.controller.exception.UsuarioNoAutorizadoException;
import madstodolist.controller.exception.UsuarioNoLogeadoException;
import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletResponse;

@ControllerAdvice
public class GlobalExceptionHandler {

    @Autowired
    private ManagerUserSession managerUserSession;

    @Autowired
    private UsuarioService usuarioService;

    // Este ModelAttribute funciona para las peticiones normales
    @ModelAttribute
    public void addAttributes(Model model) {
        cargarUsuarioEnModelo(model);
    }

    // Método auxiliar para reutilizar la lógica de cargar el usuario
    private void cargarUsuarioEnModelo(Model model) {
        Long idUsuario = managerUserSession.usuarioLogeado();
        if (idUsuario != null) {
            // Intentamos recuperar el usuario. Si no existe (caso raro), no fallamos.
            UsuarioData usuario = usuarioService.findById(idUsuario);
            if (usuario != null) {
                model.addAttribute("usuarioSesion", usuario);
                model.addAttribute("logeado", true);
            } else {
                model.addAttribute("logeado", false);
            }
        } else {
            model.addAttribute("logeado", false);
        }
    }

    @ExceptionHandler(UsuarioNoLogeadoException.class)
    public String handleUsuarioNoLogeado() {
        return "redirect:/login";
    }

    @ExceptionHandler(UsuarioNoAutorizadoException.class)
    public String handleUsuarioNoAutorizado(UsuarioNoAutorizadoException ex, Model model, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        cargarUsuarioEnModelo(model); // <--- IMPORTANTE: Recargar usuario para la vista de error
        configurarModeloError(model, 403, "Acceso Denegado", "No tienes permisos suficientes para realizar esta acción.");
        return "error";
    }

    @ExceptionHandler(ResponseStatusException.class)
    public String handleResponseStatusException(ResponseStatusException ex, Model model, HttpServletResponse response) {
        response.setStatus(ex.getStatus().value());
        cargarUsuarioEnModelo(model); // <--- IMPORTANTE
        configurarModeloError(model, ex.getStatus().value(), ex.getStatus().getReasonPhrase(), ex.getReason());
        return "error";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception ex, Model model, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        cargarUsuarioEnModelo(model); // <--- IMPORTANTE
        configurarModeloError(model, 500, "Error Interno", "Ha ocurrido un error inesperado en el servidor.");
        return "error";
    }

    private void configurarModeloError(Model model, int status, String error, String message) {
        model.addAttribute("status", status);
        model.addAttribute("error", error);
        model.addAttribute("message", message);
    }
}