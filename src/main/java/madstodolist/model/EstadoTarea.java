package madstodolist.model;

public enum EstadoTarea {
    PENDIENTE("Pendiente"),
    EN_CURSO("En curso"),
    TERMINADA("Hecho");

    private final String descripcion;

    EstadoTarea(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
