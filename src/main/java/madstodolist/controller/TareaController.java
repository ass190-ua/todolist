package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.controller.exception.UsuarioNoLogeadoException;
import madstodolist.controller.exception.TareaNotFoundException;
import madstodolist.dto.TareaData;
import madstodolist.dto.UsuarioData;
import madstodolist.dto.ProyectoData;
import madstodolist.service.TareaService;
import madstodolist.service.UsuarioService;
import madstodolist.service.ProyectoService;
import madstodolist.service.EquipoService;
import madstodolist.model.EstadoTarea;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
public class TareaController {

    @Autowired
    UsuarioService usuarioService;

    @Autowired
    TareaService tareaService;

    @Autowired
    ManagerUserSession managerUserSession;

    @Autowired
    private ProyectoService proyectoService;

    @Autowired
    private EquipoService equipoService;

    private void comprobarUsuarioLogeado(Long idUsuario) {
        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();
        if (!idUsuario.equals(idUsuarioLogeado))
            throw new UsuarioNoLogeadoException();
    }

    @GetMapping("/usuarios/{id}/tareas/nueva")
    public String formNuevaTarea(@PathVariable(value="id") Long idUsuario,
                                 @ModelAttribute TareaData tareaData, Model model,
                                 HttpSession session) {

        comprobarUsuarioLogeado(idUsuario);

        UsuarioData usuario = usuarioService.findById(idUsuario);
        model.addAttribute("usuario", usuario);
        return "formNuevaTarea";
    }

    @PostMapping("/usuarios/{id}/tareas/nueva")
    public String nuevaTarea(@PathVariable(value="id") Long idUsuario, @ModelAttribute TareaData tareaData,
                             Model model, RedirectAttributes flash,
                             HttpSession session) {

        comprobarUsuarioLogeado(idUsuario);

        tareaService.nuevaTareaUsuario(idUsuario, tareaData.getTitulo());
        flash.addFlashAttribute("mensaje", "Tarea creada correctamente");
        return "redirect:/usuarios/" + idUsuario + "/tareas";
     }

    @GetMapping("/usuarios/{id}/tareas")
    public String listadoTareas(@PathVariable(value="id") Long idUsuario, Model model, HttpSession session) {

        comprobarUsuarioLogeado(idUsuario);

        UsuarioData usuario = usuarioService.findById(idUsuario);
        //List<TareaData> tareas = tareaService.allTareasUsuario(idUsuario);
        //lo cambiamos por este para q salgan ordenadas por estado "termianada"
        List<TareaData> tareas = tareaService.allTareasUsuarioOrdenadas(idUsuario);
        model.addAttribute("usuario", usuario);
        model.addAttribute("tareas", tareas);
        return "listaTareas";
    }

    @GetMapping("/tareas/{id}/editar")
    public String formEditaTarea(@PathVariable(value="id") Long idTarea, @ModelAttribute TareaData tareaData,
                                 Model model, HttpSession session) {

        TareaData tarea = tareaService.findById(idTarea);
        if (tarea == null) {
            throw new TareaNotFoundException();
        }

        comprobarUsuarioLogeado(tarea.getUsuarioId());

        model.addAttribute("tarea", tarea);
        tareaData.setTitulo(tarea.getTitulo());
        return "formEditarTarea";
    }

    @PostMapping("/tareas/{id}/editar")
    public String grabaTareaModificada(@PathVariable(value="id") Long idTarea, @ModelAttribute TareaData tareaData,
                                       Model model, RedirectAttributes flash, HttpSession session) {
        TareaData tarea = tareaService.findById(idTarea);
        if (tarea == null) {
            throw new TareaNotFoundException();
        }

        Long idUsuario = tarea.getUsuarioId();

        comprobarUsuarioLogeado(idUsuario);

        tareaService.modificaTarea(idTarea, tareaData.getTitulo());
        flash.addFlashAttribute("mensaje", "Tarea modificada correctamente");
        return "redirect:/usuarios/" + tarea.getUsuarioId() + "/tareas";
    }

    @DeleteMapping("/tareas/{id}")
    @ResponseBody
    // La anotación @ResponseBody sirve para que la cadena devuelta sea la resupuesta
    // de la petición HTTP, en lugar de una plantilla thymeleaf
    public String borrarTarea(@PathVariable(value="id") Long idTarea, RedirectAttributes flash, HttpSession session) {
        TareaData tarea = tareaService.findById(idTarea);
        if (tarea == null) {
            throw new TareaNotFoundException();
        }

        comprobarUsuarioLogeado(tarea.getUsuarioId());

        tareaService.borraTarea(idTarea);
        return "";
    }

    @PostMapping("/tareas/{id}/terminar")
    public String terminar(@PathVariable Long id) {
        tareaService.terminarTarea(id);
        Long usuarioId = tareaService.findById(id).getUsuarioId();
        return "redirect:/usuarios/" + usuarioId + "/tareas";
    }

    @GetMapping("/proyectos/{id}/tareas/nueva")
    public String formNuevaTareaProyecto(@PathVariable(value="id") Long idProyecto,
                                         @ModelAttribute TareaData tareaData, Model model,
                                         HttpSession session) {

        Long idUsuario = managerUserSession.usuarioLogeado();
        if (idUsuario == null) throw new UsuarioNoLogeadoException();

        ProyectoData proyecto = proyectoService.findById(idProyecto);
        if (proyecto == null) {
            throw new TareaNotFoundException();
        }

        // --- Seguridad ---
        if (!usuarioPerteneceAEquipo(idUsuario, proyecto.getEquipoId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para crear tareas en este proyecto");
        }

        model.addAttribute("proyecto", proyecto);
        return "formNuevaTareaProyecto";
    }

    @PostMapping("/proyectos/{id}/tareas/nueva")
    public String nuevaTareaProyecto(@PathVariable(value="id") Long idProyecto,
                                     @ModelAttribute TareaData tareaData,
                                     RedirectAttributes flash,
                                     HttpSession session) {

        Long idUsuario = managerUserSession.usuarioLogeado();
        if (idUsuario == null) throw new UsuarioNoLogeadoException();

        // Recuperamos proyecto para ver el equipo
        ProyectoData proyecto = proyectoService.findById(idProyecto);
        if (proyecto == null) throw new TareaNotFoundException();

        // --- Seguridad ---
        if (!usuarioPerteneceAEquipo(idUsuario, proyecto.getEquipoId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para crear tareas en este proyecto");
        }

        tareaService.nuevaTareaProyecto(idProyecto, idUsuario, tareaData.getTitulo());

        flash.addFlashAttribute("mensaje", "Tarea añadida al proyecto");
        return "redirect:/proyectos/" + idProyecto;
    }

    // Endpoint AJAX para mover tareas (También lo protegemos)
    @PatchMapping("/tareas/{id}/estado")
    @ResponseBody
    public String cambiarEstadoTarea(@PathVariable Long id, @RequestBody TareaData tareaData) {
        Long idUsuario = managerUserSession.usuarioLogeado();
        if (idUsuario == null) {
            throw new UsuarioNoLogeadoException();
        }

        TareaData tarea = tareaService.findById(id);
        if (tarea == null) {
            throw new TareaNotFoundException();
        }

        EstadoTarea nuevoEstado = EstadoTarea.valueOf(tareaData.getEstado());
        tareaService.cambiarEstadoTarea(id, nuevoEstado);

        return "OK";
    }

    // Metodo helper privado para reutilizar la lógica de seguridad
    private boolean usuarioPerteneceAEquipo(Long idUsuario, Long idEquipo) {
        List<UsuarioData> miembros = equipoService.usuariosEquipo(idEquipo);
        boolean esMiembro = miembros.stream().anyMatch(u -> u.getId().equals(idUsuario));
        boolean esAdmin = equipoService.esAdminDeEquipo(idEquipo, idUsuario);
        return esMiembro || esAdmin;
    }

    @PostMapping("/tareas/{id}/pendiente")
    public String restaurarTarea(@PathVariable Long id, HttpSession session) {
        Long idUsuario = managerUserSession.usuarioLogeado();
        if (idUsuario == null) {
            throw new UsuarioNoLogeadoException();
        }

        // Reutilizamos el método que creamos para el Kanban
        tareaService.cambiarEstadoTarea(id, EstadoTarea.PENDIENTE);

        return "redirect:/usuarios/" + idUsuario + "/tareas";
    }
}
