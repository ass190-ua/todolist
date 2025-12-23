package madstodolist.repository;

import madstodolist.model.ChecklistItem;
import madstodolist.model.Tarea;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ChecklistItemRepository extends CrudRepository<ChecklistItem, Long> {
    List<ChecklistItem> findAll();

    List<ChecklistItem> findByTareaOrderByIdAsc(Tarea tarea);
}
