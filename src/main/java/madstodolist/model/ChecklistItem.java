package madstodolist.model;

import javax.persistence.*;

@Entity
@Table(name = "checklist_items")
public class ChecklistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String texto;

    private boolean completado;

    @ManyToOne(optional = false)
    private Tarea tarea;

    protected ChecklistItem() { }

    public ChecklistItem(Tarea tarea, String texto) {
        this.tarea = tarea;
        this.texto = texto;
        this.completado = false;
    }

    public Long getId() {
        return id;
    }

    public String getTexto() {
        return texto;
    }
    public void setTexto (String texto){ this.texto = texto; }

    public boolean getCompletado() {
        return completado;
    }
    public void setCompletado(boolean comp) { this.completado = comp; }

    public Tarea getTarea() {
        return tarea;
    }
    public void setTarea(Tarea tarea) { this.tarea = tarea; }

    public void completar() { this.completado = true; }

    public void desmarcarCompletado() {
        this.completado = false;
    }
}
