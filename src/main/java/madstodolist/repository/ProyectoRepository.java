package madstodolist.repository;

import madstodolist.model.Proyecto;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ProyectoRepository extends CrudRepository<Proyecto, Long> {
    List<Proyecto> findByEquipoId(Long equipoId);
}
