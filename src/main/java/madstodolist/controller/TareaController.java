package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.controller.exception.UsuarioNoLogeadoException;
import madstodolist.controller.exception.TareaNotFoundException;
import madstodolist.dto.TareaData;
import madstodolist.dto.UsuarioData;
import madstodolist.dto.ProyectoData;
import madstodolist.dto.EquipoData;
import madstodolist.dto.ChecklistItemData;
import madstodolist.service.TareaService;
import madstodolist.service.UsuarioService;
import madstodolist.service.ProyectoService;
import madstodolist.service.EquipoService;
import madstodolist.service.ChecklistItemService;
import madstodolist.model.EstadoTarea;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
public class TareaController {

    Logger logger = LoggerFactory.getLogger(TareaController.class);

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

    @Autowired
    private ChecklistItemService checklistItemService;

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

        TareaData nuevaTarea = tareaService.nuevaTareaUsuario(idUsuario, tareaData.getTitulo(), tareaData.getFechaLimite());

        // Si se especificó un estado, actualizarlo
        if (tareaData.getEstado() != null && !tareaData.getEstado().isEmpty()) {
            tareaService.cambiarEstadoTarea(nuevaTarea.getId(), EstadoTarea.valueOf(tareaData.getEstado()));
        }

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
                                 Model model, HttpSession session,
                                 @RequestHeader(value = "Referer", required = false) String referer) {

        TareaData tarea = tareaService.findById(idTarea);
        if (tarea == null) {
            throw new TareaNotFoundException();
        }

        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();
        if (idUsuarioLogeado == null) {
            throw new UsuarioNoLogeadoException();
        }

        // Verificar permisos: el usuario debe ser dueño de la tarea O miembro del equipo al que pertenece la tarea
        boolean puedeEditar = false;

        // Caso 1: La tarea tiene usuarioId y el usuario logueado es el dueño
        if (tarea.getUsuarioId() != null && tarea.getUsuarioId().equals(idUsuarioLogeado)) {
            puedeEditar = true;
        }

        // Caso 2: La tarea pertenece a un equipo y el usuario es miembro de ese equipo
        if (!puedeEditar && tarea.getEquipoId() != null) {
            List<UsuarioData> miembros = equipoService.usuariosEquipo(tarea.getEquipoId());
            puedeEditar = miembros.stream().anyMatch(u -> u.getId().equals(idUsuarioLogeado));
        }

        if (!puedeEditar) {
            throw new UsuarioNoLogeadoException();
        }

        // Guardar la URL de origen para poder redirigir después de guardar
        if (referer != null && !referer.isEmpty()) {
            session.setAttribute("editTareaReferer", referer);
        }

        // Obtener nombres para mostrar en el formulario
        if (tarea.getUsuarioId() != null) {
            UsuarioData usuario = usuarioService.findById(tarea.getUsuarioId());
            model.addAttribute("usuarioNombre", usuario.getEmail());
        }

        if (tarea.getEquipoId() != null) {
            EquipoData equipo = equipoService.recuperarEquipo(tarea.getEquipoId());
            model.addAttribute("equipoNombre", equipo.getNombre());
        }

        if (tarea.getProyectoId() != null) {
            ProyectoData proyecto = proyectoService.findById(tarea.getProyectoId());
            model.addAttribute("proyectoNombre", proyecto.getNombre());
        }

        model.addAttribute("tarea", tarea);
        tareaData.setTitulo(tarea.getTitulo());
        tareaData.setFechaLimite(tarea.getFechaLimite());
        tareaData.setEstado(tarea.getEstado());
        return "formEditarTarea";
    }

    @PostMapping("/tareas/{id}/editar")
    public String grabaTareaModificada(@PathVariable(value="id") Long idTarea, @ModelAttribute TareaData tareaData,
                                       Model model, RedirectAttributes flash, HttpSession session) {
        TareaData tarea = tareaService.findById(idTarea);
        if (tarea == null) {
            throw new TareaNotFoundException();
        }

        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();
        if (idUsuarioLogeado == null) {
            throw new UsuarioNoLogeadoException();
        }

        // Verificar permisos: el usuario debe ser dueño de la tarea O miembro del equipo al que pertenece la tarea
        boolean puedeEditar = false;

        // Caso 1: La tarea tiene usuarioId y el usuario logueado es el dueño
        if (tarea.getUsuarioId() != null && tarea.getUsuarioId().equals(idUsuarioLogeado)) {
            puedeEditar = true;
        }

        // Caso 2: La tarea pertenece a un equipo y el usuario es miembro de ese equipo
        if (!puedeEditar && tarea.getEquipoId() != null) {
            List<UsuarioData> miembros = equipoService.usuariosEquipo(tarea.getEquipoId());
            puedeEditar = miembros.stream().anyMatch(u -> u.getId().equals(idUsuarioLogeado));
        }

        if (!puedeEditar) {
            throw new UsuarioNoLogeadoException();
        }

        tareaService.modificaTarea(idTarea, tareaData.getTitulo(), tareaData.getFechaLimite());

        // Si se especificó un estado, actualizarlo
        if (tareaData.getEstado() != null && !tareaData.getEstado().isEmpty()) {
            tareaService.cambiarEstadoTarea(idTarea, EstadoTarea.valueOf(tareaData.getEstado()));
        }

        flash.addFlashAttribute("mensaje", "Tarea modificada correctamente");

        // Intentar redirigir a la página de origen guardada en la sesión
        String refererUrl = (String) session.getAttribute("editTareaReferer");
        if (refererUrl != null && !refererUrl.isEmpty()) {
            session.removeAttribute("editTareaReferer");
            // Extraer solo la ruta relativa de la URL completa
            String relativePath = refererUrl.substring(refererUrl.indexOf("/", 8));
            return "redirect:" + relativePath;
        }

        // Si no hay referer, usar la lógica anterior como fallback
        if (tarea.getProyectoId() != null) {
            return "redirect:/proyectos/" + tarea.getProyectoId();
        } else if (tarea.getUsuarioId() != null) {
            return "redirect:/usuarios/" + tarea.getUsuarioId() + "/tareas";
        } else if (tarea.getEquipoId() != null) {
            return "redirect:/equipos/" + tarea.getEquipoId();
        }

        return "redirect:/usuarios/" + idUsuarioLogeado + "/tareas";
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

        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();
        if (idUsuarioLogeado == null) {
            throw new UsuarioNoLogeadoException();
        }

        // Verificar permisos: el usuario debe ser dueño de la tarea O miembro del equipo al que pertenece la tarea
        boolean puedeBorrar = false;

        // Caso 1: La tarea tiene usuarioId y el usuario logueado es el dueño
        if (tarea.getUsuarioId() != null && tarea.getUsuarioId().equals(idUsuarioLogeado)) {
            puedeBorrar = true;
        }

        // Caso 2: La tarea pertenece a un equipo y el usuario es miembro de ese equipo
        if (!puedeBorrar && tarea.getEquipoId() != null) {
            List<UsuarioData> miembros = equipoService.usuariosEquipo(tarea.getEquipoId());
            puedeBorrar = miembros.stream().anyMatch(u -> u.getId().equals(idUsuarioLogeado));
        }

        if (!puedeBorrar) {
            throw new UsuarioNoLogeadoException();
        }

        tareaService.borraTarea(idTarea);
        return "";
    }

    @PostMapping("/tareas/{id}/terminar")
    public String terminar(@PathVariable Long id) {
        tareaService.terminarTarea(id);
        TareaData tarea = tareaService.findById(id);

        if (tarea.getUsuarioId() != null) {
            return "redirect:/usuarios/" + tarea.getUsuarioId() + "/tareas";
        }

        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();
        return "redirect:/usuarios/" + idUsuarioLogeado + "/tareas";
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

        // Obtener el nombre del equipo
        EquipoData equipo = equipoService.recuperarEquipo(proyecto.getEquipoId());
        model.addAttribute("equipoNombre", equipo.getNombre());
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

        TareaData nuevaTarea = tareaService.nuevaTareaProyecto(idProyecto, tareaData.getTitulo(), tareaData.getFechaLimite());

        // Si se especificó un estado, actualizarlo
        if (tareaData.getEstado() != null && !tareaData.getEstado().isEmpty()) {
            tareaService.cambiarEstadoTarea(nuevaTarea.getId(), EstadoTarea.valueOf(tareaData.getEstado()));
        }

        flash.addFlashAttribute("mensaje", "Tarea añadida al proyecto");
        return "redirect:/proyectos/" + idProyecto;
    }

    // Endpoint AJAX para mover tareas (También lo protegemos)
    @PatchMapping("/tareas/{id}/estado")
    @ResponseBody
    public ResponseEntity<String> cambiarEstadoTarea(@PathVariable Long id, @RequestBody TareaData tareaData) {
        Long idUsuario = managerUserSession.usuarioLogeado();
        if (idUsuario == null) {
            throw new UsuarioNoLogeadoException();
        }

        TareaData tarea = tareaService.findById(id);
        if (tarea == null) {
            throw new TareaNotFoundException();
        }

        // Verificar si la tarea pertenece a un proyecto
        if (tarea.getProyectoId() != null) {
            // La tarea está en un proyecto, verificar permisos
            ProyectoData proyecto = proyectoService.findById(tarea.getProyectoId());
            if (proyecto == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Proyecto no encontrado");
            }

            Long equipoId = proyecto.getEquipoId();
            if (equipoId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sin permiso");
            }

            // Verificar si el usuario pertenece al equipo
            if (!usuarioPerteneceAEquipo(idUsuario, equipoId)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sin permiso");
            }
        } else {
            // La tarea es personal, verificar que pertenece al usuario
            if (tarea.getUsuarioId() != null && !tarea.getUsuarioId().equals(idUsuario)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sin permiso");
            }
        }

        EstadoTarea nuevoEstado = EstadoTarea.valueOf(tareaData.getEstado());
        tareaService.cambiarEstadoTarea(id, nuevoEstado);

        return ResponseEntity.ok("OK");
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

    @GetMapping("/tareas/{id}")
    public String verTarea(@PathVariable(value="id") Long idTarea, Model model, HttpSession session) {
        TareaData tarea = tareaService.findById(idTarea);
        if (tarea == null) {
            throw new TareaNotFoundException();
        }

        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();
        if (idUsuarioLogeado == null) {
            throw new UsuarioNoLogeadoException();
        }

        // Verificar permisos: el usuario debe ser dueño de la tarea O miembro del equipo al que pertenece la tarea
        boolean puedeVer = false;

        // Caso 1: La tarea tiene usuarioId y el usuario logueado es el dueño
        if (tarea.getUsuarioId() != null && tarea.getUsuarioId().equals(idUsuarioLogeado)) {
            puedeVer = true;
        }

        // Caso 2: La tarea pertenece a un equipo y el usuario es miembro de ese equipo
        if (!puedeVer && tarea.getEquipoId() != null) {
            List<UsuarioData> miembros = equipoService.usuariosEquipo(tarea.getEquipoId());
            puedeVer = miembros.stream().anyMatch(u -> u.getId().equals(idUsuarioLogeado));
        }

        if (!puedeVer) {
            throw new UsuarioNoLogeadoException();
        }

        // Obtener información adicional según el tipo de tarea
        if (tarea.getUsuarioId() != null) {
            UsuarioData usuario = usuarioService.findById(tarea.getUsuarioId());
            model.addAttribute("propietario", usuario);
        }

        if (tarea.getEquipoId() != null) {
            EquipoData equipo = equipoService.recuperarEquipo(tarea.getEquipoId());
            model.addAttribute("equipo", equipo);
        }

        if (tarea.getProyectoId() != null) {
            ProyectoData proyecto = proyectoService.findById(tarea.getProyectoId());
            model.addAttribute("proyecto", proyecto);
        }

        // Cargar los items del checklist
        List<ChecklistItemData> checklistItems = checklistItemService.obtenerChecklistDeTarea(idTarea);
        model.addAttribute("checklistItems", checklistItems);

        UsuarioData usuarioLogeado = usuarioService.findById(idUsuarioLogeado);

        model.addAttribute("tarea", tarea);
        model.addAttribute("usuarioLogeado", usuarioLogeado);
        return "verTarea";
    }

    @GetMapping("/usuarios/{id}/calendario")
    public String calendarioTareas(@PathVariable(value="id") Long idUsuario, Model model, HttpSession session) {
        comprobarUsuarioLogeado(idUsuario);

        UsuarioData usuario = usuarioService.findById(idUsuario);
        List<TareaData> tareas = tareaService.allTareasUsuario(idUsuario);

        // Obtener todos los equipos del usuario para mostrar los nombres
        List<EquipoData> equipos = equipoService.equiposUsuario(idUsuario);

        // Obtener todos los proyectos de los equipos del usuario
        List<ProyectoData> proyectos = new java.util.ArrayList<>();
        if (equipos != null && !equipos.isEmpty()) {
            for (EquipoData equipo : equipos) {
                List<ProyectoData> proyectosEquipo = proyectoService.findAllProyectosByEquipo(equipo.getId());
                if (proyectosEquipo != null) {
                    proyectos.addAll(proyectosEquipo);
                }
            }
        }

        model.addAttribute("usuario", usuario);
        model.addAttribute("tareas", tareas);
        model.addAttribute("equipos", equipos);
        model.addAttribute("proyectos", proyectos);
        return "calendario";
    }

    // ========== ENDPOINTS DEL CHECKLIST ==========

    @PostMapping("/tareas/{id}/checklist")
    @ResponseBody
    public ResponseEntity<ChecklistItemData> crearChecklistItem(
            @PathVariable(value="id") Long idTarea,
            @RequestParam String texto,
            HttpSession session) {

        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();
        if (idUsuarioLogeado == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        TareaData tarea = tareaService.findById(idTarea);
        if (tarea == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // Verificar permisos
        boolean puedeEditar = false;
        if (tarea.getUsuarioId() != null && tarea.getUsuarioId().equals(idUsuarioLogeado)) {
            puedeEditar = true;
        }
        if (!puedeEditar && tarea.getEquipoId() != null) {
            List<UsuarioData> miembros = equipoService.usuariosEquipo(tarea.getEquipoId());
            puedeEditar = miembros.stream().anyMatch(u -> u.getId().equals(idUsuarioLogeado));
        }

        if (!puedeEditar) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        ChecklistItemData nuevoItem = checklistItemService.crearItem(idTarea, texto);
        return ResponseEntity.ok(nuevoItem);
    }

    @PatchMapping("/checklist/{id}/completar")
    @ResponseBody
    public ResponseEntity<ChecklistItemData> completarChecklistItem(
            @PathVariable(value="id") Long idItem,
            HttpSession session) {

        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();
        if (idUsuarioLogeado == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ChecklistItemData item = checklistItemService.findById(idItem);
        TareaData tarea = tareaService.findById(item.getTareaId());

        // Verificar permisos
        boolean puedeEditar = false;
        if (tarea.getUsuarioId() != null && tarea.getUsuarioId().equals(idUsuarioLogeado)) {
            puedeEditar = true;
        }
        if (!puedeEditar && tarea.getEquipoId() != null) {
            List<UsuarioData> miembros = equipoService.usuariosEquipo(tarea.getEquipoId());
            puedeEditar = miembros.stream().anyMatch(u -> u.getId().equals(idUsuarioLogeado));
        }

        if (!puedeEditar) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        ChecklistItemData itemActualizado = checklistItemService.completarItem(idItem);
        return ResponseEntity.ok(itemActualizado);
    }

    @PatchMapping("/checklist/{id}/desmarcar")
    @ResponseBody
    public ResponseEntity<ChecklistItemData> desmarcarChecklistItem(
            @PathVariable(value="id") Long idItem,
            HttpSession session) {

        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();
        if (idUsuarioLogeado == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ChecklistItemData item = checklistItemService.findById(idItem);
        TareaData tarea = tareaService.findById(item.getTareaId());

        // Verificar permisos
        boolean puedeEditar = false;
        if (tarea.getUsuarioId() != null && tarea.getUsuarioId().equals(idUsuarioLogeado)) {
            puedeEditar = true;
        }
        if (!puedeEditar && tarea.getEquipoId() != null) {
            List<UsuarioData> miembros = equipoService.usuariosEquipo(tarea.getEquipoId());
            puedeEditar = miembros.stream().anyMatch(u -> u.getId().equals(idUsuarioLogeado));
        }

        if (!puedeEditar) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        ChecklistItemData itemActualizado = checklistItemService.desmarcarItem(idItem);
        return ResponseEntity.ok(itemActualizado);
    }

    @DeleteMapping("/checklist/{id}")
    @ResponseBody
    public ResponseEntity<String> borrarChecklistItem(
            @PathVariable(value="id") Long idItem,
            HttpSession session) {

        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();
        if (idUsuarioLogeado == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ChecklistItemData item = checklistItemService.findById(idItem);
        TareaData tarea = tareaService.findById(item.getTareaId());

        // Verificar permisos
        boolean puedeEditar = false;
        if (tarea.getUsuarioId() != null && tarea.getUsuarioId().equals(idUsuarioLogeado)) {
            puedeEditar = true;
        }
        if (!puedeEditar && tarea.getEquipoId() != null) {
            List<UsuarioData> miembros = equipoService.usuariosEquipo(tarea.getEquipoId());
            puedeEditar = miembros.stream().anyMatch(u -> u.getId().equals(idUsuarioLogeado));
        }

        if (!puedeEditar) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        checklistItemService.borrarItem(idItem);
        return ResponseEntity.ok("");
    }
}
