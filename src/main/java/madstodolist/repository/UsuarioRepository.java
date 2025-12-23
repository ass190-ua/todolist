package madstodolist.repository;

import madstodolist.model.Usuario;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UsuarioRepository extends CrudRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String s);

    boolean existsByAdminTrue();
    long countByAdminTrue();
    long countByBloqueadoTrue();

    boolean existsByIdAndEquipos_Id(Long usuarioId, Long id);
}
