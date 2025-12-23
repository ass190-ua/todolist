package madstodolist.repository;

import madstodolist.model.Tarea;
import org.springframework.data.repository.CrudRepository;

import java.util.Arrays;
import java.util.List;

public interface TareaRepository extends CrudRepository<Tarea, Long> {
    List<Tarea> findByEquipoId(Long id);
    List<Tarea> findByProyectoId(Long id);
    List<Tarea> findByUsuarioId(Long usuarioId);
    List<Tarea> findByUsuarioIdAndProyectoIsNullOrderByEstadoAscIdAsc(Long usuarioId);
    List<Tarea> findByUsuarioIdOrderByEstadoAscIdAsc(Long usuarioId);
}
