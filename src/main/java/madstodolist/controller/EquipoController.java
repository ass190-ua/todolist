package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.controller.exception.UsuarioNoLogeadoException;
import madstodolist.dto.EquipoData;
import madstodolist.dto.ProyectoData;
import madstodolist.dto.UsuarioData;
import madstodolist.service.EquipoService;
import madstodolist.service.EquipoServiceException;
import madstodolist.service.ProyectoService;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Controller
public class EquipoController {

    @Autowired
    private EquipoService equipoService;

    @Autowired
    private ProyectoService proyectoService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ManagerUserSession managerUserSession;

    private void comprobarUsuarioLogeado() {
        Long idUsuario = managerUserSession.usuarioLogeado();
        if (idUsuario == null)
            throw new UsuarioNoLogeadoException();
    }

    @GetMapping("/equipos")
    public String listarEquipos(Model model) {
        Long idUsuario = managerUserSession.usuarioLogeado();
        if (idUsuario != null) {
            UsuarioData usuario = usuarioService.findById(idUsuario);
            model.addAttribute("usuarioSesion", usuario);
            model.addAttribute("logeado", true);
        } else {
            model.addAttribute("logeado", false);
        }

        List<EquipoData> equipos = equipoService.findAllOrdenadoPorNombre();
        model.addAttribute("equipos", equipos);
        return "equipos";
    }

    @PostMapping("/equipos/nuevo")
    public String crearEquipo(@RequestParam String nombre) {
        comprobarUsuarioLogeado();
        Long idUsuario = managerUserSession.usuarioLogeado();
        equipoService.crearEquipo(nombre, idUsuario);
        return "redirect:/equipos";
    }

    @GetMapping("/equipos/{id}")
    public String detalleEquipo(@PathVariable Long id, Model model) {
        Long idUsuario = managerUserSession.usuarioLogeado();
        UsuarioData usuario = null;

        if (idUsuario != null) {
            usuario = usuarioService.findById(idUsuario);
            model.addAttribute("usuarioSesion", usuario);
            model.addAttribute("logeado", true);
        } else {
            model.addAttribute("logeado", false);
        }

        EquipoData equipo = equipoService.recuperarEquipo(id);
        List<UsuarioData> usuarios = equipoService.usuariosEquipo(id);
        List<ProyectoData> proyectos = proyectoService.findAllProyectosByEquipo(id);

        model.addAttribute("equipo", equipo);
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("proyectos", proyectos);

        boolean soyMiembro = false;
        boolean esAdmin = false;

        if (idUsuario != null) {
            soyMiembro = usuarios.stream().anyMatch(u -> u.getId().equals(idUsuario));
            esAdmin = equipoService.esAdminDeEquipo(id, idUsuario);
        }

        model.addAttribute("soyMiembro", soyMiembro);
        model.addAttribute("esAdmin", esAdmin);

        if (esAdmin) {
            List<UsuarioData> disponibles = equipoService.usuariosNoMiembros(id);
            model.addAttribute("usuariosDisponibles", disponibles);
        }

        return "equipo";
    }

    @PostMapping("/equipos/{id}/miembro/add")
    public String añadirUsuarioAEquipoPorAdmin(@PathVariable Long id, @RequestParam Long usuarioId) {
        comprobarUsuarioLogeado();
        Long idUsuario = managerUserSession.usuarioLogeado();
        if (!equipoService.esAdminDeEquipo(id, idUsuario)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el administrador puede añadir miembros");
        }
        equipoService.añadirUsuarioAEquipoComoAdmin(id, usuarioId, idUsuario);
        return "redirect:/equipos/" + id;
    }

    @PostMapping("/equipos/{id}/miembro/eliminar-usuario")
    public String eliminarUsuarioDeEquipoPorAdmin(@PathVariable Long id, @RequestParam Long usuarioId) {
        comprobarUsuarioLogeado();
        Long idUsuario = managerUserSession.usuarioLogeado();
        if (!equipoService.esAdminDeEquipo(id, idUsuario)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el administrador puede eliminar miembros");
        }
        equipoService.quitarUsuarioDeEquipoComoAdmin(id, usuarioId, idUsuario);
        return "redirect:/equipos/" + id;
    }

    @PostMapping("/equipos/{id}/miembro")
    public String unirmeAEquipo(@PathVariable Long id) {
        comprobarUsuarioLogeado();
        Long idUsuario = managerUserSession.usuarioLogeado();
        equipoService.añadirUsuarioAEquipo(id, idUsuario);
        return "redirect:/equipos/" + id;
    }

    @PostMapping("/equipos/{id}/miembro/eliminar")
    public String salirmeDeEquipo(@PathVariable Long id) {
        comprobarUsuarioLogeado();
        Long idUsuario = managerUserSession.usuarioLogeado();
        equipoService.quitarUsuarioDeEquipo(id, idUsuario);
        return "redirect:/equipos/" + id;
    }

    @ExceptionHandler(EquipoServiceException.class)
    public ResponseEntity<String> manejarEquipoServiceException(EquipoServiceException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @GetMapping("/equipos/{id}/editar")
    public String editarEquipoForm(@PathVariable Long id, Model model) {
        comprobarUsuarioLogeado();
        Long idUsuario = managerUserSession.usuarioLogeado();

        if (!equipoService.esAdminDeEquipo(id, idUsuario)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el administrador puede editar el equipo");
        }

        UsuarioData usuario = usuarioService.findById(idUsuario);
        model.addAttribute("usuarioSesion", usuario);
        model.addAttribute("logeado", true);

        EquipoData equipo = equipoService.recuperarEquipo(id);
        model.addAttribute("equipo", equipo);
        return "equipo-editar";
    }

    @PostMapping("/equipos/{id}/editar")
    public String actualizarEquipo(@PathVariable Long id, @RequestParam String nombre) {
        comprobarUsuarioLogeado();
        Long idUsuario = managerUserSession.usuarioLogeado();

        if (!equipoService.esAdminDeEquipo(id, idUsuario)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el administrador puede renombrar el equipo");
        }
        equipoService.actualizarNombreEquipo(id, nombre, idUsuario);
        return "redirect:/equipos/" + id;
    }

    @PostMapping("/equipos/{id}/eliminar")
    public String eliminarEquipo(@PathVariable Long id) {
        comprobarUsuarioLogeado();
        Long idUsuario = managerUserSession.usuarioLogeado();

        if (!equipoService.esAdminDeEquipo(id, idUsuario)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo el administrador puede eliminar el equipo");
        }
        equipoService.eliminarEquipo(id);
        return "redirect:/equipos";
    }
}