package madstodolist.repository;

import madstodolist.model.Equipo;
import madstodolist.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Sql(scripts = "/clean-db.sql")
public class EquipoTest {

    @Autowired
    private EquipoRepository equipoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    public void crearEquipo() {
        Equipo equipo = new Equipo("Proyecto P1");
        assertThat(equipo.getNombre()).isEqualTo("Proyecto P1");
    }

    @Test
    @Transactional
    public void grabarYBuscarEquipo() {
        Equipo equipo = new Equipo();
        equipo.setNombre("Proyecto P1");

        equipoRepository.save(equipo);

        Long equipoId = equipo.getId();
        assertThat(equipoId).isNotNull();

        Equipo equipoDB = equipoRepository.findById(equipoId).orElse(null);
        assertThat(equipoDB).isNotNull();
        assertThat(equipoDB.getNombre()).isEqualTo("Proyecto P1");
    }

    @Test
    public void igualdadPorNombre() {
        Equipo e1 = new Equipo("Proyecto P1");
        Equipo e2 = new Equipo("Proyecto P1");
        Equipo e3 = new Equipo("Proyecto P2");

        assertThat(e1).isEqualTo(e2);
        assertThat(e1).isNotEqualTo(e3);
        assertThat(e1.hashCode()).isEqualTo(e2.hashCode());
    }

    @Test
    @Transactional
    public void relacionEquipoUsuarios() {
        Usuario u1 = new Usuario();
        u1.setEmail("ana@ua.es");
        u1.setPassword("123456");

        Usuario u2 = new Usuario();
        u2.setEmail("luis@ua.es");
        u2.setPassword("123456");

        usuarioRepository.save(u1);
        usuarioRepository.save(u2);

        Equipo equipo = new Equipo();
        equipo.setNombre("Proyecto P1");
        equipo.addUsuario(u1);
        equipo.addUsuario(u2);

        equipoRepository.save(equipo);

        Long id = equipo.getId();
        assertThat(id).isNotNull();

        Equipo equipoDB = equipoRepository.findById(id).orElse(null);
        assertThat(equipoDB).isNotNull();
        assertThat(equipoDB.getUsuarios()).hasSize(2);
        assertThat(
                equipoDB.getUsuarios()
                        .stream()
                        .map(Usuario::getEmail)
                        .collect(Collectors.toList())
        ).containsExactlyInAnyOrder("ana@ua.es", "luis@ua.es");
    }

    @Test
    @Transactional
    public void comprobarFindAll() {
        equipoRepository.save(new Equipo("Proyecto 2"));
        equipoRepository.save(new Equipo("Proyecto 3"));

        List<Equipo> equipos = equipoRepository.findAll();

        assertThat(equipos).hasSize(2);
    }

    @Test
    @Transactional
    public void persistirAdminUserId() {
        Usuario u = new Usuario();
        u.setEmail("admin@ua.es");
        u.setPassword("123");
        usuarioRepository.save(u);

        Equipo equipo = new Equipo("ConAdmin");
        equipo.setAdminUserId(u.getId());
        equipo.addUsuario(u);

        equipoRepository.save(equipo);

        Equipo equipoDB = equipoRepository.findById(equipo.getId()).orElse(null);
        assertThat(equipoDB).isNotNull();
        assertThat(equipoDB.getAdminUserId()).isEqualTo(u.getId());
    }
}
