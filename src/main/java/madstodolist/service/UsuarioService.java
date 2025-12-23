package madstodolist.service;

import madstodolist.dto.UsuarioData;
import madstodolist.model.Usuario;
import madstodolist.repository.UsuarioRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import madstodolist.service.UsuarioServiceException;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class UsuarioService {

    Logger logger = LoggerFactory.getLogger(UsuarioService.class);

    public enum LoginStatus { LOGIN_OK, USER_NOT_FOUND, ERROR_PASSWORD, BLOCKED }
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public LoginStatus login(String eMail, String password) {
        Optional<Usuario> usuario = usuarioRepository.findByEmail(eMail);
        if (!usuario.isPresent()) return LoginStatus.USER_NOT_FOUND;

        Usuario u = usuario.get();
        if (Boolean.TRUE.equals(u.getBloqueado())) {
            return LoginStatus.BLOCKED; // <<< nuevo
        }
        if (!u.getPassword().equals(password)) {
            return LoginStatus.ERROR_PASSWORD;
        }
        return LoginStatus.LOGIN_OK;
    }

    // Se añade un usuario en la aplicación.
    // El email y password del usuario deben ser distinto de null
    // El email no debe estar registrado en la base de datos
    @Transactional
    public UsuarioData registrar(UsuarioData usuario) {
        Optional<Usuario> usuarioBD = usuarioRepository.findByEmail(usuario.getEmail());
        if (usuarioBD.isPresent())
            throw new UsuarioServiceException("El usuario " + usuario.getEmail() + " ya está registrado");
        else if (usuario.getEmail() == null)
            throw new UsuarioServiceException("El usuario no tiene email");
        else if (usuario.getPassword() == null)
            throw new UsuarioServiceException("El usuario no tiene password");
        else {
            // Enforce: solo un admin
            if (Boolean.TRUE.equals(usuario.getAdmin()) && usuarioRepository.existsByAdminTrue()) {
                usuario.setAdmin(false); // o lanza excepción, como prefieras
            }
            Usuario usuarioNuevo = modelMapper.map(usuario, Usuario.class);
            usuarioNuevo = usuarioRepository.save(usuarioNuevo);
            return modelMapper.map(usuarioNuevo, UsuarioData.class);
        }
    }

    @Transactional
    public UsuarioData actualizarPerfil(Long idUsuario, UsuarioData datos) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new UsuarioServiceException("Usuario no encontrado"));

        // Campos editables por el propio usuario
        usuario.setNombre(datos.getNombre());
        usuario.setEmail(datos.getEmail());
        usuario.setFechaNacimiento(datos.getFechaNacimiento());

        usuario = usuarioRepository.save(usuario);
        return modelMapper.map(usuario, UsuarioData.class);
    }

    @Transactional
    public void cambiarPassword(Long idUsuario, String passwordActual, String passwordNueva) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new UsuarioServiceException("Usuario no encontrado"));

        if (!usuario.getPassword().equals(passwordActual)) {
            throw new UsuarioServiceException("La contraseña actual no es correcta");
        }

        usuario.setPassword(passwordNueva);
        usuarioRepository.save(usuario);
    }

    @Transactional(readOnly = true)
    public UsuarioData findByEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
        if (usuario == null) return null;
        else {
            return modelMapper.map(usuario, UsuarioData.class);
        }
    }

    @Transactional(readOnly = true)
    public UsuarioData findById(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        if (usuario == null) return null;
        else {
            return modelMapper.map(usuario, UsuarioData.class);
        }
    }

    @Transactional(readOnly = true)
    public List<UsuarioData> findAll() {
        List<UsuarioData> res = new ArrayList<>();
        usuarioRepository.findAll()
                .forEach(u -> res.add(modelMapper.map(u, UsuarioData.class)));
        return res;
    }

    @Transactional(readOnly = true)
    public boolean existeAdministrador() {
        return usuarioRepository.existsByAdminTrue();
    }

    @Transactional
    public UsuarioData toggleBloqueo(Long idUsuario) {
        Usuario u = usuarioRepository.findById(idUsuario).orElse(null);
        if (u == null) return null;
        u.setBloqueado(!Boolean.TRUE.equals(u.getBloqueado()));
        u = usuarioRepository.save(u);
        return modelMapper.map(u, UsuarioData.class);
    }

    @Transactional
    public void borrarUsuario(Long idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new UsuarioServiceException("Usuario no encontrado"));

        // Paso 1: Desvincular al usuario de todos los equipos (tabla intermedia)
        // Usamos una copia de la lista para evitar errores de concurrencia al borrar
        for (madstodolist.model.Equipo equipo : new ArrayList<>(usuario.getEquipos())) {
            equipo.removeUsuario(usuario);
        }

        // Paso 2: Borrar el usuario (Las tareas se borran solas gracias al CascadeType.ALL del Paso 1)
        usuarioRepository.delete(usuario);
    }
}
