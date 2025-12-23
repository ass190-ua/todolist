package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.ProyectoData;
import madstodolist.dto.TareaData;
import madstodolist.dto.UsuarioData;
import madstodolist.model.EstadoTarea;
import madstodolist.service.EquipoService;
import madstodolist.service.ProyectoService;
import madstodolist.service.TareaService;
import madstodolist.service.UsuarioService;
import madstodolist.service.EquipoServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class ProyectoController {

    @Autowired
    private ProyectoService proyectoService;
    @Autowired
    private EquipoService equipoService;
    @Autowired
    private TareaService tareaService;
    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private ManagerUserSession managerUserSession;

    @GetMapping("/equipos/{equipoId}/proyectos/nuevo")
    public String formNuevoProyecto(@PathVariable Long equipoId, Model model, HttpSession session) {
        Long idUsuario = managerUserSession.usuarioLogeado();
        if (idUsuario == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Debes iniciar sesión");
        }

        // Verificación de permisos
        List<UsuarioData> miembros = equipoService.usuariosEquipo(equipoId);
        boolean esMiembro = miembros.stream().anyMatch(u -> u.getId().equals(idUsuario));
        boolean esAdminEquipo = equipoService.esAdminDeEquipo(equipoId, idUsuario);

        if (!esMiembro && !esAdminEquipo) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No eres miembro de este equipo");
        }

        ProyectoData proyectoData = new ProyectoData();
        proyectoData.setEquipoId(equipoId);

        model.addAttribute("proyectoData", proyectoData);
        model.addAttribute("equipoId", equipoId);
        return "formNuevoProyecto";
    }

    @PostMapping("/proyectos/nuevo")
    public String crearProyecto( @ModelAttribute ProyectoData proyectoData,  RedirectAttributes flash,  HttpSession session) {
        Long idUsuario = managerUserSession.usuarioLogeado();
        if (idUsuario == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Debes iniciar sesión");
        }

        // Validación extra de seguridad
        Long equipoId = proyectoData.getEquipoId();
        List<UsuarioData> miembros = equipoService.usuariosEquipo(equipoId);
        boolean esMiembro = miembros.stream().anyMatch(u -> u.getId().equals(idUsuario));
        boolean esAdminEquipo = equipoService.esAdminDeEquipo(equipoId, idUsuario);

        if (!esMiembro && !esAdminEquipo) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para crear proyectos en este equipo");
        }

        proyectoService.crearProyecto(
                proyectoData.getNombre(),
                proyectoData.getDescripcion(),
                proyectoData.getEquipoId()
        );
        flash.addFlashAttribute("mensaje", "Proyecto creado correctamente");
        return "redirect:/equipos/" + proyectoData.getEquipoId();
    }

    @GetMapping("/proyectos/{id}")
    public String tableroProyecto(@PathVariable Long id, Model model, HttpSession session) {
        Long idUsuario = managerUserSession.usuarioLogeado();
        if (idUsuario == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Debes iniciar sesión");
        }

        ProyectoData proyecto = proyectoService.findById(id);
        if (proyecto == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Proyecto no encontrado");
        }

        // --- Seguridad ---
        Long equipoId = proyecto.getEquipoId();
        List<UsuarioData> miembros = equipoService.usuariosEquipo(equipoId);
        boolean esMiembro = miembros.stream().anyMatch(u -> u.getId().equals(idUsuario));
        boolean esAdminEquipo = equipoService.esAdminDeEquipo(equipoId, idUsuario);

        if (!esMiembro && !esAdminEquipo) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para ver este proyecto");
        }

        List<TareaData> tareas = tareaService.allTareasProyecto(id);

        Map<String, List<TareaData>> tareasPorEstado = tareas.stream()
                .collect(Collectors.groupingBy(TareaData::getEstado));

        // Crear un mapa de usuarios para mostrar los nombres
        Map<Long, String> usuariosMap = new java.util.HashMap<>();
        for (TareaData tarea : tareas) {
            if (tarea.getUsuarioId() != null && !usuariosMap.containsKey(tarea.getUsuarioId())) {
                UsuarioData usuario = usuarioService.findById(tarea.getUsuarioId());
                if (usuario != null) {
                    usuariosMap.put(tarea.getUsuarioId(), usuario.getEmail());
                }
            }
        }

        model.addAttribute("proyecto", proyecto);
        model.addAttribute("tareasPendientes", tareasPorEstado.get(EstadoTarea.PENDIENTE.toString()));
        model.addAttribute("tareasEnCurso", tareasPorEstado.get(EstadoTarea.EN_CURSO.toString()));
        model.addAttribute("tareasTerminadas", tareasPorEstado.get(EstadoTarea.TERMINADA.toString()));
        model.addAttribute("usuariosMap", usuariosMap);

        return "proyecto";
    }
}
