package madstodolist.dto;

import java.io.Serializable;
import java.util.Objects;

// Data Transfer Object para la clase ChecklistItem
public class ChecklistItemData implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String texto;
    private boolean completado;
    private Long tareaId;

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getTexto() { return texto; }

    public void setTexto(String texto) { this.texto = texto; }

    public boolean getCompletado() { return completado; }

    public void setCompletado(boolean completado) { this.completado = completado; }

    public Long getTareaId() { return tareaId; }

    public void setTareaId(Long tareaId) { this.tareaId = tareaId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChecklistItemData)) return false;
        ChecklistItemData that = (ChecklistItemData) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
