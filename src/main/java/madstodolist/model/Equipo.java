package madstodolist.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "equipos") // La tabla en BD se llamará "equipos"
public class Equipo implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nombre;

    // 🔹 Relación ManyToMany con Usuario LADO  DUEÑO
    @ManyToMany
    @JoinTable(
            name = "equipos_usuarios",
            joinColumns = @JoinColumn(name = "equipo_id"),
            inverseJoinColumns = @JoinColumn(name = "usuario_id")
    )
    private Set<Usuario> usuarios = new HashSet<>();

    @Column(name = "admin_user_id")
    private Long adminUserId;

    //Relacion con proyecto LADO INVERSO
    @OneToMany(mappedBy = "equipo", cascade = CascadeType.ALL)
    private Set<Proyecto> proyectos = new HashSet<>();

    //LADO INVERSO
    @OneToMany(mappedBy="equipo")
    private Set<Tarea> tareas = new HashSet<>();

    // 🔹 Constructor vacío requerido por JPA
    public Equipo() { }

    // 🔹 Constructor con nombre
    //public Equipo(String nombre, Long adminUserId) {
    public Equipo(String nombre) {
        this.nombre = nombre;
        //this.adminUserId = adminUserId;
    }

    // 🔹 Getters y Setters
    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }

    public void setNombre(String nombre) { this.nombre = nombre; }

    public Set<Usuario> getUsuarios() { return usuarios; }

    public Long getAdminUserId() { return adminUserId; }

    public void setAdminUserId(Long adminUserId) { this.adminUserId = adminUserId; }

    public Set<Proyecto> getProyectos() { return proyectos; }

    public Set<Tarea> getTareas() { return tareas; }

    public void addProyecto(Proyecto proyecto) {
        if (!proyectos.contains(proyecto)) {
            proyectos.add(proyecto);
            proyecto.setEquipo(this);
        }
    }

    // 🔹 Helpers de dominio
    public void addUsuario(Usuario usuario) {
        if (!usuarios.contains(usuario)) {
            usuarios.add(usuario);
            usuario.getEquipos().add(this);
        }
    }

    public void addTarea(Tarea tarea) {
        if (!tareas.contains(tarea)) {
            tareas.add(tarea);
            // NO llamar tarea.setEquipo(), eso pertenece al lado dueño
        }
    }

    public void removeUsuario(Usuario usuario) {
        this.usuarios.remove(usuario);
        usuario.getEquipos().remove(this);
    }


    // 🔹 equals y hashCode por nombre (único)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Equipo)) return false;
        Equipo equipo = (Equipo) o;
        return nombre != null && nombre.equals(equipo.nombre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nombre);
    }



}
