// src/main/java/madstodolist/controller/EquipoController.java
package madstodolist.controller;

import madstodolist.dto.EquipoData;
import madstodolist.dto.UsuarioData;
import madstodolist.service.EquipoService;
import madstodolist.service.EquipoServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
public class EquipoController {

    @Autowired
    private EquipoService equipoService;

    @GetMapping("/equipos")
    public String listarEquipos(Model model, HttpSession session) {
        // sin cambios
        UsuarioData usuarioSesion = (UsuarioData) session.getAttribute("usuarioSesion");
        model.addAttribute("logeado", usuarioSesion != null);
        model.addAttribute("usuarioSesion", usuarioSesion);

        List<EquipoData> equipos = equipoService.findAllOrdenadoPorNombre();
        model.addAttribute("equipos", equipos);
        return "equipos";
    }

    @PostMapping("/equipos/nuevo")
    public String crearEquipo(@RequestParam String nombre, HttpSession session) {
        UsuarioData usuarioSesion = (UsuarioData) session.getAttribute("usuarioSesion");
        if (usuarioSesion == null)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Debe iniciar sesión");

        equipoService.crearEquipo(nombre, usuarioSesion.getId());
        return "redirect:/equipos";
    }

    @GetMapping("/equipos/{id}")
    public String detalleEquipo(@PathVariable Long id, Model model, HttpSession session) {
        UsuarioData usuarioSesion = (UsuarioData) session.getAttribute("usuarioSesion");
        model.addAttribute("logeado", usuarioSesion != null);
        model.addAttribute("usuarioSesion", usuarioSesion);

        EquipoData equipo = equipoService.recuperarEquipo(id);
        List<UsuarioData> usuarios = equipoService.usuariosEquipo(id);

        boolean soyMiembro = false;
        boolean esAdmin = false;
        if (usuarioSesion != null) {
            Long miId = usuarioSesion.getId();
            soyMiembro = usuarios.stream().anyMatch(u -> u.getId().equals(miId));
            esAdmin = equipoService.esAdminDeEquipo(id, miId);
        }

        model.addAttribute("equipo", equipo);
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("soyMiembro", soyMiembro);
        model.addAttribute("esAdmin", esAdmin);

        // Si es admin, pasar lista de usuarios que no son miembros (para añadir)
        if (esAdmin) {
            List<UsuarioData> disponibles = equipoService.usuariosNoMiembros(id);
            model.addAttribute("usuariosDisponibles", disponibles);
        }

        return "equipo";
    }

    // Añadir miembro: ahora solo admin puede añadir otros usuarios (form envía usuarioId)
    @PostMapping("/equipos/{id}/miembro/add")
    public String añadirUsuarioAEquipoPorAdmin(@PathVariable Long id,
                                               @RequestParam Long usuarioId,
                                               HttpSession session) {
        UsuarioData usuarioSesion = (UsuarioData) session.getAttribute("usuarioSesion");
        if (usuarioSesion == null || !equipoService.esAdminDeEquipo(id, usuarioSesion.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el administrador del equipo puede añadir miembros");
        }
        equipoService.añadirUsuarioAEquipoComoAdmin(id, usuarioId, usuarioSesion.getId());
        return "redirect:/equipos/" + id;
    }

    // Eliminar miembro por admin (no confundir con la ruta para "salirme" que ya existe)
    @PostMapping("/equipos/{id}/miembro/eliminar-usuario")
    public String eliminarUsuarioDeEquipoPorAdmin(@PathVariable Long id,
                                                  @RequestParam Long usuarioId,
                                                  HttpSession session) {
        UsuarioData usuarioSesion = (UsuarioData) session.getAttribute("usuarioSesion");
        if (usuarioSesion == null || !equipoService.esAdminDeEquipo(id, usuarioSesion.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el administrador del equipo puede eliminar miembros");
        }
        equipoService.quitarUsuarioDeEquipoComoAdmin(id, usuarioId, usuarioSesion.getId());
        return "redirect:/equipos/" + id;
    }

    // Endpoints previos para unirse/quitarse a sí mismo y edición/eliminación del equipo siguen igual...

    // ... (resto del controlador sin cambios)
    @PostMapping("/equipos/{id}/miembro")
    public String unirmeAEquipo(@PathVariable Long id, HttpSession session) {
        UsuarioData usuarioSesion = (UsuarioData) session.getAttribute("usuarioSesion");
        if (usuarioSesion == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Debe iniciar sesión");
        }
        // El usuario se añade a sí mismo al equipo (no hace falta ser admin)
        equipoService.añadirUsuarioAEquipo(id, usuarioSesion.getId());
        return "redirect:/equipos/" + id;
    }

    @PostMapping("/equipos/{id}/miembro/eliminar")
    public String salirmeDeEquipo(@PathVariable Long id, HttpSession session) {
        UsuarioData usuarioSesion = (UsuarioData) session.getAttribute("usuarioSesion");
        if (usuarioSesion == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Debe iniciar sesión");
        }
        // El usuario se elimina a sí mismo del equipo
        equipoService.quitarUsuarioDeEquipo(id, usuarioSesion.getId());
        return "redirect:/equipos/" + id;
    }

    @ExceptionHandler(EquipoServiceException.class)
    public ResponseEntity<String> manejarEquipoServiceException(EquipoServiceException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @GetMapping("/equipos/{id}/editar")
    public String editarEquipoForm(@PathVariable Long id, Model model, HttpSession session) {
        UsuarioData usuarioSesion = (UsuarioData) session.getAttribute("usuarioSesion");
        if (usuarioSesion == null || !equipoService.esAdminDeEquipo(id, usuarioSesion.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el administrador del equipo puede editarlo");
        }
        EquipoData equipo = equipoService.recuperarEquipo(id);
        model.addAttribute("logeado", true);
        model.addAttribute("usuarioSesion", usuarioSesion);
        model.addAttribute("equipo", equipo);
        return "equipo-editar";
    }

    @PostMapping("/equipos/{id}/editar")
    public String actualizarEquipo(@PathVariable Long id,
                                   @RequestParam String nombre,
                                   HttpSession session) {
        UsuarioData usuarioSesion = (UsuarioData) session.getAttribute("usuarioSesion");
        if (usuarioSesion == null || !equipoService.esAdminDeEquipo(id, usuarioSesion.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el administrador del equipo puede renombrarlo");
        }
        equipoService.actualizarNombreEquipo(id, nombre, usuarioSesion.getId());
        return "redirect:/equipos/" + id;
    }

    @PostMapping("/equipos/{id}/eliminar")
    public String eliminarEquipo(@PathVariable Long id, HttpSession session) {
        UsuarioData usuarioSesion = (UsuarioData) session.getAttribute("usuarioSesion");
        if (usuarioSesion == null || !equipoService.esAdminDeEquipo(id, usuarioSesion.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el administrador del equipo puede eliminarlo");
        }
        equipoService.eliminarEquipo(id);
        return "redirect:/equipos";
    }
}
