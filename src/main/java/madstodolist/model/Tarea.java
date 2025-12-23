package madstodolist.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "tareas")
public class Tarea implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    private String titulo;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "equipo_id")
    private Equipo equipo;

    @ManyToOne
    @JoinColumn(name = "proyecto_id")
    private Proyecto proyecto;

    @NotNull
    @Enumerated(EnumType.STRING)
    private EstadoTarea estado = EstadoTarea.PENDIENTE;

    private LocalDate fechaLimite;

    // Constructor vacío necesario para JPA/Hibernate.
    // No debe usarse desde la aplicación.
    public Tarea() {}

    // Tarea personal
    public Tarea(Usuario usuario, String titulo, LocalDate fecha) {
        this.titulo = titulo;
        this.usuario = usuario;
        this.fechaLimite = fecha;
    }

    // Tarea de equipo
    public Tarea(Equipo equipo, String titulo, LocalDate fecha) {
        this.titulo = titulo;
        this.equipo = equipo;
        this.fechaLimite = fecha;
    }

    // Tarea de proyecto
    public Tarea(Proyecto proyecto, String titulo, LocalDate fecha) {
        this.titulo = titulo;
        this.proyecto = proyecto;
        this.equipo = proyecto.getEquipo();
        this.fechaLimite = fecha;
    }

    // Getters y setters básicos

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    // Getters y setters de la relación muchos-a-uno con Usuario

    public Usuario getUsuario() {
        return usuario;
    }

    // Método para establecer la relación con el usuario

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Equipo getEquipo(){
        return equipo;
    }

    public void setEquipo(Equipo equipo){
        this.equipo = equipo;
    }

    public Proyecto getProyecto(){
        return proyecto;
    }

    public void setProyecto(Proyecto proyecto){
        this.proyecto = proyecto;
        this.equipo = proyecto.getEquipo();
    }

    public EstadoTarea getEstado() { return estado; }
    public void setEstado(EstadoTarea estado) { this.estado = estado; }

    // Helpers de compatibilidad para código antiguo (opcional, pero ayuda)
    public boolean getTerminada() { return this.estado == EstadoTarea.TERMINADA; }

    public void setFechaLimite(LocalDate fecha) { this.fechaLimite = fecha; }

    public LocalDate getFechaLimite() { return this.fechaLimite; }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tarea tarea = (Tarea) o;
        if (id != null && tarea.id != null)
            // Si tenemos los ID, comparamos por ID
            return Objects.equals(id, tarea.id);
        // si no comparamos por campos obligatorios
        return titulo.equals(tarea.titulo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(titulo);
    }


}
