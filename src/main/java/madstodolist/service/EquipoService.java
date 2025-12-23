// java
package madstodolist.service;

import madstodolist.dto.EquipoData;
import madstodolist.dto.UsuarioData;
import madstodolist.model.Equipo;
import madstodolist.model.Usuario;
import madstodolist.repository.EquipoRepository;
import madstodolist.repository.UsuarioRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class EquipoService {

    @Autowired
    private EquipoRepository equipoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ModelMapper modelMapper;

    // Crear equipo: ahora recibe el id del actor (creador) y lo marca como admin y miembro
    @Transactional
    public EquipoData crearEquipo(String nombre, Long actorUserId) {
        if (actorUserId == null) {
            throw new EquipoServiceException("Usuario no autenticado");
        }
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new EquipoServiceException("El nombre del equipo no puede estar vacío");
        }

        Usuario actor = usuarioRepository.findById(actorUserId)
                .orElseThrow(() -> new EquipoServiceException("Usuario no encontrado: " + actorUserId));

        Equipo e = new Equipo(nombre.trim());
        e.setAdminUserId(actorUserId);

        // Guardar el equipo primero para obtener el ID
        e = equipoRepository.save(e);

        // Ahora añadir al creador como miembro
        e.addUsuario(actor);

        // Guardar de nuevo para persistir la relación
        e = equipoRepository.save(e);

        return modelMapper.map(e, EquipoData.class);
    }

    // Recuperar equipo por ID
    @Transactional(readOnly = true)
    public EquipoData recuperarEquipo(Long id) {
        Equipo e = equipoRepository.findById(id)
                .orElseThrow(() -> new EquipoServiceException("Equipo no encontrado: " + id));
        return modelMapper.map(e, EquipoData.class);
    }

    @Transactional(readOnly = true)
    public boolean esAdminDeEquipo(Long equipoId, Long usuarioId) {
        Equipo e = equipoRepository.findById(equipoId)
                .orElseThrow(() -> new EquipoServiceException("Equipo no encontrado: " + equipoId));
        return e.getAdminUserId() != null && e.getAdminUserId().equals(usuarioId);
    }

    // Listado de equipos ordenados por nombre
    @Transactional(readOnly = true)
    public List<EquipoData> findAllOrdenadoPorNombre() {
        return equipoRepository.findAll()
                .stream()
                .sorted((e1, e2) -> e1.getNombre().compareToIgnoreCase(e2.getNombre()))
                .map(e -> modelMapper.map(e, EquipoData.class))
                .collect(Collectors.toList());
    }

    // Añadir usuario a equipo
    @Transactional
    public void añadirUsuarioAEquipo(Long equipoId, Long usuarioId) {
        Equipo equipo = equipoRepository.findById(equipoId)
                .orElseThrow(() -> new EquipoServiceException("Equipo no encontrado: " + equipoId));
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EquipoServiceException("Usuario no encontrado: " + usuarioId));

        if (equipo.getUsuarios().contains(usuario)) {
            throw new EquipoServiceException("El usuario ya pertenece al equipo");
        }

        equipo.addUsuario(usuario);
    }

    @Transactional
    public void añadirUsuarioAEquipoComoAdmin(Long equipoId, Long usuarioId, Long actorUserId) {
        if (!esAdminDeEquipo(equipoId, actorUserId)) {
            throw new EquipoServiceException("Solo el administrador del equipo puede añadir miembros");
        }
        añadirUsuarioAEquipo(equipoId, usuarioId);
    }

    // Listado de usuarios de un equipo (incluye al administrador)
    @Transactional(readOnly = true)
    public List<UsuarioData> usuariosEquipo(Long equipoId) {
        Equipo equipo = equipoRepository.findById(equipoId)
                .orElseThrow(() -> new EquipoServiceException("Equipo no encontrado: " + equipoId));

        return equipo.getUsuarios().stream()
                .map(u -> modelMapper.map(u, UsuarioData.class))
                .collect(Collectors.toList());
    }

    // Listado de equipos de un usuario
    @Transactional(readOnly = true)
    public List<EquipoData> equiposUsuario(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EquipoServiceException("Usuario no encontrado: " + usuarioId));

        return usuario.getEquipos().stream()
                .map(e -> modelMapper.map(e, EquipoData.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public void quitarUsuarioDeEquipo(Long equipoId, Long usuarioId) {
        Equipo equipo = equipoRepository.findById(equipoId)
                .orElseThrow(() -> new EquipoServiceException("Equipo no encontrado: " + equipoId));
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EquipoServiceException("Usuario no encontrado: " + usuarioId));

        if (!equipo.getUsuarios().contains(usuario)) {
            throw new EquipoServiceException("El usuario " + usuarioId + " no pertenece al equipo " + equipoId);
        }

        equipo.removeUsuario(usuario);
    }

    @Transactional
    public void quitarUsuarioDeEquipoComoAdmin(Long equipoId, Long usuarioId, Long actorUserId) {
        if (!esAdminDeEquipo(equipoId, actorUserId)) {
            throw new EquipoServiceException("Solo el administrador del equipo puede eliminar miembros");
        }
        quitarUsuarioDeEquipo(equipoId, usuarioId);
    }

    @Transactional
    public EquipoData actualizarNombreEquipo(Long equipoId, String nuevoNombre, Long actorUserId) {
        if (nuevoNombre == null || nuevoNombre.trim().isEmpty())
            throw new EquipoServiceException("El nombre del equipo no puede estar vacío");

        Equipo equipo = equipoRepository.findById(equipoId)
                .orElseThrow(() -> new EquipoServiceException("Equipo no encontrado: " + equipoId));

        if (equipo.getAdminUserId() == null || !equipo.getAdminUserId().equals(actorUserId)) {
            throw new EquipoServiceException("Solo el administrador del equipo puede renombrarlo");
        }

        equipo.setNombre(nuevoNombre.trim());
        equipo = equipoRepository.save(equipo);

        return modelMapper.map(equipo, EquipoData.class);
    }

    @Transactional
    public void eliminarEquipo(Long equipoId) {
        Equipo equipo = equipoRepository.findById(equipoId)
                .orElseThrow(() -> new EquipoServiceException("Equipo no encontrado: " + equipoId));
        for (Usuario u : new java.util.ArrayList<>(equipo.getUsuarios())) {
            equipo.removeUsuario(u);
        }
        equipoRepository.delete(equipo);
    }

    @Transactional(readOnly = true)
    public List<UsuarioData> usuariosNoMiembros(Long equipoId) {
        Equipo equipo = equipoRepository.findById(equipoId)
                .orElseThrow(() -> new EquipoServiceException("Equipo no encontrado: " + equipoId));

        Iterable<Usuario> todosIterable = usuarioRepository.findAll();
        List<Usuario> todos = java.util.stream.StreamSupport.stream(todosIterable.spliterator(), false)
                .collect(Collectors.toList());

        return todos.stream()
                .filter(u -> !equipo.getUsuarios().contains(u))
                .map(u -> modelMapper.map(u, UsuarioData.class))
                .collect(Collectors.toList());
    }
}