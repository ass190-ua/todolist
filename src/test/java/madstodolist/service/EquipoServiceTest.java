package madstodolist.service;

import madstodolist.dto.EquipoData;
import madstodolist.dto.UsuarioData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Sql(scripts = "/clean-db.sql")
public class EquipoServiceTest {

    @Autowired
    EquipoService equipoService;

    @Autowired
    UsuarioService usuarioService;

    @Test
    public void crearRecuperarEquipo() {
        UsuarioData creador = new UsuarioData();
        creador.setEmail("creador@ua");
        creador.setPassword("123");
        creador = usuarioService.registrar(creador);

        EquipoData equipo = equipoService.crearEquipo("Proyecto 1", creador.getId());
        assertThat(equipo.getId()).isNotNull();

        EquipoData equipoBd = equipoService.recuperarEquipo(equipo.getId());
        assertThat(equipoBd).isNotNull();
        assertThat(equipoBd.getNombre()).isEqualTo("Proyecto 1");
        assertThat(equipoBd.getAdminUserId()).isEqualTo(creador.getId());
    }

    @Test
    public void listadoEquiposOrdenAlfabetico() {
        UsuarioData u = new UsuarioData(); u.setEmail("u@ua"); u.setPassword("x"); u = usuarioService.registrar(u);

        equipoService.crearEquipo("Proyecto BBB", u.getId());
        equipoService.crearEquipo("Proyecto AAA", u.getId());

        List<EquipoData> equipos = equipoService.findAllOrdenadoPorNombre();

        assertThat(equipos).hasSize(2);
        assertThat(equipos.get(0).getNombre()).isEqualTo("Proyecto AAA");
        assertThat(equipos.get(1).getNombre()).isEqualTo("Proyecto BBB");
    }

    @Test
    public void añadirUsuarioAEquipo() {
        // GIVEN: un usuario creador, otro a añadir y un equipo
        UsuarioData creador = new UsuarioData(); creador.setEmail("c@ua"); creador.setPassword("1"); creador = usuarioService.registrar(creador);
        UsuarioData usuario = new UsuarioData(); usuario.setEmail("user@ua"); usuario.setPassword("123"); usuario = usuarioService.registrar(usuario);

        EquipoData equipo = equipoService.crearEquipo("Proyecto 1", creador.getId());

        // WHEN: añadir usuario al equipo
        equipoService.añadirUsuarioAEquipo(equipo.getId(), usuario.getId());

        // THEN: el equipo tiene 2 usuarios (el creador + el usuario añadido)
        List<UsuarioData> usuarios = equipoService.usuariosEquipo(equipo.getId());
        assertThat(usuarios).hasSize(2);
        assertThat(usuarios).extracting(UsuarioData::getEmail).containsExactlyInAnyOrder("c@ua", "user@ua");
    }

    @Test
    public void recuperarEquiposDeUsuario() {
        UsuarioData u = new UsuarioData(); u.setEmail("user@ua"); u.setPassword("123"); u = usuarioService.registrar(u);
        UsuarioData creador = new UsuarioData(); creador.setEmail("c2@ua"); creador.setPassword("1"); creador = usuarioService.registrar(creador);

        EquipoData equipo1 = equipoService.crearEquipo("Proyecto 1", creador.getId());
        EquipoData equipo2 = equipoService.crearEquipo("Proyecto 2", creador.getId());
        equipoService.añadirUsuarioAEquipo(equipo1.getId(), u.getId());
        equipoService.añadirUsuarioAEquipo(equipo2.getId(), u.getId());

        List<EquipoData> equipos = equipoService.equiposUsuario(u.getId());

        assertThat(equipos).hasSize(2);
        assertThat(equipos).extracting(EquipoData::getNombre).containsExactlyInAnyOrder("Proyecto 1", "Proyecto 2");
    }

    @Test
    public void comprobarExcepciones() {
        UsuarioData creador = new UsuarioData(); creador.setEmail("c3@ua"); creador.setPassword("1"); creador = usuarioService.registrar(creador);

        // recuperar equipo inexistente
        assertThatThrownBy(() -> equipoService.recuperarEquipo(1L))
                .isInstanceOf(EquipoServiceException.class);

        // añadir usuario a equipo inexistente
        assertThatThrownBy(() -> equipoService.añadirUsuarioAEquipo(1L, 1L))
                .isInstanceOf(EquipoServiceException.class);

        // usuariosEquipo sobre equipo inexistente
        assertThatThrownBy(() -> equipoService.usuariosEquipo(1L))
                .isInstanceOf(EquipoServiceException.class);

        // equiposUsuario sobre usuario inexistente
        assertThatThrownBy(() -> equipoService.equiposUsuario(999L))
                .isInstanceOf(EquipoServiceException.class);

        // crear equipo y añadir usuario inexistente
        EquipoData equipo = equipoService.crearEquipo("Proyecto 1", creador.getId());
        assertThatThrownBy(() -> equipoService.añadirUsuarioAEquipo(equipo.getId(), 999L))
                .isInstanceOf(EquipoServiceException.class);
    }

    @Test
    public void quitarUsuarioDeEquipo() {
        UsuarioData u = new UsuarioData(); u.setEmail("user@ua"); u.setPassword("123"); u = usuarioService.registrar(u);
        UsuarioData creador = new UsuarioData(); creador.setEmail("c4@ua"); creador.setPassword("1"); creador = usuarioService.registrar(creador);

        EquipoData e = equipoService.crearEquipo("Proyecto X", creador.getId());
        equipoService.añadirUsuarioAEquipo(e.getId(), u.getId());

        // Ahora hay 2 usuarios: el creador y el usuario añadido
        assertThat(equipoService.usuariosEquipo(e.getId())).hasSize(2);

        equipoService.quitarUsuarioDeEquipo(e.getId(), u.getId());

        // Después de quitar al usuario, solo queda el creador
        assertThat(equipoService.usuariosEquipo(e.getId())).hasSize(1);
        assertThat(equipoService.usuariosEquipo(e.getId()).get(0).getEmail()).isEqualTo("c4@ua");
    }

    @Test
    public void quitarUsuarioDeEquipo_excepciones() {
        UsuarioData creador = new UsuarioData(); creador.setEmail("c5@ua"); creador.setPassword("1"); creador = usuarioService.registrar(creador);

        // equipo inexistente
        assertThatThrownBy(() -> equipoService.quitarUsuarioDeEquipo(999L, 1L))
                .isInstanceOf(EquipoServiceException.class);

        // usuario inexistente
        EquipoData e = equipoService.crearEquipo("Equipo Y", creador.getId());
        final Long equipoYId = e.getId();
        assertThatThrownBy(() -> equipoService.quitarUsuarioDeEquipo(equipoYId, 999L))
                .isInstanceOf(EquipoServiceException.class);

        // usuario no pertenece
        UsuarioData u = new UsuarioData();
        u.setEmail("otra@ua");
        u.setPassword("123");
        u = usuarioService.registrar(u);

        final Long usuarioId = u.getId();
        assertThatThrownBy(() -> equipoService.quitarUsuarioDeEquipo(equipoYId, usuarioId))
                .isInstanceOf(EquipoServiceException.class);
    }

    @Test
    public void crearEquipo_nombreVacio_lanzaExcepcion() {
        UsuarioData creador = new UsuarioData();
        creador.setEmail("c6@ua");
        creador.setPassword("1");
        creador = usuarioService.registrar(creador);
        final Long creadorId = creador.getId();

        assertThatThrownBy(() -> equipoService.crearEquipo("   ", creadorId))
                .isInstanceOf(EquipoServiceException.class);
    }

    @Test
    public void anadirUsuario_duplicado_lanzaExcepcion() {
        UsuarioData u = new UsuarioData(); u.setEmail("dup@ua"); u.setPassword("123"); u = usuarioService.registrar(u);
        UsuarioData creador = new UsuarioData(); creador.setEmail("c7@ua"); creador.setPassword("1"); creador = usuarioService.registrar(creador);
        EquipoData e = equipoService.crearEquipo("Dup Equipo", creador.getId());

        equipoService.añadirUsuarioAEquipo(e.getId(), u.getId());

        final Long equipoId = e.getId();
        final Long usuarioId = u.getId();

        assertThatThrownBy(() -> equipoService.añadirUsuarioAEquipo(equipoId, usuarioId))
                .isInstanceOf(EquipoServiceException.class);
    }

    @Test
    public void usuariosNoMiembros_devuelveSoloNoMiembros() {
        UsuarioData creador = new UsuarioData(); creador.setEmail("c8@ua"); creador.setPassword("1"); creador = usuarioService.registrar(creador);
        UsuarioData otro = new UsuarioData(); otro.setEmail("otro@ua"); otro.setPassword("1"); otro = usuarioService.registrar(otro);

        EquipoData e = equipoService.crearEquipo("E6", creador.getId());
        // creador ya es miembro; otro no
        List<UsuarioData> noMiembros = equipoService.usuariosNoMiembros(e.getId());
        assertThat(noMiembros).extracting(UsuarioData::getEmail).contains("otro@ua");
        assertThat(noMiembros).extracting(UsuarioData::getEmail).doesNotContain(creador.getEmail());
    }
}