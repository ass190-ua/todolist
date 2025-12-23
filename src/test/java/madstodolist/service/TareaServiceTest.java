package madstodolist.service;

import madstodolist.dto.EquipoData;
import madstodolist.dto.ProyectoData;
import madstodolist.dto.TareaData;
import madstodolist.dto.UsuarioData;
import madstodolist.model.EstadoTarea;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

// Hemos eliminado todos los @Transactional de los tests
// y usado un script para limpiar la BD de test después de
// cada test
// https://dev.to/henrykeys/don-t-use-transactional-in-tests-40eb

@SpringBootTest
@Sql(scripts = "/clean-db.sql")
public class TareaServiceTest {

    @Autowired
    UsuarioService usuarioService;

    @Autowired
    TareaService tareaService;

    @Autowired
    EquipoService equipoService;

    @Autowired
    ProyectoService proyectoService;


    // Método para inicializar los datos de prueba en la BD
    Map<String, Long> addUsuarioTareasBD() {
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@ua");
        usuario.setPassword("123");

        UsuarioData usuarioNuevo = usuarioService.registrar(usuario);

        TareaData tarea1 = tareaService.nuevaTareaUsuario(usuarioNuevo.getId(), "Lavar coche", null);
        tareaService.nuevaTareaUsuario(usuarioNuevo.getId(), "Renovar DNI", null);

        Map<String, Long> ids = new HashMap<>();
        ids.put("usuarioId", usuarioNuevo.getId());
        ids.put("tareaId", tarea1.getId());
        return ids;
    }

    private Long crearUsuarioBasico(String email) {
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail(email);
        usuario.setPassword("123");

        UsuarioData usuarioNuevo = usuarioService.registrar(usuario);
        return usuarioNuevo.getId();
    }

    private Long crearProyectoParaUsuasrio() {
        Long userId = crearUsuarioBasico("user@ua");
        EquipoData equipo = equipoService.crearEquipo("Equipo A", userId);
        Long equipoId = equipo.getId();

        ProyectoData proyecto = proyectoService.crearProyecto("Proyecto X", "Descripcion", equipoId);
        return proyecto.getId();
    }

    private Long crearEquipoUsuasrio() {
        Long userId = crearUsuarioBasico("user@ua");
        EquipoData equipo = equipoService.crearEquipo("Equipo A", userId);
        return equipo.getId();
    }


    //TESTEAMOS CREACION DE TAREAS PARA LOS 3 CASOS
    //casos ok
    @Test
    public void testNuevaTareaUsuario() {
        // GIVEN
        Long usuarioId = crearUsuarioBasico("personal@ua");

        // WHEN
        TareaData nuevaTarea = tareaService.nuevaTareaUsuario(usuarioId, "Práctica 1 de MADS", null);
        List<TareaData> tareas = tareaService.allTareasUsuario(usuarioId);

        // THEN
        // 1) La nueva tarea está en la lista del usuario
        assertThat(tareas).hasSize(1);
        assertThat(tareas).contains(nuevaTarea);

        // 2) A nivel de DTO, la pertenencia queda como tarea personal
        assertThat(nuevaTarea.getUsuarioId()).isEqualTo(usuarioId);
    }

    @Test
    public void crearTareaProyecto() {
        // GIVEN
        Long proyectoId = crearProyectoParaUsuasrio();

        // WHEN
        TareaData nuevaTarea = tareaService.nuevaTareaProyecto(proyectoId, "Práctica 1 de MADS", null);
        List<TareaData> tareas = tareaService.allTareasProyecto(proyectoId);

        // THEN
        // 1) La nueva tarea está en la lista del proyecto
        assertThat(tareas).hasSize(1);
        assertThat(tareas).contains(nuevaTarea);

        // 2) A nivel de DTO, la pertenencia queda como tarea del proyecto y equipo
        assertThat(nuevaTarea.getProyectoId()).isEqualTo(proyectoId);
        //FAlta sacar esto
        //assertThat(nuevaTarea.getEquipo()).isEqualTo(equipoId);
    }

    @Test
    public void crearTareaEquipo() {
        // GIVEN
        Long equipoId = crearEquipoUsuasrio();

        // WHEN
        TareaData nuevaTarea = tareaService.nuevaTareaEquipo(equipoId, "Práctica 1 de MADS", null);
        List<TareaData> tareas = tareaService.allTareasEquipo(equipoId);

        // THEN
        // 1) La nueva tarea está en la lista del proyecto
        assertThat(tareas).hasSize(1);
        assertThat(tareas).contains(nuevaTarea);

        // 2) A nivel de DTO, la pertenencia queda como tarea del equipo
        assertThat(nuevaTarea.getEquipoId()).isEqualTo(equipoId);
    }

    //casos error
    @Test
    public void nuevaTareaUsuarioNoExiste() {
        //GIVEN usuario no existente
        Long usuarioId = 9999L;

        //WHEN
        //creamos nueva tarea
        //THEN
        //lanza excepcion
        assertThatThrownBy(() -> tareaService.nuevaTareaUsuario(usuarioId, "Tarea fantasma", null))
                .isInstanceOf(TareaServiceException.class)
                .hasMessageContaining("Usuario");
    }

    @Test
    public void nuevaTareaEquipoNoExiste() {
        //GIVEN euiqpo no existente
        Long equipo = 9999L;

        //WHEN
        //creamos nueva tarea
        //THEN
        //lanza excepcion
        assertThatThrownBy(() -> tareaService.nuevaTareaEquipo(equipo, "Tarea fantasma", null))
                .isInstanceOf(TareaServiceException.class)
                .hasMessageContaining("Equipo");
    }

    @Test
    public void nuevaTareaProyectoNoExiste() {
        //GIVEN proyecto no existente
        Long proeyctoId = 9999L;

        //WHEN
        //creamos nueva tarea
        //THEN
        //lanza excepcion
        assertThatThrownBy(() -> tareaService.nuevaTareaProyecto(proeyctoId, "Tarea fantasma", null))
                .isInstanceOf(TareaServiceException.class)
                .hasMessageContaining("Proyecto");
    }

    //BUSCAR TAREAS
    @Test
    public void testBuscarTarea() {
        Long tareaId = addUsuarioTareasBD().get("tareaId");
        TareaData lavarCoche = tareaService.findById(tareaId);
        assertThat(lavarCoche).isNotNull();
        assertThat(lavarCoche.getTitulo()).isEqualTo("Lavar coche");
    }

    @Test
    public void buscarTareaNoExiste() {
        assertThat(tareaService.findById(9999L)).isNull();
    }

    @Test
    public void allTareasUsuarioNoExiste() {
        assertThatThrownBy(() -> tareaService.allTareasUsuario(9999L))
                .isInstanceOf(TareaServiceException.class)
                .hasMessageContaining("Usuario");
    }

    @Test
    public void allTareasProyectoNoExiste() {
        assertThatThrownBy(() -> tareaService.allTareasProyecto(9999L))
                .isInstanceOf(TareaServiceException.class)
                .hasMessageContaining("Proyecto");
    }

    @Test
    public void allTareasEquipoNoExiste() {
        assertThatThrownBy(() -> tareaService.allTareasEquipo(9999L))
                .isInstanceOf(TareaServiceException.class)
                .hasMessageContaining("Equipo");
    }

    //MODIFICAR ATRIBUTOS
    @Test
    public void testModificarTareaTitulo() {
        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");
        Long tareaId = ids.get("tareaId");

        tareaService.modificaTarea(tareaId, "Limpiar los cristales del coche", null);
        TareaData tareaBD = tareaService.findById(tareaId);

        assertThat(tareaBD.getTitulo()).isEqualTo("Limpiar los cristales del coche");
        List<TareaData> tareas = tareaService.allTareasUsuario(usuarioId);
        assertThat(tareas).contains(tareaBD);
    }

    @Test
    public void modificarTareaNoExiste() {
        assertThatThrownBy(() ->
                tareaService.modificaTarea(9999L, "Nuevo titulo", null)
        ).isInstanceOf(TareaServiceException.class);
    }

    @Test
    public void cambiarEstadoKanban() {
        Long usuarioId = crearUsuarioBasico("kanban@ua");

        TareaData t1 = tareaService.nuevaTareaUsuario(usuarioId, "T1", null);

        tareaService.cambiarEstadoTarea(t1.getId(), EstadoTarea.EN_CURSO);
        assertThat(tareaService.findById(t1.getId()).getEstado())
                .isEqualTo("EN_CURSO");

        tareaService.cambiarEstadoTarea(t1.getId(), EstadoTarea.TERMINADA);
        assertThat(tareaService.findById(t1.getId()).getEstado())
                .isEqualTo("TERMINADA");
    }

    @Test
    public void cambiarEstadoTareaNoExiste() {
        assertThatThrownBy(() ->
                tareaService.cambiarEstadoTarea(9999L, EstadoTarea.EN_CURSO)
        ).isInstanceOf(TareaServiceException.class);
    }

    @Test
    public void asignarEtiquetaATarea() {
        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");
        Long tareaId = ids.get("tareaId");
        assertThat(tareaService.usuarioContieneTarea(usuarioId, tareaId)).isTrue();
    }

    @Test
    public void acabarTarea() {
        // GIVEN
        Long tareaId = addUsuarioTareasBD().get("tareaId");

        // WHEN
        tareaService.terminarTarea(tareaId);

        // THEN
        TareaData tarea = tareaService.findById(tareaId);
        assertThat(tarea.getEstado()).isEqualTo("TERMINADA");
    }

    @Test
    public void acabarTareaException() {
        assertThatThrownBy(() -> tareaService.terminarTarea(999L))
                .isInstanceOf(TareaServiceException.class);
    }

    @Test
    public void modificarFechaLimite() {
        Long usuarioId = crearUsuarioBasico("fecha@ua");
        TareaData t = tareaService.nuevaTareaUsuario(usuarioId, "Tarea con fecha", null);

        LocalDate fecha = LocalDate.of(2025, 2, 1);
        tareaService.modificarFechaLimite(t.getId(), fecha);

        assertThat(tareaService.findById(t.getId()).getFechaLimite())
                .isEqualTo(fecha);
    }

    @Test
    public void testEliminarFechaLimite() {
        Map<String, Long> ids = addUsuarioTareasBD();
        Long tareaId = ids.get("tareaId");

        // Primero ponemos una fecha
        tareaService.modificaTarea(tareaId, "Lavar coche", LocalDate.now());

        // Ahora la eliminamos
        tareaService.modificaTarea(tareaId, "Lavar coche", null);

        TareaData tareaBD = tareaService.findById(tareaId);

        assertThat(tareaBD.getFechaLimite()).isNull();
    }

    //BORRAR
    @Test
    public void testBorrarTarea() {
        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");
        Long tareaId = ids.get("tareaId");

        tareaService.borraTarea(tareaId);
        assertThat(tareaService.findById(tareaId)).isNull();
        List<TareaData> tareas = tareaService.allTareasUsuario(usuarioId);
        assertThat(tareas).hasSize(1);
    }

    @Test
    public void borrarTareaNoExiste() {
        assertThatThrownBy(() -> tareaService.borraTarea(9999L))
                .isInstanceOf(TareaServiceException.class)
                .hasMessageContaining("tarea");
    }


    //PERTENENCIA USUARIO/EQUIPO/PROYECTO
    @Test
    public void actualizarUsuario_usuarioNoPerteneceAlEquipo() {
        Long u1 = crearUsuarioBasico("u1@ua");
        Long u2 = crearUsuarioBasico("u2@ua");

        EquipoData e = equipoService.crearEquipo("Equipo A", u1);
        TareaData t = tareaService.nuevaTareaEquipo(e.getId(), "Tarea", null);

        assertThatThrownBy(() ->
                tareaService.actualizarUsuario(t.getId(), u2)
        ).isInstanceOf(TareaServiceException.class)
                .hasMessageContaining("Usuario no pertenece al equipo");
    }

    @Test
    public void actualizarUsuario_usuarioNoMiembroDelEquipoDelProyecto() {
        Long u1 = crearUsuarioBasico("u1@ua");
        Long u2 = crearUsuarioBasico("u2@ua");

        EquipoData e = equipoService.crearEquipo("E1", u1);
        ProyectoData p = proyectoService.crearProyecto("P", "d", e.getId());
        TareaData t = tareaService.nuevaTareaProyecto(p.getId(), "Tarea", null);

        assertThatThrownBy(() ->
                tareaService.actualizarUsuario(t.getId(), u2)
        ).isInstanceOf(TareaServiceException.class);
    }

    @Test
    public void actualizarEquipo_asignarEquipoPorPrimeraVez() {
        Long u = crearUsuarioBasico("u@ua");

        Long equipoId = equipoService.crearEquipo("E1", u).getId();

        TareaData t = tareaService.nuevaTareaUsuario(u, "Tarea", null);

        TareaData actualizada = tareaService.actualizarEquipo(t.getId(), equipoId);

        assertThat(actualizada.getEquipoId()).isEqualTo(equipoId);
    }

    @Test
    public void actualizarEquipo_noSePuedeCambiarEquipo() {
        Long u = crearUsuarioBasico("u@ua");

        Long e1 = equipoService.crearEquipo("E1", u).getId();
        Long e2 = equipoService.crearEquipo("E2", u).getId();

        TareaData t = tareaService.nuevaTareaEquipo(e1, "Tarea", null);

        assertThatThrownBy(() ->
                tareaService.actualizarEquipo(t.getId(), e2)
        ).isInstanceOf(TareaServiceException.class)
                .hasMessageContaining("No se puede cambiar el equipo de una tarea");
    }

    @Test
    public void actualizarEquipo_usuarioNoMiembroDelNuevoEquipo() {
        Long u1 = crearUsuarioBasico("u1@ua");
        Long u2 = crearUsuarioBasico("u2@ua");

        Long equipoNuevo = equipoService.crearEquipo("E2", u2).getId();

        TareaData t = tareaService.nuevaTareaUsuario(u1, "Tarea", null);

        assertThatThrownBy(() ->
                tareaService.actualizarEquipo(t.getId(), equipoNuevo)
        ).isInstanceOf(TareaServiceException.class);
    }

    @Test
    public void actualizarEquipo_proyectoDeOtroEquipo() {
        Long u = crearUsuarioBasico("u@ua");

        EquipoData e1 = equipoService.crearEquipo("E1", u);
        EquipoData e2 = equipoService.crearEquipo("E2", u);

        ProyectoData p = proyectoService.crearProyecto("P", "d", e1.getId());

        TareaData t = tareaService.nuevaTareaProyecto(p.getId(), "Tarea", null);

        assertThatThrownBy(() ->
                tareaService.actualizarEquipo(t.getId(), e2.getId())
        )
                .isInstanceOf(TareaServiceException.class)
                .hasMessageContaining("ya tiene equipo asignado");
    }

    @Test
    public void actualizarProyecto_cambiarDentroDelMismoEquipo() {
        Long u = crearUsuarioBasico("u@ua");

        EquipoData e = equipoService.crearEquipo("E1", u);

        ProyectoData p1 = proyectoService.crearProyecto("P1", "d", e.getId());
        ProyectoData p2 = proyectoService.crearProyecto("P2", "d", e.getId());

        TareaData t = tareaService.nuevaTareaProyecto(p1.getId(), "Tarea", null);

        TareaData actualizada = tareaService.actualizarProyecto(t.getId(), p2.getId());

        assertThat(actualizada.getProyectoId()).isEqualTo(p2.getId());
    }

    @Test
    public void actualizarProyecto_cambiarAProyectoDeOtroEquipo() {
        Long u = crearUsuarioBasico("u@ua");

        EquipoData e1 = equipoService.crearEquipo("E1", u);
        EquipoData e2 = equipoService.crearEquipo("E2", u);

        ProyectoData p1 = proyectoService.crearProyecto("P1", "d", e1.getId());
        ProyectoData p2 = proyectoService.crearProyecto("P2", "d", e2.getId());

        TareaData t = tareaService.nuevaTareaProyecto(p1.getId(), "Tarea", null);

        assertThatThrownBy(() ->
                tareaService.actualizarProyecto(t.getId(), p2.getId())
        ).isInstanceOf(TareaServiceException.class)
                .hasMessageContaining("El proyecto pertenece a un equipo distinto");
    }

    @Test
    public void actualizarProyecto_usuarioNoMiembroDelEquipo() {
        Long u1 = crearUsuarioBasico("u1@ua");
        Long u2 = crearUsuarioBasico("u2@ua");

        EquipoData e = equipoService.crearEquipo("E1", u1);
        ProyectoData p1 = proyectoService.crearProyecto("P1", "d", e.getId());

        //creamos tarea con usuario q no pertenece al equipo
        TareaData t = tareaService.nuevaTareaUsuario(u2, "Tarea", null);

        assertThatThrownBy(() ->
                tareaService.actualizarProyecto(t.getId(), p1.getId())
        ).isInstanceOf(TareaServiceException.class);
    }

    // Tests adicionales para estados EN_CURSO
    @Test
    public void testCrearTareaConEstadoInicial() {
        // GIVEN
        Long usuarioId = crearUsuarioBasico("estados@ua");

        // WHEN
        TareaData tarea = tareaService.nuevaTareaUsuario(usuarioId, "Nueva tarea", null);

        // THEN - Por defecto debe ser PENDIENTE
        assertThat(tarea.getEstado()).isEqualTo("PENDIENTE");
    }

    @Test
    public void testCambiarEstadoDePendienteAEnCurso() {
        // GIVEN
        Long usuarioId = crearUsuarioBasico("estados2@ua");
        TareaData tarea = tareaService.nuevaTareaUsuario(usuarioId, "Tarea en progreso", null);

        // WHEN
        tareaService.cambiarEstadoTarea(tarea.getId(), EstadoTarea.EN_CURSO);

        // THEN
        TareaData tareaActualizada = tareaService.findById(tarea.getId());
        assertThat(tareaActualizada.getEstado()).isEqualTo("EN_CURSO");
    }

    @Test
    public void testCambiarEstadoDeEnCursoATerminada() {
        // GIVEN
        Long usuarioId = crearUsuarioBasico("estados3@ua");
        TareaData tarea = tareaService.nuevaTareaUsuario(usuarioId, "Tarea a completar", null);
        tareaService.cambiarEstadoTarea(tarea.getId(), EstadoTarea.EN_CURSO);

        // WHEN
        tareaService.cambiarEstadoTarea(tarea.getId(), EstadoTarea.TERMINADA);

        // THEN
        TareaData tareaActualizada = tareaService.findById(tarea.getId());
        assertThat(tareaActualizada.getEstado()).isEqualTo("TERMINADA");
    }

    @Test
    public void testTareaProyectoConEstados() {
        // GIVEN
        Long proyectoId = crearProyectoParaUsuasrio();

        // WHEN
        TareaData tarea1 = tareaService.nuevaTareaProyecto(proyectoId, "Tarea pendiente", null);
        TareaData tarea2 = tareaService.nuevaTareaProyecto(proyectoId, "Tarea en curso", null);
        TareaData tarea3 = tareaService.nuevaTareaProyecto(proyectoId, "Tarea terminada", null);

        tareaService.cambiarEstadoTarea(tarea2.getId(), EstadoTarea.EN_CURSO);
        tareaService.cambiarEstadoTarea(tarea3.getId(), EstadoTarea.TERMINADA);

        // THEN - Verificar que las tareas tienen los estados correctos
        List<TareaData> todasLasTareas = tareaService.allTareasProyecto(proyectoId);

        List<TareaData> tareasPendientes = todasLasTareas.stream()
                .filter(t -> t.getEstado().equals("PENDIENTE"))
                .collect(java.util.stream.Collectors.toList());

        List<TareaData> tareasEnCurso = todasLasTareas.stream()
                .filter(t -> t.getEstado().equals("EN_CURSO"))
                .collect(java.util.stream.Collectors.toList());

        List<TareaData> tareasTerminadas = todasLasTareas.stream()
                .filter(t -> t.getEstado().equals("TERMINADA"))
                .collect(java.util.stream.Collectors.toList());

        assertThat(tareasPendientes).hasSize(1);
        assertThat(tareasEnCurso).hasSize(1);
        assertThat(tareasTerminadas).hasSize(1);
        assertThat(tareasEnCurso.get(0).getId()).isEqualTo(tarea2.getId());
    }

    @Test
    public void testFiltrarTareasEquipoPorEstado() {
        // GIVEN
        Long equipoId = crearEquipoUsuasrio();

        // WHEN
        TareaData t1 = tareaService.nuevaTareaEquipo(equipoId, "Tarea 1", null);
        TareaData t2 = tareaService.nuevaTareaEquipo(equipoId, "Tarea 2", null);
        TareaData t3 = tareaService.nuevaTareaEquipo(equipoId, "Tarea 3", null);

        tareaService.cambiarEstadoTarea(t2.getId(), EstadoTarea.EN_CURSO);
        tareaService.cambiarEstadoTarea(t3.getId(), EstadoTarea.TERMINADA);

        // THEN
        List<TareaData> tareasEnCurso = tareaService.allTareasEquipo(equipoId).stream()
                .filter(t -> t.getEstado().equals("EN_CURSO"))
                .collect(java.util.stream.Collectors.toList());

        assertThat(tareasEnCurso).hasSize(1);
        assertThat(tareasEnCurso.get(0).getTitulo()).isEqualTo("Tarea 2");
    }

    @Test
    public void testTareasOrdenadas() {
        // GIVEN
        Long usuarioId = crearUsuarioBasico("orden@ua");

        // WHEN - Crear tareas con diferentes estados
        TareaData t1 = tareaService.nuevaTareaUsuario(usuarioId, "Tarea A", null);
        TareaData t2 = tareaService.nuevaTareaUsuario(usuarioId, "Tarea B", null);
        TareaData t3 = tareaService.nuevaTareaUsuario(usuarioId, "Tarea C", null);

        tareaService.cambiarEstadoTarea(t1.getId(), EstadoTarea.TERMINADA);
        tareaService.cambiarEstadoTarea(t2.getId(), EstadoTarea.EN_CURSO);
        // t3 queda PENDIENTE

        // THEN - Las tareas ordenadas deben tener primero PENDIENTE, luego EN_CURSO, luego TERMINADA
        List<TareaData> tareasOrdenadas = tareaService.allTareasUsuarioOrdenadas(usuarioId);

        assertThat(tareasOrdenadas).hasSize(3);
        assertThat(tareasOrdenadas.get(0).getEstado()).isIn("PENDIENTE", "EN_CURSO");
        assertThat(tareasOrdenadas.get(2).getEstado()).isEqualTo("TERMINADA");
    }

    @Test
    public void testCrearTareaProyectoConFechaLimite() {
        // GIVEN
        Long proyectoId = crearProyectoParaUsuasrio();
        LocalDate fecha = LocalDate.of(2025, 12, 31);

        // WHEN
        TareaData tarea = tareaService.nuevaTareaProyecto(proyectoId, "Tarea con fecha", fecha);

        // THEN
        assertThat(tarea.getFechaLimite()).isEqualTo(fecha);
        assertThat(tarea.getProyectoId()).isEqualTo(proyectoId);
    }

    @Test
    public void testCrearTareaEquipoConUsuarioAsignado() {
        // GIVEN
        Long u1 = crearUsuarioBasico("u1@ua");
        Long u2 = crearUsuarioBasico("u2@ua");
        EquipoData equipo = equipoService.crearEquipo("Equipo Test", u1);
        equipoService.añadirUsuarioAEquipo(equipo.getId(), u2);

        // WHEN
        TareaData tarea = tareaService.nuevaTareaEquipo(equipo.getId(), "Tarea asignada", null, u2);

        // THEN
        assertThat(tarea.getEquipoId()).isEqualTo(equipo.getId());
        assertThat(tarea.getUsuarioId()).isEqualTo(u2);
    }

    @Test
    public void testRevertirEstadoDeTerminadaAPendiente() {
        // GIVEN
        Long usuarioId = crearUsuarioBasico("revertir@ua");
        TareaData tarea = tareaService.nuevaTareaUsuario(usuarioId, "Tarea a revertir", null);
        tareaService.cambiarEstadoTarea(tarea.getId(), EstadoTarea.TERMINADA);

        // WHEN - Revertir a PENDIENTE
        tareaService.cambiarEstadoTarea(tarea.getId(), EstadoTarea.PENDIENTE);

        // THEN
        TareaData tareaActualizada = tareaService.findById(tarea.getId());
        assertThat(tareaActualizada.getEstado()).isEqualTo("PENDIENTE");
    }

}