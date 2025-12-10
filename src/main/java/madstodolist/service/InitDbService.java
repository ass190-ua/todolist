package madstodolist.service;

import madstodolist.model.*;
import madstodolist.repository.EquipoRepository;
import madstodolist.repository.ProyectoRepository;
import madstodolist.repository.TareaRepository;
import madstodolist.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile("dev")
public class InitDbService {

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private TareaRepository tareaRepository;
    @Autowired
    private EquipoRepository equipoRepository;
    @Autowired
    private ProyectoRepository proyectoRepository;

    @PostConstruct
    @Transactional
    public void initDatabase() {
        // ------------------------------------------------------------------
        // 1. USUARIOS
        // ------------------------------------------------------------------

        // Admin del sistema
        Usuario admin = new Usuario("admin@ua.es");
        admin.setNombre("Administrador");
        admin.setPassword("admin");
        admin.setAdmin(true);
        usuarioRepository.save(admin);

        // Usuario principal
        Usuario user = new Usuario("user@ua.es");
        user.setNombre("Usuario Principal");
        user.setPassword("1234");
        usuarioRepository.save(user);

        // Otros compañeros
        Usuario arturo = new Usuario("arturo@ua.es");
        arturo.setNombre("Arturo");
        arturo.setPassword("1234");
        usuarioRepository.save(arturo);

        Usuario esther = new Usuario("esther@ua.es");
        esther.setNombre("Esther");
        esther.setPassword("1234");
        usuarioRepository.save(esther);

        Usuario hugo = new Usuario("hugo@ua.es");
        hugo.setNombre("Hugo");
        hugo.setPassword("1234");
        usuarioRepository.save(hugo);

        // ------------------------------------------------------------------
        // 2. EQUIPOS
        // ------------------------------------------------------------------

        // Equipo A: Desarrollo (Admin: user)
        // Miembros: user, arturo, hugo, admin,
        Equipo equipoDev = new Equipo("Equipo Desarrollo");
        equipoDev.setAdminUserId(user.getId());
        equipoDev.addUsuario(user);
        equipoDev.addUsuario(arturo);
        equipoDev.addUsuario(admin);
        equipoDev.addUsuario(hugo);
        equipoRepository.save(equipoDev);

        // Equipo B: Marketing (Admin: esther)
        // Miembros: esther, user
        Equipo equipoMkt = new Equipo("Equipo Marketing");
        equipoMkt.setAdminUserId(esther.getId());
        equipoMkt.addUsuario(esther);
        equipoMkt.addUsuario(user);
        equipoRepository.save(equipoMkt);

        // Equipo C: Sistemas (Admin: hugo)
        // Miembros: hugo, arturo
        Equipo equipoSys = new Equipo("Equipo Sistemas");
        equipoSys.setAdminUserId(hugo.getId());
        equipoSys.addUsuario(hugo);
        equipoSys.addUsuario(arturo);
        equipoRepository.save(equipoSys);

        // ------------------------------------------------------------------
        // 3. PROYECTOS
        // ------------------------------------------------------------------

        // --- Proyectos de Desarrollo ---
        Proyecto pAppMovil = new Proyecto("App Móvil Android", equipoDev);
        pAppMovil.setDescripcion("Desarrollo de la versión 1.0 de la app nativa.");
        proyectoRepository.save(pAppMovil);

        Proyecto pWeb = new Proyecto("Web Corporativa", equipoDev);
        pWeb.setDescripcion("Rediseño del portal web con Spring Boot.");
        proyectoRepository.save(pWeb);

        // --- Proyectos de Marketing ---
        Proyecto pCampana = new Proyecto("Campaña Navidad 2024", equipoMkt);
        pCampana.setDescripcion("Estrategia de redes sociales y ads.");
        proyectoRepository.save(pCampana);

        // --- Proyectos de Sistemas ---
        Proyecto pMigracion = new Proyecto("Migración a la Nube", equipoSys);
        pMigracion.setDescripcion("Mover infraestructura on-premise a AWS.");
        proyectoRepository.save(pMigracion);

        // ------------------------------------------------------------------
        // 4. TAREAS
        // ------------------------------------------------------------------

        // --- Tareas del Proyecto 1 (App Móvil) ---
        crearTarea(user, pAppMovil, "Diseñar Mockups UI", EstadoTarea.TERMINADA);
        crearTarea(arturo, pAppMovil, "Configurar Firebase", EstadoTarea.TERMINADA);

        crearTarea(user, pAppMovil, "Implementar Login", EstadoTarea.EN_CURSO);
        crearTarea(hugo, pAppMovil, "Optimización de memoria", EstadoTarea.EN_CURSO); // Tarea para Hugo

        crearTarea(arturo, pAppMovil, "Pantalla de Perfil", EstadoTarea.PENDIENTE);
        crearTarea(user, pAppMovil, "Integrar API de Mapas", EstadoTarea.PENDIENTE);
        crearTarea(hugo, pAppMovil, "Tests Unitarios", EstadoTarea.PENDIENTE); // Tarea para Hugo

        // --- Tareas del Proyecto 2 (Web) ---
        crearTarea(user, pWeb, "Maquetar Home", EstadoTarea.PENDIENTE);
        crearTarea(arturo, pWeb, "Desplegar en AWS", EstadoTarea.PENDIENTE);

        // --- Tareas del Proyecto Sistemas (Migración) ---
        crearTarea(hugo, pMigracion, "Configurar VPC", EstadoTarea.EN_CURSO);
        crearTarea(arturo, pMigracion, "Crear base de datos RDS", EstadoTarea.PENDIENTE);

        // --- Tareas Personales (SIN PROYECTO) ---
        crearTareaPersonal(user, "Comprar leche", EstadoTarea.PENDIENTE);
        crearTareaPersonal(user, "Llamar al dentista", EstadoTarea.TERMINADA);
        crearTareaPersonal(arturo, "Renovar DNI", EstadoTarea.PENDIENTE);
        crearTareaPersonal(hugo, "Estudiar certificación Java", EstadoTarea.PENDIENTE); // Personal de Hugo
    }

    // Helper para tareas de proyecto
    private void crearTarea(Usuario usuario, Proyecto proyecto, String titulo, EstadoTarea estado) {
        Tarea t = new Tarea(usuario, titulo);
        t.setProyecto(proyecto);
        t.setEstado(estado);
        tareaRepository.save(t);
    }

    // Helper para tareas personales
    private void crearTareaPersonal(Usuario usuario, String titulo, EstadoTarea estado) {
        Tarea t = new Tarea(usuario, titulo);
        t.setEstado(estado);
        // proyecto es null por defecto
        tareaRepository.save(t);
    }
}
