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
    public TareaData nuevaTareaUsuario(Long idUsuario, String tituloTarea) {
        logger.debug("Añadiendo tarea " + tituloTarea + " al usuario " + idUsuario);
        Usuario usuario = usuarioRepository.findById(idUsuario).orElse(null);
        if (usuario == null) {
            throw new TareaServiceException("Usuario " + idUsuario + " no existe al crear tarea " + tituloTarea);
        }
        Tarea tarea = new Tarea(usuario, tituloTarea);
        tareaRepository.save(tarea);
        return modelMapper.map(tarea, TareaData.class);
    }

    @Transactional
    public TareaData nuevaTareaProyecto(Long idProyecto, Long idUsuario, String tituloTarea) {
        logger.debug("Añadiendo tarea " + tituloTarea + " al proyecto " + idProyecto);

        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new TareaServiceException("Usuario no encontrado"));

        Proyecto proyecto = proyectoRepository.findById(idProyecto)
                .orElseThrow(() -> new TareaServiceException("Proyecto no encontrado"));

        Tarea tarea = new Tarea(usuario, tituloTarea);
        tarea.setProyecto(proyecto); // Vinculamos al proyecto
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

        List<TareaData> tareas = usuario.getTareas().stream()
                .filter(t -> t.getProyecto() == null) // <--- FILTRO AÑADIDO: Solo tareas sin proyecto
                .map(tarea -> modelMapper.map(tarea, TareaData.class))
                .collect(Collectors.toList());

        Collections.sort(tareas, (a, b) -> a.getId() < b.getId() ? -1 : a.getId() == b.getId() ? 0 : 1);
        return tareas;
    }

    @Transactional(readOnly = true)
    public List<TareaData> getTareasProyecto(Long idProyecto) {
        return tareaRepository.findByProyectoId(idProyecto).stream()
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
    public TareaData modificaTarea(Long idTarea, String nuevoTitulo) {
        logger.debug("Modificando tarea " + idTarea + " - " + nuevoTitulo);
        Tarea tarea = tareaRepository.findById(idTarea).orElse(null);
        if (tarea == null) {
            throw new TareaServiceException("No existe tarea con id " + idTarea);
        }
        tarea.setTitulo(nuevoTitulo);
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
}
