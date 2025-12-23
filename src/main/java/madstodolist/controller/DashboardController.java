package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.controller.exception.UsuarioNoLogeadoException;
import madstodolist.dto.EquipoData;
import madstodolist.dto.TareaData;
import madstodolist.dto.UsuarioData;
import madstodolist.service.EquipoService;
import madstodolist.service.TareaService;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private TareaService tareaService;
    @Autowired
    private EquipoService equipoService;
    @Autowired
    private ManagerUserSession managerUserSession;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Long idUsuario = managerUserSession.usuarioLogeado();
        if (idUsuario == null) {
            throw new UsuarioNoLogeadoException();
        }

        UsuarioData usuario = usuarioService.findById(idUsuario);

        // --- LÓGICA MEJORADA DE TAREAS ---
        List<TareaData> tareas = tareaService.allTareasUsuario(idUsuario);

        long totalTareas = tareas.size();
        long tareasPendientes = tareas.stream()
                .filter(t -> !"TERMINADA".equals(t.getEstado()))
                .count();
        long tareasCompletadas = totalTareas - tareasPendientes;

        // Calculamos porcentaje (evitando división por cero)
        int porcentajeProgreso = totalTareas > 0 ? (int)((tareasCompletadas * 100) / totalTareas) : 0;

        // Obtenemos las 5 tareas más recientes (ordenadas por ID descendente)
        // Nota: Usamos el ID como proxy de "tiempo" ya que es autoincremental
        List<TareaData> ultimasTareas = tareas.stream()
                .sorted(Comparator.comparing(TareaData::getId).reversed())
                .limit(5)
                .collect(Collectors.toList());

        // --- LÓGICA DE EQUIPOS ---
        List<EquipoData> equipos = equipoService.findAllOrdenadoPorNombre();
        long misEquipos = equipos.stream()
                .filter(e -> equipoService.usuariosEquipo(e.getId()).stream()
                        .anyMatch(u -> u.getId().equals(idUsuario)))
                .count();

        // Pasamos datos al modelo
        model.addAttribute("usuario", usuario);
        model.addAttribute("usuarioSesion", usuario); // Para navbar
        model.addAttribute("logeado", true);          // Para navbar

        // Datos Stats
        model.addAttribute("totalTareas", totalTareas);
        model.addAttribute("tareasPendientes", tareasPendientes);
        model.addAttribute("porcentajeProgreso", porcentajeProgreso);
        model.addAttribute("ultimasTareas", ultimasTareas);
        model.addAttribute("totalEquipos", misEquipos);

        return "dashboard";
    }
}