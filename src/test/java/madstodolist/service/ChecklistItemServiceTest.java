package madstodolist.service;

import madstodolist.dto.ChecklistItemData;
import madstodolist.dto.TareaData;
import madstodolist.dto.UsuarioData;
import madstodolist.model.ChecklistItem;
import madstodolist.repository.ChecklistItemRepository;
import madstodolist.repository.TareaRepository;
import madstodolist.repository.UsuarioRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Sql(scripts = "/clean-db.sql")
public class ChecklistItemServiceTest {

    @Autowired
    ChecklistItemService checklistItemService;

    @Autowired
    TareaService tareaService;

    @Autowired
    UsuarioService usuarioService;

    private Long crearUsuario() {
        UsuarioData u = new UsuarioData();
        u.setEmail("user@ua");
        u.setPassword("123");
        return usuarioService.registrar(u).getId();
    }

    @Test
    public void añadirChecklistItemATarea() {
        Long userId = crearUsuario();
        TareaData tarea = tareaService.nuevaTareaUsuario(userId, "Tarea", null);

        ChecklistItemData item =
                checklistItemService.crearItem(tarea.getId(), "Primer paso");

        assertThat(item.getId()).isNotNull();
        assertThat(item.getTexto()).isEqualTo("Primer paso");
        assertThat(item.getCompletado()).isFalse();

        List<ChecklistItemData> checklist =
                checklistItemService.obtenerChecklistDeTarea(tarea.getId());

        assertThat(checklist).hasSize(1);
        assertThat(checklist).contains(item);
    }

    @Test
    public void completarChecklistItem() {
        Long userId = crearUsuario();
        TareaData tarea = tareaService.nuevaTareaUsuario(userId, "Tarea", null);

        ChecklistItemData item =
                checklistItemService.crearItem(tarea.getId(), "Paso");

        checklistItemService.completarItem(item.getId());

        ChecklistItemData actualizado =
                checklistItemService.findById(item.getId());

        assertThat(actualizado.getCompletado()).isTrue();
    }

    @Test
    public void desmarcarChecklistItem() {
        Long userId = crearUsuario();
        TareaData tarea = tareaService.nuevaTareaUsuario(userId, "Tarea", null);

        ChecklistItemData item =
                checklistItemService.crearItem(tarea.getId(), "Paso");

        checklistItemService.completarItem(item.getId());
        checklistItemService.desmarcarItem(item.getId());

        ChecklistItemData actualizado =
                checklistItemService.findById(item.getId());

        assertThat(actualizado.getCompletado()).isFalse();
    }

    @Test
    public void borrarChecklistItem() {
        Long userId = crearUsuario();
        TareaData tarea = tareaService.nuevaTareaUsuario(userId, "Tarea", null);

        ChecklistItemData item =
                checklistItemService.crearItem(tarea.getId(), "Paso");

        checklistItemService.borrarItem(item.getId());

        assertThatThrownBy(() ->
                checklistItemService.findById(item.getId())
        ).isInstanceOf(ChecklistItemServiceException.class);
    }
}
