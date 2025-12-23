package madstodolist.service;

import madstodolist.model.Proyecto;
import madstodolist.model.Equipo;
import madstodolist.dto.TareaData;
import madstodolist.model.EstadoTarea;
import madstodolist.model.Tarea;
import madstodolist.model.Usuario;
import madstodolist.repository.EquipoRepository;
import madstodolist.repository.ProyectoRepository;
import madstodolist.repository.TareaRepository;
import madstodolist.repository.UsuarioRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TareaService {

    Logger logger = LoggerFactory.getLogger(TareaService.class);

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private EquipoRepository equipoRepository;
    @Autowired
    private TareaRepository tareaRepository;
    @Autowired
    private ProyectoRepository proyectoRepository;
    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    public TareaData nuevaTareaUsuario(Long idUsuario, String tituloTarea, LocalDate fecha) {
        logger.debug("Añadiendo tarea " + tituloTarea + " al usuario " + idUsuario);
        Usuario usuario = usuarioRepository.findById(idUsuario).orElse(null);
        if (usuario == null) {
            throw new TareaServiceException("Usuario " + idUsuario + " no existe al crear tarea " + tituloTarea);
        }
        Tarea tarea = new Tarea(usuario, tituloTarea, fecha);
        tareaRepository.save(tarea);
        return modelMapper.map(tarea, TareaData.class);
    }

    @Transactional
    public TareaData nuevaTareaProyecto(Long idProyecto, String tituloTarea, LocalDate fecha) {
        logger.debug("Añadiendo tarea " + tituloTarea + " al proyecto " + idProyecto);

        Proyecto proyecto = proyectoRepository.findById(idProyecto)
                .orElseThrow(() -> new TareaServiceException("Proyecto no encontrado"));

        Tarea tarea = new Tarea(proyecto, tituloTarea, fecha);
        tareaRepository.save(tarea);

        return modelMapper.map(tarea, TareaData.class);
    }

    @Transactional
    public TareaData nuevaTareaEquipo(Long equipoId, String tituloTarea, LocalDate fecha) {
        logger.debug("Añadiendo tarea " + tituloTarea + " al equipo " + equipoId);

        Equipo equipo = equipoRepository.findById(equipoId)
                .orElseThrow(() -> new TareaServiceException("Equipo no encontrado"));

        Tarea tarea = new Tarea(equipo, tituloTarea, fecha);
        tareaRepository.save(tarea);

        return modelMapper.map(tarea, TareaData.class);
    }

    @Transactional
    public TareaData nuevaTareaEquipo(Long equipoId, String tituloTarea, LocalDate fecha, Long usuarioId) {
        logger.debug("Añadiendo tarea " + tituloTarea + " al equipo " + equipoId + " con usuario asignado " + usuarioId);

        Equipo equipo = equipoRepository.findById(equipoId)
                .orElseThrow(() -> new TareaServiceException("Equipo no encontrado"));

        Tarea tarea = new Tarea(equipo, tituloTarea, fecha);

        // Si se especifica un usuario, asignarlo a la tarea
        if (usuarioId != null) {
            Usuario usuario = usuarioRepository.findById(usuarioId)
                    .orElseThrow(() -> new TareaServiceException("Usuario no encontrado"));
            tarea.setUsuario(usuario);
        }

        tareaRepository.save(tarea);

        return modelMapper.map(tarea, TareaData.class);
    }

    @Transactional(readOnly = true)
    public List<TareaData> allTareasUsuario(Long idUsuario) {
        logger.debug("Devolviendo todas las tareas del usuario " + idUsuario);
        Usuario usuario = usuarioRepository.findById(idUsuario).orElse(null);
        if (usuario == null) {
            throw new TareaServiceException("Usuario " + idUsuario + " no existe al listar tareas ");
        }

        List<Tarea> tareas = tareaRepository.findByUsuarioId(idUsuario);

        return tareas.stream()
                .map(tarea -> modelMapper.map(tarea, TareaData.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TareaData> allTareasProyecto(Long proyectoId) {
        Proyecto proyecto = proyectoRepository.findById(proyectoId)
                .orElseThrow(() -> new TareaServiceException("Proyecto no existe"));

        List<Tarea> tareas = tareaRepository.findByProyectoId(proyectoId);

        return tareas.stream()
                .map(t -> modelMapper.map(t, TareaData.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TareaData> allTareasEquipo(Long equipoId) {
        Equipo euipo = equipoRepository.findById(equipoId)
                .orElseThrow(() -> new TareaServiceException("Equipo no existe"));

        List<Tarea> tareas = tareaRepository.findByEquipoId(equipoId);

        return tareas.stream()
                .map(t -> modelMapper.map(t, TareaData.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TareaData findById(Long tareaId) {
        logger.debug("Buscando tarea " + tareaId);
        Tarea tarea = tareaRepository.findById(tareaId).orElse(null);
        if (tarea == null) return null;
        else return modelMapper.map(tarea, TareaData.class);
    }

    @Transactional
    public TareaData modificaTarea(Long idTarea, String nuevoTitulo, LocalDate fecha) {
        logger.debug("Modificando tarea " + idTarea + " - " + nuevoTitulo);
        Tarea tarea = tareaRepository.findById(idTarea).orElse(null);
        if (tarea == null) {
            throw new TareaServiceException("No existe tarea con id " + idTarea);
        }
        tarea.setTitulo(nuevoTitulo);
        tarea.setFechaLimite(fecha);
        tarea = tareaRepository.save(tarea);
        return modelMapper.map(tarea, TareaData.class);
    }

    @Transactional
    public void borraTarea(Long idTarea) {
        logger.debug("Borrando tarea " + idTarea);
        Tarea tarea = tareaRepository.findById(idTarea).orElse(null);
        if (tarea == null) {
            throw new TareaServiceException("No existe tarea con id " + idTarea);
        }
        tareaRepository.delete(tarea);
    }

    @Transactional
    public boolean usuarioContieneTarea(Long usuarioId, Long tareaId) {
        Tarea tarea = tareaRepository.findById(tareaId).orElse(null);
        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        if (tarea == null || usuario == null) {
            throw new TareaServiceException("No existe tarea o usuario id");
        }
        return usuario.getTareas().contains(tarea);
    }

    @Transactional
    public void terminarTarea(Long id){
        Tarea tarea = tareaRepository.findById(id).orElse(null);
        if (tarea == null ){
            throw new TareaServiceException("No existe tarea con id " + id);
        }
        tarea.setEstado(EstadoTarea.TERMINADA);
    }

    @Transactional
    public void cambiarEstadoTarea(Long id, EstadoTarea nuevoEstado) {
        Tarea tarea = tareaRepository.findById(id)
                .orElseThrow(() -> new TareaServiceException("No existe tarea"));
        tarea.setEstado(nuevoEstado);
    }

    @Transactional(readOnly = true)
    public List<TareaData> allTareasUsuarioOrdenadas(Long usuarioId) {
        return tareaRepository.findByUsuarioIdAndProyectoIsNullOrderByEstadoAscIdAsc(usuarioId)
                .stream()
                .map(t -> modelMapper.map(t, TareaData.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public TareaData actualizarProyecto(Long tareaId, Long proyectoId) {

        Tarea tarea = tareaRepository.findById(tareaId)
                .orElseThrow(() -> new TareaServiceException("Tarea no encontrada"));

        Proyecto nuevoProyecto = proyectoRepository.findById(proyectoId)
                .orElseThrow(() -> new TareaServiceException("Proyecto no encontrado"));

        Equipo equipoNuevo = nuevoProyecto.getEquipo();
        Equipo equipoActual = tarea.getEquipo();

        // 1) Si no tiene equipo → asignamos el del proyecto
        if (equipoActual == null) {
            tarea.setEquipo(equipoNuevo);
            equipoActual = equipoNuevo;
        }

        // 2) Si tiene equipo → debe ser el mismo que el del proyecto
        if (!equipoActual.equals(equipoNuevo)) {
            throw new TareaServiceException("El proyecto pertenece a un equipo distinto");
        }

        // 3) Validar que el usuario asignado pertenece al equipo (si hay usuario)
        if (tarea.getUsuario() != null) {
            boolean pertenece = usuarioRepository
                    .existsByIdAndEquipos_Id(tarea.getUsuario().getId(), equipoActual.getId());

            if (!pertenece) {
                throw new TareaServiceException("El usuario no pertenece al equipo del proyecto");
            }
        }

        tarea.setProyecto(nuevoProyecto);
        tareaRepository.save(tarea);

        return modelMapper.map(tarea, TareaData.class);
    }


    @Transactional
    public TareaData actualizarUsuario(Long tareaId, Long usuarioId) {

        Tarea tarea = tareaRepository.findById(tareaId)
                .orElseThrow(() -> new TareaServiceException("Tarea no encontrada"));

        Usuario nuevoUsuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new TareaServiceException("Usuario no encontrado"));

        Equipo equipoActual = tarea.getEquipo();

        // 1) Validación: si la tarea tiene equipo → usuario debe pertenecer al equipo
        if (equipoActual != null) {
            boolean pertenece = usuarioRepository
                    .existsByIdAndEquipos_Id(usuarioId, equipoActual.getId());

            if (!pertenece) {
                throw new TareaServiceException("Usuario no pertenece al equipo de la tarea");
            }
        }

        // 2) Si tiene proyecto → ya es del mismo equipo, no hace falta validar
        // porque equipoActual == proyecto.getEquipo()

        tarea.setUsuario(nuevoUsuario);
        tareaRepository.save(tarea);

        return modelMapper.map(tarea, TareaData.class);
    }

    @Transactional
    public TareaData actualizarEquipo(Long tareaId, Long equipoId) {

        Tarea tarea = tareaRepository.findById(tareaId)
                .orElseThrow(() -> new TareaServiceException("Tarea no encontrada"));

        Equipo nuevoEquipo = equipoRepository.findById(equipoId)
                .orElseThrow(() -> new TareaServiceException("Equipo no encontrado"));

        Equipo equipoActual = tarea.getEquipo();

        // 1) Si ya tenía equipo → NO se puede cambiar
        if (equipoActual != null) {
            throw new TareaServiceException("No se puede cambiar el equipo de una tarea que ya tiene equipo asignado");
        }

        // 2) Si la tarea tiene usuario -> debe pertenecer al nuevo equipo
        if (tarea.getUsuario() != null) {
            boolean pertenece = usuarioRepository
                    .existsByIdAndEquipos_Id(tarea.getUsuario().getId(), nuevoEquipo.getId());

            if (!pertenece) {
                throw new TareaServiceException("El usuario asignado no pertenece al nuevo equipo");
            }
        }

        // 3) Si la tarea tiene proyecto → debe pertenecer al mismo equipo
        if (tarea.getProyecto() != null &&
                !tarea.getProyecto().getEquipo().equals(nuevoEquipo)) {

            throw new TareaServiceException("El proyecto asignado pertenece a otro equipo");
        }

        tarea.setEquipo(nuevoEquipo);
        tareaRepository.save(tarea);
        return modelMapper.map(tarea, TareaData.class);
    }


    public TareaData modificarFechaLimite(Long id, LocalDate fecha) {
        Tarea tarea = tareaRepository.findById(id)
                .orElseThrow(() -> new TareaServiceException("No existe tarea"));

        tarea.setFechaLimite(fecha);
        tareaRepository.save(tarea);

        return modelMapper.map(tarea, TareaData.class);
    }
}
