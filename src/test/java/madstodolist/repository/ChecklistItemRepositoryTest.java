package madstodolist.repository;

import madstodolist.model.ChecklistItem;
import madstodolist.model.Tarea;
import madstodolist.model.Usuario;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Sql(scripts = "/clean-db.sql")
public class ChecklistItemRepositoryTest {

    @Autowired
    private ChecklistItemRepository checklistItemRepository;

    @Autowired
    private TareaRepository tareaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private static class DatosChecklist {
        Usuario usuario;
        Tarea tarea;
        ChecklistItem item;
    }

    private DatosChecklist crearUsuarioTareaYItem(String textoItem) {
        DatosChecklist datos = new DatosChecklist();

        Usuario usuario = new Usuario("user@ua");
        usuarioRepository.save(usuario);

        Tarea tarea = new Tarea(usuario, "Tarea con checklist", null);
        tareaRepository.save(tarea);

        ChecklistItem item = new ChecklistItem(tarea, textoItem);
        checklistItemRepository.save(item);

        datos.usuario = usuario;
        datos.tarea = tarea;
        datos.item = item;

        return datos;
    }

    @Test
    public void guardarChecklistItem() {
        DatosChecklist datos = crearUsuarioTareaYItem("Primer paso");

        assertThat(datos.item.getId()).isNotNull();

        ChecklistItem recuperado =
                checklistItemRepository.findById(datos.item.getId()).orElse(null);

        assertThat(recuperado).isNotNull();
        assertThat(recuperado.getTexto()).isEqualTo("Primer paso");
        assertThat(recuperado.getCompletado()).isFalse();
        assertThat(recuperado.getTarea().getId())
                .isEqualTo(datos.tarea.getId());
    }

    @Test
    public void editarChecklistItem() {
        DatosChecklist datos = crearUsuarioTareaYItem("Texto original");

        datos.item.setTexto("Texto modificado");
        datos.item.setCompletado(true);
        checklistItemRepository.save(datos.item);

        ChecklistItem actualizado =
                checklistItemRepository.findById(datos.item.getId()).orElse(null);

        assertThat(actualizado).isNotNull();
        assertThat(actualizado.getTexto()).isEqualTo("Texto modificado");
        assertThat(actualizado.getCompletado()).isTrue();
    }

    @Test
    public void listarChecklistPorTarea() {
        DatosChecklist datos = crearUsuarioTareaYItem("Paso 1");

        ChecklistItem item2 = new ChecklistItem(datos.tarea, "Paso 2");
        checklistItemRepository.save(item2);

        List<ChecklistItem> checklist =
                checklistItemRepository.findByTareaOrderByIdAsc(datos.tarea);

        assertThat(checklist).hasSize(2);
        assertThat(checklist.get(0).getTexto()).isEqualTo("Paso 1");
        assertThat(checklist.get(1).getTexto()).isEqualTo("Paso 2");
    }

    @Test
    public void borrarChecklistItem() {
        DatosChecklist datos = crearUsuarioTareaYItem("Paso a borrar");

        Long itemId = datos.item.getId();

        checklistItemRepository.deleteById(itemId);

        assertThat(checklistItemRepository.findById(itemId)).isEmpty();

        List<ChecklistItem> checklist =
                checklistItemRepository.findByTareaOrderByIdAsc(datos.tarea);

        assertThat(checklist).isEmpty();
    }
}
