package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.TareaData;
import madstodolist.dto.UsuarioData;
import madstodolist.service.TareaService;
import madstodolist.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/clean-db.sql")
public class TareaWebTest {

    @Autowired
    private MockMvc mockMvc;

    // Declaramos los servicios como Autowired
    @Autowired
    private TareaService tareaService;

    @Autowired
    private UsuarioService usuarioService;

    // Moqueamos el managerUserSession para poder moquear el usuario logeado
    @MockBean
    private ManagerUserSession managerUserSession;

    // Método para inicializar los datos de prueba en la BD
    // Devuelve un mapa con los identificadores del usuario y de la primera tarea añadida

    Map<String, Long> addUsuarioTareasBD() {
        // Añadimos un usuario a la base de datos
        UsuarioData usuario = new UsuarioData();
        usuario.setEmail("user@ua");
        usuario.setPassword("123");
        usuario = usuarioService.registrar(usuario);

        // Y añadimos dos tareas asociadas a ese usuario
        TareaData tarea1 = tareaService.nuevaTareaUsuario(usuario.getId(), "Lavar coche", null);
        tareaService.nuevaTareaUsuario(usuario.getId(), "Renovar DNI", null);

        // Devolvemos los ids del usuario y de la primera tarea añadida
        Map<String, Long> ids = new HashMap<>();
        ids.put("usuarioId", usuario.getId());
        ids.put("tareaId", tarea1.getId());
        return ids;

    }

    @Test
    public void listaTareas() throws Exception {
        // GIVEN
        // Un usuario con dos tareas en la BD
        Long usuarioId = addUsuarioTareasBD().get("usuarioId");

        // Moqueamos el método usuarioLogeado para que devuelva el usuario 1L,
        // el mismo que se está usando en la petición. De esta forma evitamos
        // que salte la excepción de que el usuario que está haciendo la
        // petición no está logeado.
        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        // WHEN, THEN
        // se realiza la petición GET al listado de tareas del usuario,
        // el HTML devuelto contiene las descripciones de sus tareas.

        String url = "/usuarios/" + usuarioId.toString() + "/tareas";

        this.mockMvc.perform(get(url))
                .andExpect((content().string(allOf(
                        containsString("Lavar coche"),
                        containsString("Renovar DNI")
                ))));
    }

    @Test
    public void getNuevaTareaDevuelveForm() throws Exception {
        // GIVEN
        // Un usuario con dos tareas en la BD
        Long usuarioId = addUsuarioTareasBD().get("usuarioId");

        // Ver el comentario en el primer test
        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        // WHEN, THEN
        // si ejecutamos una petición GET para crear una nueva tarea de un usuario,
        // el HTML resultante contiene un formulario y la ruta con
        // la acción para crear la nueva tarea.

        String urlPeticion = "/usuarios/" + usuarioId.toString() + "/tareas/nueva";
        String urlAction = "action=\"/usuarios/" + usuarioId.toString() + "/tareas/nueva\"";

        this.mockMvc.perform(get(urlPeticion))
                .andExpect((content().string(allOf(
                        containsString("form method=\"post\""),
                        containsString(urlAction)
                ))));
    }

    @Test
    public void postNuevaTareaDevuelveRedirectYAñadeTarea() throws Exception {
        // GIVEN
        // Un usuario con dos tareas en la BD
        Long usuarioId = addUsuarioTareasBD().get("usuarioId");

        // Ver el comentario en el primer test
        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        // WHEN, THEN
        // realizamos la petición POST para añadir una nueva tarea,
        // el estado HTTP que se devuelve es un REDIRECT al listado
        // de tareas.

        String urlPost = "/usuarios/" + usuarioId.toString() + "/tareas/nueva";
        String urlRedirect = "/usuarios/" + usuarioId.toString() + "/tareas";

        this.mockMvc.perform(post(urlPost)
                        .param("titulo", "Estudiar examen MADS"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(urlRedirect));

        // y si después consultamos el listado de tareas con una petición
        // GET el HTML contiene la tarea añadida.

        this.mockMvc.perform(get(urlRedirect))
                .andExpect((content().string(containsString("Estudiar examen MADS"))));
    }

    @Test
    public void deleteTareaDevuelveOKyBorraTarea() throws Exception {
        // GIVEN
        // Un usuario con dos tareas en la BD
        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");
        Long tareaLavarCocheId = ids.get("tareaId");

        // Ver el comentario en el primer test
        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        // WHEN, THEN
        // realizamos la petición DELETE para borrar una tarea,
        // se devuelve el estado HTTP que se devuelve es OK,

        String urlDelete = "/tareas/" + tareaLavarCocheId.toString();

        this.mockMvc.perform(delete(urlDelete))
                .andExpect(status().isOk());

        // y cuando se pide un listado de tareas del usuario, la tarea borrada ya no aparece.

        String urlListado = "/usuarios/" + usuarioId + "/tareas";

        this.mockMvc.perform(get(urlListado))
                .andExpect(content().string(
                        allOf(not(containsString("Lavar coche")),
                                containsString("Renovar DNI"))));
    }

    @Test
    public void editarTareaActualizaLaTarea() throws Exception {
        // GIVEN
        // Un usuario con dos tareas en la BD
        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");
        Long tareaLavarCocheId = ids.get("tareaId");

        // Ver el comentario en el primer test
        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        // WHEN, THEN
        // realizamos una petición POST al endpoint para editar una tarea

        String urlEditar = "/tareas/" + tareaLavarCocheId + "/editar";
        String urlRedirect = "/usuarios/" + usuarioId + "/tareas";

        this.mockMvc.perform(post(urlEditar)
                        .param("titulo", "Limpiar cristales coche"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(urlRedirect));

        // Y si realizamos un listado de las tareas del usuario
        // ha cambiado el título de la tarea modificada

        String urlListado = "/usuarios/" + usuarioId + "/tareas";

        this.mockMvc.perform(get(urlListado))
                .andExpect(content().string(containsString("Limpiar cristales coche")));
    }

    @Test
    public void terminarTareaActualizaEstado() throws Exception {
        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");
        Long tareaId = ids.get("tareaId");

        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        String url = "/tareas/" + tareaId + "/terminar";

        this.mockMvc.perform(post(url))
                .andExpect(status().is3xxRedirection());

        String listado = "/usuarios/" + usuarioId + "/tareas";

        this.mockMvc.perform(get(listado))
                .andExpect(content().string(containsString("Terminada")));
    }

    @Test
    public void editarTareaActualizaFecha() throws Exception {
        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");
        Long tareaId = ids.get("tareaId");

        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        String urlEditar = "/tareas/" + tareaId + "/editar";
        String urlRedirect = "/usuarios/" + usuarioId + "/tareas";

        this.mockMvc.perform(post(urlEditar)
                        .param("titulo", "Nueva tarea con fecha")
                        .param("fechaLimite", "2025-02-01"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(urlRedirect));

        this.mockMvc.perform(get(urlRedirect))
                .andExpect(content().string(containsString("01/02/2025")));
    }

    @Test
    public void editarTareaEliminaFecha() throws Exception {
        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");
        Long tareaId = ids.get("tareaId");

        // Primero asignamos una fecha
        tareaService.modificaTarea(tareaId, "Título", LocalDate.of(2025, 1, 1));

        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        String urlEditar = "/tareas/" + tareaId + "/editar";

        // Enviar fecha vacía desde HTML la convierte en null
        this.mockMvc.perform(post(urlEditar)
                        .param("titulo", "Título")
                        .param("fechaLimite", ""))
                .andExpect(status().is3xxRedirection());

        String urlListado = "/usuarios/" + usuarioId + "/tareas";

        this.mockMvc.perform(get(urlListado))
                .andExpect(content().string(containsString("—")));
    }

    @Test
    public void postNuevaTareaConFecha() throws Exception {
        Long usuarioId = addUsuarioTareasBD().get("usuarioId");

        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        String urlPost = "/usuarios/" + usuarioId + "/tareas/nueva";
        String urlRedirect = "/usuarios/" + usuarioId + "/tareas";

        this.mockMvc.perform(post(urlPost)
                        .param("titulo", "Tarea con fecha")
                        .param("fechaLimite", "2025-03-15"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(urlRedirect));

        this.mockMvc.perform(get(urlRedirect))
                .andExpect(content().string(containsString("15/03/2025")));
    }

    @Test
    public void getNuevaTareaIncluyeCampoFecha() throws Exception {
        Long usuarioId = addUsuarioTareasBD().get("usuarioId");

        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        String urlPeticion = "/usuarios/" + usuarioId + "/tareas/nueva";

        this.mockMvc.perform(get(urlPeticion))
                .andExpect(content().string(containsString("fechaLimite")))
                .andExpect(content().string(containsString("type=\"date\"")));
    }

    @Test
    public void formularioNuevaTareaIncluyeEstadoEnCurso() throws Exception {
        Long usuarioId = addUsuarioTareasBD().get("usuarioId");

        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        String urlPeticion = "/usuarios/" + usuarioId + "/tareas/nueva";

        this.mockMvc.perform(get(urlPeticion))
                .andExpect(content().string(containsString("EN_CURSO")))
                .andExpect(content().string(containsString("En Curso")))
                .andExpect(content().string(containsString("PENDIENTE")))
                .andExpect(content().string(containsString("TERMINADA")));
    }

    @Test
    public void crearTareaConEstadoEnCurso() throws Exception {
        Long usuarioId = addUsuarioTareasBD().get("usuarioId");

        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        String urlPost = "/usuarios/" + usuarioId + "/tareas/nueva";
        String urlRedirect = "/usuarios/" + usuarioId + "/tareas";

        this.mockMvc.perform(post(urlPost)
                        .param("titulo", "Tarea en progreso")
                        .param("estado", "EN_CURSO"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(urlRedirect));

        this.mockMvc.perform(get(urlRedirect))
                .andExpect(content().string(containsString("Tarea en progreso")))
                .andExpect(content().string(containsString("En Curso")));
    }

    @Test
    public void editarTareaCambiaEstadoAEnCurso() throws Exception {
        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");
        Long tareaId = ids.get("tareaId");

        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        String urlEditar = "/tareas/" + tareaId + "/editar";
        String urlRedirect = "/usuarios/" + usuarioId + "/tareas";

        this.mockMvc.perform(post(urlEditar)
                        .param("titulo", "Lavar coche")
                        .param("estado", "EN_CURSO"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(urlRedirect));

        this.mockMvc.perform(get(urlRedirect))
                .andExpect(content().string(containsString("En Curso")));
    }

    @Test
    public void formularioEditarTareaIncluyeEstadoEnCurso() throws Exception {
        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");
        Long tareaId = ids.get("tareaId");

        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        String urlEditar = "/tareas/" + tareaId + "/editar";

        this.mockMvc.perform(get(urlEditar))
                .andExpect(content().string(containsString("EN_CURSO")))
                .andExpect(content().string(containsString("En Curso")));
    }

    @Test
    public void verDetallesDeTarea() throws Exception {
        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");
        Long tareaId = ids.get("tareaId");

        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        String urlDetalles = "/tareas/" + tareaId;

        this.mockMvc.perform(get(urlDetalles))
                .andExpect(status().isOk())
                .andExpect(view().name("verTarea"))
                .andExpect(content().string(containsString("Lavar coche")))
                .andExpect(content().string(containsString("DETALLES")));
    }

    @Test
    public void verDetallesMuestraEstadoPendiente() throws Exception {
        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");
        Long tareaId = ids.get("tareaId");

        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        this.mockMvc.perform(get("/tareas/" + tareaId))
                .andExpect(content().string(containsString("Pendiente")))
                .andExpect(content().string(containsString("badge-warning")));
    }

    @Test
    public void verDetallesMuestraEstadoEnCurso() throws Exception {
        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");
        Long tareaId = ids.get("tareaId");

        tareaService.cambiarEstadoTarea(tareaId, madstodolist.model.EstadoTarea.EN_CURSO);

        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        this.mockMvc.perform(get("/tareas/" + tareaId))
                .andExpect(content().string(containsString("En Curso")))
                .andExpect(content().string(containsString("badge-info")));
    }

    @Test
    public void verDetallesMuestraEstadoTerminada() throws Exception {
        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");
        Long tareaId = ids.get("tareaId");

        tareaService.cambiarEstadoTarea(tareaId, madstodolist.model.EstadoTarea.TERMINADA);

        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        this.mockMvc.perform(get("/tareas/" + tareaId))
                .andExpect(content().string(containsString("Terminada")))
                .andExpect(content().string(containsString("badge-success")));
    }

    @Test
    public void verDetallesMuestraFechaLimite() throws Exception {
        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");
        Long tareaId = ids.get("tareaId");

        tareaService.modificarFechaLimite(tareaId, LocalDate.of(2025, 12, 25));

        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        this.mockMvc.perform(get("/tareas/" + tareaId))
                .andExpect(content().string(containsString("25/12/2025")));
    }

    @Test
    public void verDetallesIncluyeBotonesDeAccion() throws Exception {
        Map<String, Long> ids = addUsuarioTareasBD();
        Long usuarioId = ids.get("usuarioId");
        Long tareaId = ids.get("tareaId");

        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        this.mockMvc.perform(get("/tareas/" + tareaId))
                .andExpect(content().string(containsString("Editar Tarea")))
                .andExpect(content().string(containsString("Eliminar Tarea")))
                .andExpect(content().string(containsString("Volver a Mis Tareas")));
    }

    @Test
    public void listaTareasMuestraEstadoEnCurso() throws Exception {
        Long usuarioId = addUsuarioTareasBD().get("usuarioId");

        TareaData tarea = tareaService.nuevaTareaUsuario(usuarioId, "Tarea en progreso", null);
        tareaService.cambiarEstadoTarea(tarea.getId(), madstodolist.model.EstadoTarea.EN_CURSO);

        when(managerUserSession.usuarioLogeado()).thenReturn(usuarioId);

        String urlListado = "/usuarios/" + usuarioId + "/tareas";

        this.mockMvc.perform(get(urlListado))
                .andExpect(content().string(containsString("Tarea en progreso")))
                .andExpect(content().string(containsString("En Curso")));
    }
}
