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

import java.time.LocalDate;

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

        // Nuevos usuarios
        Usuario laura = new Usuario("laura@ua.es");
        laura.setNombre("Laura");
        laura.setPassword("1234");
        usuarioRepository.save(laura);

        Usuario carlos = new Usuario("carlos@ua.es");
        carlos.setNombre("Carlos");
        carlos.setPassword("1234");
        usuarioRepository.save(carlos);

        // ------------------------------------------------------------------
        // 2. EQUIPOS
        // ------------------------------------------------------------------

        // Equipo A: Desarrollo (Admin: user)
        Equipo equipoDev = new Equipo("Equipo Desarrollo");
        equipoDev.setAdminUserId(user.getId());
        equipoDev.addUsuario(user);
        equipoDev.addUsuario(arturo);
        equipoDev.addUsuario(admin);
        equipoDev.addUsuario(hugo);
        equipoRepository.save(equipoDev);

        // Equipo B: Marketing (Admin: esther)
        Equipo equipoMkt = new Equipo("Equipo Marketing");
        equipoMkt.setAdminUserId(esther.getId());
        equipoMkt.addUsuario(esther);
        equipoMkt.addUsuario(user);
        equipoMkt.addUsuario(laura);
        equipoRepository.save(equipoMkt);

        // Equipo C: Sistemas (Admin: hugo)
        Equipo equipoSys = new Equipo("Equipo Sistemas");
        equipoSys.setAdminUserId(hugo.getId());
        equipoSys.addUsuario(hugo);
        equipoSys.addUsuario(arturo);
        equipoSys.addUsuario(carlos);
        equipoRepository.save(equipoSys);

        // Equipo D: Diseño (Admin: laura)
        Equipo equipoDiseno = new Equipo("Equipo Diseño");
        equipoDiseno.setAdminUserId(laura.getId());
        equipoDiseno.addUsuario(laura);
        equipoDiseno.addUsuario(user);
        equipoDiseno.addUsuario(esther);
        equipoRepository.save(equipoDiseno);

        // Equipo E: Calidad (Admin: carlos)
        Equipo equipoQA = new Equipo("Equipo Calidad");
        equipoQA.setAdminUserId(carlos.getId());
        equipoQA.addUsuario(carlos);
        equipoQA.addUsuario(user);
        equipoQA.addUsuario(arturo);
        equipoRepository.save(equipoQA);

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

        Proyecto pAPI = new Proyecto("API REST v2", equipoDev);
        pAPI.setDescripcion("Nueva versión de la API con mejoras de rendimiento.");
        proyectoRepository.save(pAPI);

        // --- Proyectos de Marketing ---
        Proyecto pCampana = new Proyecto("Campaña Navidad 2024", equipoMkt);
        pCampana.setDescripcion("Estrategia de redes sociales y ads.");
        proyectoRepository.save(pCampana);

        Proyecto pNewsletter = new Proyecto("Newsletter Mensual", equipoMkt);
        pNewsletter.setDescripcion("Boletín informativo para clientes.");
        proyectoRepository.save(pNewsletter);

        // --- Proyectos de Sistemas ---
        Proyecto pMigracion = new Proyecto("Migración a la Nube", equipoSys);
        pMigracion.setDescripcion("Mover infraestructura on-premise a AWS.");
        proyectoRepository.save(pMigracion);

        Proyecto pSeguridad = new Proyecto("Auditoría de Seguridad", equipoSys);
        pSeguridad.setDescripcion("Revisión completa de seguridad del sistema.");
        proyectoRepository.save(pSeguridad);

        // --- Proyectos de Diseño ---
        Proyecto pBranding = new Proyecto("Renovación de Branding", equipoDiseno);
        pBranding.setDescripcion("Actualización de la identidad visual corporativa.");
        proyectoRepository.save(pBranding);

        // --- Proyectos de Calidad ---
        Proyecto pTestAuto = new Proyecto("Testing Automatizado", equipoQA);
        pTestAuto.setDescripcion("Implementación de tests automatizados E2E.");
        proyectoRepository.save(pTestAuto);

        // ------------------------------------------------------------------
        // 4. TAREAS CON FECHAS
        // ------------------------------------------------------------------

        LocalDate hoy = LocalDate.now();
        LocalDate ayer = hoy.minusDays(1);
        LocalDate manana = hoy.plusDays(1);
        LocalDate proximaSemana = hoy.plusWeeks(1);
        LocalDate proximoMes = hoy.plusMonths(1);

        // --- Tareas del Proyecto App Móvil ---
        crearTarea(user, pAppMovil, "Diseñar Mockups UI", EstadoTarea.TERMINADA, ayer.minusDays(5));
        crearTarea(arturo, pAppMovil, "Configurar Firebase", EstadoTarea.TERMINADA, ayer.minusDays(3));
        crearTarea(user, pAppMovil, "Implementar Login", EstadoTarea.EN_CURSO, hoy);
        crearTarea(hugo, pAppMovil, "Optimización de memoria", EstadoTarea.EN_CURSO, hoy.plusDays(1));
        crearTarea(arturo, pAppMovil, "Pantalla de Perfil", EstadoTarea.PENDIENTE, proximaSemana);
        crearTarea(user, pAppMovil, "Integrar API de Mapas", EstadoTarea.PENDIENTE, proximaSemana.plusDays(2));
        crearTarea(hugo, pAppMovil, "Tests Unitarios", EstadoTarea.PENDIENTE, proximaSemana.plusDays(4));
        crearTarea(arturo, pAppMovil, "Notificaciones Push", EstadoTarea.PENDIENTE, proximoMes);

        // --- Tareas del Proyecto Web Corporativa ---
        crearTarea(user, pWeb, "Maquetar Home", EstadoTarea.EN_CURSO, hoy.plusDays(2));
        crearTarea(arturo, pWeb, "Desplegar en AWS", EstadoTarea.PENDIENTE, proximaSemana);
        crearTarea(hugo, pWeb, "Configurar SSL", EstadoTarea.PENDIENTE, proximaSemana.plusDays(1));
        crearTarea(user, pWeb, "Sección de contacto", EstadoTarea.PENDIENTE, proximaSemana.plusDays(5));
        crearTarea(arturo, pWeb, "Blog corporativo", EstadoTarea.PENDIENTE, proximoMes.plusDays(3));

        // --- Tareas del Proyecto API REST v2 ---
        crearTarea(user, pAPI, "Definir endpoints", EstadoTarea.TERMINADA, ayer.minusDays(7));
        crearTarea(arturo, pAPI, "Implementar autenticación JWT", EstadoTarea.EN_CURSO, hoy.plusDays(1));
        crearTarea(hugo, pAPI, "Documentación Swagger", EstadoTarea.PENDIENTE, proximaSemana.plusDays(2));
        crearTarea(user, pAPI, "Testing de carga", EstadoTarea.PENDIENTE, proximoMes);
        crearTarea(arturo, pAPI, "Optimización de queries", EstadoTarea.PENDIENTE, proximoMes.plusDays(5));

        // --- Tareas del Proyecto Campaña Navidad ---
        crearTarea(esther, pCampana, "Diseñar banners", EstadoTarea.TERMINADA, ayer.minusDays(10));
        crearTarea(laura, pCampana, "Redactar textos", EstadoTarea.TERMINADA, ayer.minusDays(6));
        crearTarea(user, pCampana, "Configurar Google Ads", EstadoTarea.EN_CURSO, hoy.plusDays(1));
        crearTarea(esther, pCampana, "Publicar en redes sociales", EstadoTarea.PENDIENTE, hoy.plusDays(3));
        crearTarea(laura, pCampana, "Analizar resultados", EstadoTarea.PENDIENTE, proximaSemana.plusDays(10));

        // --- Tareas del Proyecto Newsletter ---
        crearTarea(esther, pNewsletter, "Diseñar plantilla HTML", EstadoTarea.EN_CURSO, hoy.plusDays(2));
        crearTarea(laura, pNewsletter, "Seleccionar contenido", EstadoTarea.PENDIENTE, proximaSemana);
        crearTarea(user, pNewsletter, "Integrar con MailChimp", EstadoTarea.PENDIENTE, proximaSemana.plusDays(3));

        // --- Tareas del Proyecto Migración a la Nube ---
        crearTarea(hugo, pMigracion, "Configurar VPC", EstadoTarea.EN_CURSO, hoy);
        crearTarea(arturo, pMigracion, "Crear base de datos RDS", EstadoTarea.PENDIENTE, proximaSemana);
        crearTarea(carlos, pMigracion, "Migrar servidores", EstadoTarea.PENDIENTE, proximaSemana.plusDays(5));
        crearTarea(hugo, pMigracion, "Configurar balanceador", EstadoTarea.PENDIENTE, proximoMes);

        // --- Tareas del Proyecto Auditoría de Seguridad ---
        crearTarea(carlos, pSeguridad, "Escaneo de vulnerabilidades", EstadoTarea.TERMINADA, ayer.minusDays(4));
        crearTarea(hugo, pSeguridad, "Actualizar certificados", EstadoTarea.EN_CURSO, hoy);
        crearTarea(arturo, pSeguridad, "Implementar 2FA", EstadoTarea.PENDIENTE, proximaSemana.plusDays(3));
        crearTarea(carlos, pSeguridad, "Auditoría de logs", EstadoTarea.PENDIENTE, proximoMes);

        // --- Tareas del Proyecto Branding ---
        crearTarea(laura, pBranding, "Investigación de mercado", EstadoTarea.TERMINADA, ayer.minusDays(12));
        crearTarea(esther, pBranding, "Propuesta de logo", EstadoTarea.TERMINADA, ayer.minusDays(8));
        crearTarea(laura, pBranding, "Paleta de colores", EstadoTarea.EN_CURSO, hoy.plusDays(1));
        crearTarea(user, pBranding, "Manual de identidad", EstadoTarea.PENDIENTE, proximaSemana.plusDays(7));
        crearTarea(esther, pBranding, "Mockups aplicados", EstadoTarea.PENDIENTE, proximoMes.plusDays(4));

        // --- Tareas del Proyecto Testing Automatizado ---
        crearTarea(carlos, pTestAuto, "Configurar Selenium", EstadoTarea.TERMINADA, ayer.minusDays(2));
        crearTarea(user, pTestAuto, "Escribir tests E2E", EstadoTarea.EN_CURSO, hoy.plusDays(2));
        crearTarea(arturo, pTestAuto, "Integrar con CI/CD", EstadoTarea.PENDIENTE, proximaSemana.plusDays(4));
        crearTarea(carlos, pTestAuto, "Reports automáticos", EstadoTarea.PENDIENTE, proximoMes.plusDays(2));

        // ------------------------------------------------------------------
        // 5. TAREAS PERSONALES (sin proyecto ni equipo)
        // ------------------------------------------------------------------

        // Usuario principal
        crearTareaPersonal(user, "Comprar leche", EstadoTarea.PENDIENTE, hoy);
        crearTareaPersonal(user, "Llamar al dentista", EstadoTarea.TERMINADA, ayer);
        crearTareaPersonal(user, "Revisar correos", EstadoTarea.TERMINADA, ayer.minusDays(1));
        crearTareaPersonal(user, "Preparar presentación", EstadoTarea.EN_CURSO, manana);
        crearTareaPersonal(user, "Renovar seguro coche", EstadoTarea.PENDIENTE, proximaSemana);
        crearTareaPersonal(user, "Comprar regalo cumpleaños", EstadoTarea.PENDIENTE, hoy.plusDays(5));
        crearTareaPersonal(user, "Hacer ejercicio", EstadoTarea.PENDIENTE, hoy.plusDays(1));
        crearTareaPersonal(user, "Estudiar certificación Java", EstadoTarea.EN_CURSO, proximaSemana.plusDays(2));

        // Arturo
        crearTareaPersonal(arturo, "Renovar DNI", EstadoTarea.PENDIENTE, proximaSemana.plusDays(1));
        crearTareaPersonal(arturo, "Pagar facturas", EstadoTarea.TERMINADA, ayer.minusDays(2));
        crearTareaPersonal(arturo, "Cita médico", EstadoTarea.PENDIENTE, hoy.plusDays(6));
        crearTareaPersonal(arturo, "Comprar regalos Navidad", EstadoTarea.PENDIENTE, proximoMes);

        // Hugo
        crearTareaPersonal(hugo, "Estudiar certificación Java", EstadoTarea.EN_CURSO, proximaSemana);
        crearTareaPersonal(hugo, "Leer libro técnico", EstadoTarea.PENDIENTE, proximoMes);
        crearTareaPersonal(hugo, "Organizar escritorio", EstadoTarea.TERMINADA, ayer.minusDays(3));
        crearTareaPersonal(hugo, "Actualizar LinkedIn", EstadoTarea.PENDIENTE, hoy.plusDays(4));

        // Esther
        crearTareaPersonal(esther, "Planificar vacaciones", EstadoTarea.EN_CURSO, hoy.plusDays(2));
        crearTareaPersonal(esther, "Comprar plantas", EstadoTarea.PENDIENTE, proximaSemana);
        crearTareaPersonal(esther, "Clases de yoga", EstadoTarea.TERMINADA, ayer);

        // Laura
        crearTareaPersonal(laura, "Curso de fotografía", EstadoTarea.EN_CURSO, hoy.plusDays(1));
        crearTareaPersonal(laura, "Actualizar portfolio", EstadoTarea.PENDIENTE, proximoMes);
        crearTareaPersonal(laura, "Ordenar fotos", EstadoTarea.PENDIENTE, proximaSemana.plusDays(3));

        // Carlos
        crearTareaPersonal(carlos, "Mantenimiento coche", EstadoTarea.PENDIENTE, hoy.plusDays(8));
        crearTareaPersonal(carlos, "Aprender Docker", EstadoTarea.EN_CURSO, proximaSemana.plusDays(1));
        crearTareaPersonal(carlos, "Preparar CV", EstadoTarea.PENDIENTE, proximoMes.plusDays(3));

        // ------------------------------------------------------------------
        // 6. TAREAS DE EQUIPO (sin proyecto específico)
        // ------------------------------------------------------------------

        crearTareaEquipo(equipoDev, user, "Reunión de retrospectiva", EstadoTarea.PENDIENTE, hoy.plusDays(3));
        crearTareaEquipo(equipoDev, arturo, "Actualizar documentación técnica", EstadoTarea.EN_CURSO, hoy.plusDays(1));
        crearTareaEquipo(equipoDev, hugo, "Code review semanal", EstadoTarea.PENDIENTE, proximaSemana);

        crearTareaEquipo(equipoMkt, esther, "Análisis de competencia", EstadoTarea.PENDIENTE, proximaSemana);
        crearTareaEquipo(equipoMkt, laura, "Planificar contenido del mes", EstadoTarea.EN_CURSO, hoy.plusDays(2));

        crearTareaEquipo(equipoSys, hugo, "Backup semanal", EstadoTarea.TERMINADA, ayer);
        crearTareaEquipo(equipoSys, carlos, "Monitoreo de servidores", EstadoTarea.PENDIENTE, hoy.plusDays(4));

        crearTareaEquipo(equipoDiseno, laura, "Sesión de brainstorming", EstadoTarea.PENDIENTE, hoy.plusDays(5));
        crearTareaEquipo(equipoDiseno, esther, "Revisar guía de estilo", EstadoTarea.PENDIENTE, proximaSemana.plusDays(2));

        crearTareaEquipo(equipoQA, carlos, "Reunión de planificación", EstadoTarea.PENDIENTE, hoy.plusDays(6));
        crearTareaEquipo(equipoQA, arturo, "Revisar casos de prueba", EstadoTarea.EN_CURSO, hoy.plusDays(3));
    }

    // Helper para tareas de proyecto
    private void crearTarea(Usuario usuario, Proyecto proyecto, String titulo, EstadoTarea estado, LocalDate fecha) {
        Tarea t = new Tarea(usuario, titulo, fecha);
        t.setProyecto(proyecto);
        t.setEstado(estado);
        tareaRepository.save(t);
    }

    // Helper para tareas personales
    private void crearTareaPersonal(Usuario usuario, String titulo, EstadoTarea estado, LocalDate fecha) {
        Tarea t = new Tarea(usuario, titulo, fecha);
        t.setEstado(estado);
        // proyecto es null por defecto
        tareaRepository.save(t);
    }

    // Helper para tareas de equipo
    private void crearTareaEquipo(Equipo equipo, Usuario usuario, String titulo, EstadoTarea estado, LocalDate fecha) {
        Tarea t = new Tarea(usuario, titulo, fecha);
        t.setEquipo(equipo);
        t.setEstado(estado);
        tareaRepository.save(t);
    }
}
