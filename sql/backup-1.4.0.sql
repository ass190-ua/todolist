--
-- PostgreSQL database dump
--

\restrict dxRzZ5estZD6vLBQ0ENejyMysc2dV7twjnBxEPqAELNFQBEXzRbO3nZoBdG8Oo9

-- Dumped from database version 13.22 (Debian 13.22-1.pgdg13+1)
-- Dumped by pg_dump version 13.22 (Debian 13.22-1.pgdg13+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

ALTER TABLE ONLY public.checklist_items DROP CONSTRAINT fkqp2y21wsjvndsucixxi8a85vd;
ALTER TABLE ONLY public.equipos_usuarios DROP CONSTRAINT fkprnt7fvxies9rgwvjcs9y3b40;
ALTER TABLE ONLY public.tareas DROP CONSTRAINT fkft65ny3hi8vlpmybmhpndas8d;
ALTER TABLE ONLY public.tareas DROP CONSTRAINT fkdmoaxl7yv4q6vkc9h32wvbddr;
ALTER TABLE ONLY public.tareas DROP CONSTRAINT fk7a7bbe0arnbqnrekrryoab7je;
ALTER TABLE ONLY public.proyectos DROP CONSTRAINT fk5w1gfpqqeuumkj1897ndr0uwn;
ALTER TABLE ONLY public.equipos_usuarios DROP CONSTRAINT fk2ur21xsvlpk9k0vnkqmg6ukg7;
ALTER TABLE ONLY public.usuarios DROP CONSTRAINT usuarios_pkey;
ALTER TABLE ONLY public.equipos DROP CONSTRAINT uk_4r8c31cj9qstoo4ra6ud41beh;
ALTER TABLE ONLY public.tareas DROP CONSTRAINT tareas_pkey;
ALTER TABLE ONLY public.proyectos DROP CONSTRAINT proyectos_pkey;
ALTER TABLE ONLY public.equipos_usuarios DROP CONSTRAINT equipos_usuarios_pkey;
ALTER TABLE ONLY public.equipos DROP CONSTRAINT equipos_pkey;
ALTER TABLE ONLY public.checklist_items DROP CONSTRAINT checklist_items_pkey;
ALTER TABLE public.usuarios ALTER COLUMN id DROP DEFAULT;
ALTER TABLE public.tareas ALTER COLUMN id DROP DEFAULT;
ALTER TABLE public.proyectos ALTER COLUMN id DROP DEFAULT;
ALTER TABLE public.equipos ALTER COLUMN id DROP DEFAULT;
ALTER TABLE public.checklist_items ALTER COLUMN id DROP DEFAULT;
DROP SEQUENCE public.usuarios_id_seq;
DROP TABLE public.usuarios;
DROP SEQUENCE public.tareas_id_seq;
DROP TABLE public.tareas;
DROP SEQUENCE public.proyectos_id_seq;
DROP TABLE public.proyectos;
DROP TABLE public.equipos_usuarios;
DROP SEQUENCE public.equipos_id_seq;
DROP TABLE public.equipos;
DROP SEQUENCE public.checklist_items_id_seq;
DROP TABLE public.checklist_items;
SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: checklist_items; Type: TABLE; Schema: public; Owner: mads
--

CREATE TABLE public.checklist_items (
    id bigint NOT NULL,
    completado boolean NOT NULL,
    texto character varying(255),
    tarea_id bigint NOT NULL
);


ALTER TABLE public.checklist_items OWNER TO mads;

--
-- Name: checklist_items_id_seq; Type: SEQUENCE; Schema: public; Owner: mads
--

CREATE SEQUENCE public.checklist_items_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.checklist_items_id_seq OWNER TO mads;

--
-- Name: checklist_items_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: mads
--

ALTER SEQUENCE public.checklist_items_id_seq OWNED BY public.checklist_items.id;


--
-- Name: equipos; Type: TABLE; Schema: public; Owner: mads
--

CREATE TABLE public.equipos (
    id bigint NOT NULL,
    admin_user_id bigint,
    nombre character varying(255) NOT NULL
);


ALTER TABLE public.equipos OWNER TO mads;

--
-- Name: equipos_id_seq; Type: SEQUENCE; Schema: public; Owner: mads
--

CREATE SEQUENCE public.equipos_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.equipos_id_seq OWNER TO mads;

--
-- Name: equipos_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: mads
--

ALTER SEQUENCE public.equipos_id_seq OWNED BY public.equipos.id;


--
-- Name: equipos_usuarios; Type: TABLE; Schema: public; Owner: mads
--

CREATE TABLE public.equipos_usuarios (
    equipo_id bigint NOT NULL,
    usuario_id bigint NOT NULL
);


ALTER TABLE public.equipos_usuarios OWNER TO mads;

--
-- Name: proyectos; Type: TABLE; Schema: public; Owner: mads
--

CREATE TABLE public.proyectos (
    id bigint NOT NULL,
    descripcion character varying(255),
    nombre character varying(255) NOT NULL,
    equipo_id bigint NOT NULL
);


ALTER TABLE public.proyectos OWNER TO mads;

--
-- Name: proyectos_id_seq; Type: SEQUENCE; Schema: public; Owner: mads
--

CREATE SEQUENCE public.proyectos_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.proyectos_id_seq OWNER TO mads;

--
-- Name: proyectos_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: mads
--

ALTER SEQUENCE public.proyectos_id_seq OWNED BY public.proyectos.id;


--
-- Name: tareas; Type: TABLE; Schema: public; Owner: mads
--

CREATE TABLE public.tareas (
    id bigint NOT NULL,
    estado character varying(255) NOT NULL,
    fecha_limite date,
    titulo character varying(255) NOT NULL,
    equipo_id bigint,
    proyecto_id bigint,
    usuario_id bigint
);


ALTER TABLE public.tareas OWNER TO mads;

--
-- Name: tareas_id_seq; Type: SEQUENCE; Schema: public; Owner: mads
--

CREATE SEQUENCE public.tareas_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.tareas_id_seq OWNER TO mads;

--
-- Name: tareas_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: mads
--

ALTER SEQUENCE public.tareas_id_seq OWNED BY public.tareas.id;


--
-- Name: usuarios; Type: TABLE; Schema: public; Owner: mads
--

CREATE TABLE public.usuarios (
    id bigint NOT NULL,
    admin boolean NOT NULL,
    bloqueado boolean NOT NULL,
    email character varying(255) NOT NULL,
    fecha_nacimiento date,
    nombre character varying(255),
    password character varying(255)
);


ALTER TABLE public.usuarios OWNER TO mads;

--
-- Name: usuarios_id_seq; Type: SEQUENCE; Schema: public; Owner: mads
--

CREATE SEQUENCE public.usuarios_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.usuarios_id_seq OWNER TO mads;

--
-- Name: usuarios_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: mads
--

ALTER SEQUENCE public.usuarios_id_seq OWNED BY public.usuarios.id;


--
-- Name: checklist_items id; Type: DEFAULT; Schema: public; Owner: mads
--

ALTER TABLE ONLY public.checklist_items ALTER COLUMN id SET DEFAULT nextval('public.checklist_items_id_seq'::regclass);


--
-- Name: equipos id; Type: DEFAULT; Schema: public; Owner: mads
--

ALTER TABLE ONLY public.equipos ALTER COLUMN id SET DEFAULT nextval('public.equipos_id_seq'::regclass);


--
-- Name: proyectos id; Type: DEFAULT; Schema: public; Owner: mads
--

ALTER TABLE ONLY public.proyectos ALTER COLUMN id SET DEFAULT nextval('public.proyectos_id_seq'::regclass);


--
-- Name: tareas id; Type: DEFAULT; Schema: public; Owner: mads
--

ALTER TABLE ONLY public.tareas ALTER COLUMN id SET DEFAULT nextval('public.tareas_id_seq'::regclass);


--
-- Name: usuarios id; Type: DEFAULT; Schema: public; Owner: mads
--

ALTER TABLE ONLY public.usuarios ALTER COLUMN id SET DEFAULT nextval('public.usuarios_id_seq'::regclass);


--
-- Data for Name: checklist_items; Type: TABLE DATA; Schema: public; Owner: mads
--

COPY public.checklist_items (id, completado, texto, tarea_id) FROM stdin;
\.


--
-- Data for Name: equipos; Type: TABLE DATA; Schema: public; Owner: mads
--

COPY public.equipos (id, admin_user_id, nombre) FROM stdin;
1	2	Equipo Desarrollo
2	4	Equipo Marketing
3	5	Equipo Sistemas
4	6	Equipo Diseño
5	7	Equipo Calidad
\.


--
-- Data for Name: equipos_usuarios; Type: TABLE DATA; Schema: public; Owner: mads
--

COPY public.equipos_usuarios (equipo_id, usuario_id) FROM stdin;
1	2
1	1
1	5
1	3
2	6
2	2
2	4
3	5
3	7
3	3
4	6
4	2
4	4
5	2
5	7
5	3
\.


--
-- Data for Name: proyectos; Type: TABLE DATA; Schema: public; Owner: mads
--

COPY public.proyectos (id, descripcion, nombre, equipo_id) FROM stdin;
1	Desarrollo de la versión 1.0 de la app nativa.	App Móvil Android	1
2	Rediseño del portal web con Spring Boot.	Web Corporativa	1
3	Nueva versión de la API con mejoras de rendimiento.	API REST v2	1
4	Estrategia de redes sociales y ads.	Campaña Navidad 2024	2
5	Boletín informativo para clientes.	Newsletter Mensual	2
6	Mover infraestructura on-premise a AWS.	Migración a la Nube	3
7	Revisión completa de seguridad del sistema.	Auditoría de Seguridad	3
8	Actualización de la identidad visual corporativa.	Renovación de Branding	4
9	Implementación de tests automatizados E2E.	Testing Automatizado	5
\.


--
-- Data for Name: tareas; Type: TABLE DATA; Schema: public; Owner: mads
--

COPY public.tareas (id, estado, fecha_limite, titulo, equipo_id, proyecto_id, usuario_id) FROM stdin;
1	TERMINADA	2025-12-08	Diseñar Mockups UI	1	1	2
2	TERMINADA	2025-12-10	Configurar Firebase	1	1	3
3	EN_CURSO	2025-12-14	Implementar Login	1	1	2
4	EN_CURSO	2025-12-15	Optimización de memoria	1	1	5
5	PENDIENTE	2025-12-21	Pantalla de Perfil	1	1	3
6	PENDIENTE	2025-12-23	Integrar API de Mapas	1	1	2
7	PENDIENTE	2025-12-25	Tests Unitarios	1	1	5
8	PENDIENTE	2026-01-14	Notificaciones Push	1	1	3
9	EN_CURSO	2025-12-16	Maquetar Home	1	2	2
10	PENDIENTE	2025-12-21	Desplegar en AWS	1	2	3
11	PENDIENTE	2025-12-22	Configurar SSL	1	2	5
12	PENDIENTE	2025-12-26	Sección de contacto	1	2	2
13	PENDIENTE	2026-01-17	Blog corporativo	1	2	3
14	TERMINADA	2025-12-06	Definir endpoints	1	3	2
15	EN_CURSO	2025-12-15	Implementar autenticación JWT	1	3	3
16	PENDIENTE	2025-12-23	Documentación Swagger	1	3	5
17	PENDIENTE	2026-01-14	Testing de carga	1	3	2
18	PENDIENTE	2026-01-19	Optimización de queries	1	3	3
19	TERMINADA	2025-12-03	Diseñar banners	2	4	4
20	TERMINADA	2025-12-07	Redactar textos	2	4	6
21	EN_CURSO	2025-12-15	Configurar Google Ads	2	4	2
22	PENDIENTE	2025-12-17	Publicar en redes sociales	2	4	4
23	PENDIENTE	2025-12-31	Analizar resultados	2	4	6
24	EN_CURSO	2025-12-16	Diseñar plantilla HTML	2	5	4
25	PENDIENTE	2025-12-21	Seleccionar contenido	2	5	6
26	PENDIENTE	2025-12-24	Integrar con MailChimp	2	5	2
27	EN_CURSO	2025-12-14	Configurar VPC	3	6	5
28	PENDIENTE	2025-12-21	Crear base de datos RDS	3	6	3
29	PENDIENTE	2025-12-26	Migrar servidores	3	6	7
30	PENDIENTE	2026-01-14	Configurar balanceador	3	6	5
31	TERMINADA	2025-12-09	Escaneo de vulnerabilidades	3	7	7
32	EN_CURSO	2025-12-14	Actualizar certificados	3	7	5
33	PENDIENTE	2025-12-24	Implementar 2FA	3	7	3
34	PENDIENTE	2026-01-14	Auditoría de logs	3	7	7
35	TERMINADA	2025-12-01	Investigación de mercado	4	8	6
36	TERMINADA	2025-12-05	Propuesta de logo	4	8	4
37	EN_CURSO	2025-12-15	Paleta de colores	4	8	6
38	PENDIENTE	2025-12-28	Manual de identidad	4	8	2
39	PENDIENTE	2026-01-18	Mockups aplicados	4	8	4
40	TERMINADA	2025-12-11	Configurar Selenium	5	9	7
41	EN_CURSO	2025-12-16	Escribir tests E2E	5	9	2
42	PENDIENTE	2025-12-25	Integrar con CI/CD	5	9	3
43	PENDIENTE	2026-01-16	Reports automáticos	5	9	7
44	PENDIENTE	2025-12-14	Comprar leche	\N	\N	2
45	TERMINADA	2025-12-13	Llamar al dentista	\N	\N	2
46	TERMINADA	2025-12-12	Revisar correos	\N	\N	2
47	EN_CURSO	2025-12-15	Preparar presentación	\N	\N	2
48	PENDIENTE	2025-12-21	Renovar seguro coche	\N	\N	2
49	PENDIENTE	2025-12-19	Comprar regalo cumpleaños	\N	\N	2
50	PENDIENTE	2025-12-15	Hacer ejercicio	\N	\N	2
51	EN_CURSO	2025-12-23	Estudiar certificación Java	\N	\N	2
52	PENDIENTE	2025-12-22	Renovar DNI	\N	\N	3
53	TERMINADA	2025-12-11	Pagar facturas	\N	\N	3
54	PENDIENTE	2025-12-20	Cita médico	\N	\N	3
55	PENDIENTE	2026-01-14	Comprar regalos Navidad	\N	\N	3
56	EN_CURSO	2025-12-21	Estudiar certificación Java	\N	\N	5
57	PENDIENTE	2026-01-14	Leer libro técnico	\N	\N	5
58	TERMINADA	2025-12-10	Organizar escritorio	\N	\N	5
59	PENDIENTE	2025-12-18	Actualizar LinkedIn	\N	\N	5
60	EN_CURSO	2025-12-16	Planificar vacaciones	\N	\N	4
61	PENDIENTE	2025-12-21	Comprar plantas	\N	\N	4
62	TERMINADA	2025-12-13	Clases de yoga	\N	\N	4
63	EN_CURSO	2025-12-15	Curso de fotografía	\N	\N	6
64	PENDIENTE	2026-01-14	Actualizar portfolio	\N	\N	6
65	PENDIENTE	2025-12-24	Ordenar fotos	\N	\N	6
66	PENDIENTE	2025-12-22	Mantenimiento coche	\N	\N	7
67	EN_CURSO	2025-12-22	Aprender Docker	\N	\N	7
68	PENDIENTE	2026-01-17	Preparar CV	\N	\N	7
69	PENDIENTE	2025-12-17	Reunión de retrospectiva	1	\N	2
70	EN_CURSO	2025-12-15	Actualizar documentación técnica	1	\N	3
71	PENDIENTE	2025-12-21	Code review semanal	1	\N	5
72	PENDIENTE	2025-12-21	Análisis de competencia	2	\N	4
73	EN_CURSO	2025-12-16	Planificar contenido del mes	2	\N	6
74	TERMINADA	2025-12-13	Backup semanal	3	\N	5
75	PENDIENTE	2025-12-18	Monitoreo de servidores	3	\N	7
76	PENDIENTE	2025-12-19	Sesión de brainstorming	4	\N	6
77	PENDIENTE	2025-12-23	Revisar guía de estilo	4	\N	4
78	PENDIENTE	2025-12-20	Reunión de planificación	5	\N	7
79	EN_CURSO	2025-12-17	Revisar casos de prueba	5	\N	3
\.


--
-- Data for Name: usuarios; Type: TABLE DATA; Schema: public; Owner: mads
--

COPY public.usuarios (id, admin, bloqueado, email, fecha_nacimiento, nombre, password) FROM stdin;
1	t	f	admin@ua.es	\N	Administrador	admin
2	f	f	user@ua.es	\N	Usuario Principal	1234
3	f	f	arturo@ua.es	\N	Arturo	1234
4	f	f	esther@ua.es	\N	Esther	1234
5	f	f	hugo@ua.es	\N	Hugo	1234
6	f	f	laura@ua.es	\N	Laura	1234
7	f	f	carlos@ua.es	\N	Carlos	1234
\.


--
-- Name: checklist_items_id_seq; Type: SEQUENCE SET; Schema: public; Owner: mads
--

SELECT pg_catalog.setval('public.checklist_items_id_seq', 1, false);


--
-- Name: equipos_id_seq; Type: SEQUENCE SET; Schema: public; Owner: mads
--

SELECT pg_catalog.setval('public.equipos_id_seq', 5, true);


--
-- Name: proyectos_id_seq; Type: SEQUENCE SET; Schema: public; Owner: mads
--

SELECT pg_catalog.setval('public.proyectos_id_seq', 9, true);


--
-- Name: tareas_id_seq; Type: SEQUENCE SET; Schema: public; Owner: mads
--

SELECT pg_catalog.setval('public.tareas_id_seq', 79, true);


--
-- Name: usuarios_id_seq; Type: SEQUENCE SET; Schema: public; Owner: mads
--

SELECT pg_catalog.setval('public.usuarios_id_seq', 7, true);


--
-- Name: checklist_items checklist_items_pkey; Type: CONSTRAINT; Schema: public; Owner: mads
--

ALTER TABLE ONLY public.checklist_items
    ADD CONSTRAINT checklist_items_pkey PRIMARY KEY (id);


--
-- Name: equipos equipos_pkey; Type: CONSTRAINT; Schema: public; Owner: mads
--

ALTER TABLE ONLY public.equipos
    ADD CONSTRAINT equipos_pkey PRIMARY KEY (id);


--
-- Name: equipos_usuarios equipos_usuarios_pkey; Type: CONSTRAINT; Schema: public; Owner: mads
--

ALTER TABLE ONLY public.equipos_usuarios
    ADD CONSTRAINT equipos_usuarios_pkey PRIMARY KEY (equipo_id, usuario_id);


--
-- Name: proyectos proyectos_pkey; Type: CONSTRAINT; Schema: public; Owner: mads
--

ALTER TABLE ONLY public.proyectos
    ADD CONSTRAINT proyectos_pkey PRIMARY KEY (id);


--
-- Name: tareas tareas_pkey; Type: CONSTRAINT; Schema: public; Owner: mads
--

ALTER TABLE ONLY public.tareas
    ADD CONSTRAINT tareas_pkey PRIMARY KEY (id);


--
-- Name: equipos uk_4r8c31cj9qstoo4ra6ud41beh; Type: CONSTRAINT; Schema: public; Owner: mads
--

ALTER TABLE ONLY public.equipos
    ADD CONSTRAINT uk_4r8c31cj9qstoo4ra6ud41beh UNIQUE (nombre);


--
-- Name: usuarios usuarios_pkey; Type: CONSTRAINT; Schema: public; Owner: mads
--

ALTER TABLE ONLY public.usuarios
    ADD CONSTRAINT usuarios_pkey PRIMARY KEY (id);


--
-- Name: equipos_usuarios fk2ur21xsvlpk9k0vnkqmg6ukg7; Type: FK CONSTRAINT; Schema: public; Owner: mads
--

ALTER TABLE ONLY public.equipos_usuarios
    ADD CONSTRAINT fk2ur21xsvlpk9k0vnkqmg6ukg7 FOREIGN KEY (equipo_id) REFERENCES public.equipos(id);


--
-- Name: proyectos fk5w1gfpqqeuumkj1897ndr0uwn; Type: FK CONSTRAINT; Schema: public; Owner: mads
--

ALTER TABLE ONLY public.proyectos
    ADD CONSTRAINT fk5w1gfpqqeuumkj1897ndr0uwn FOREIGN KEY (equipo_id) REFERENCES public.equipos(id);


--
-- Name: tareas fk7a7bbe0arnbqnrekrryoab7je; Type: FK CONSTRAINT; Schema: public; Owner: mads
--

ALTER TABLE ONLY public.tareas
    ADD CONSTRAINT fk7a7bbe0arnbqnrekrryoab7je FOREIGN KEY (equipo_id) REFERENCES public.equipos(id);


--
-- Name: tareas fkdmoaxl7yv4q6vkc9h32wvbddr; Type: FK CONSTRAINT; Schema: public; Owner: mads
--

ALTER TABLE ONLY public.tareas
    ADD CONSTRAINT fkdmoaxl7yv4q6vkc9h32wvbddr FOREIGN KEY (usuario_id) REFERENCES public.usuarios(id);


--
-- Name: tareas fkft65ny3hi8vlpmybmhpndas8d; Type: FK CONSTRAINT; Schema: public; Owner: mads
--

ALTER TABLE ONLY public.tareas
    ADD CONSTRAINT fkft65ny3hi8vlpmybmhpndas8d FOREIGN KEY (proyecto_id) REFERENCES public.proyectos(id);


--
-- Name: equipos_usuarios fkprnt7fvxies9rgwvjcs9y3b40; Type: FK CONSTRAINT; Schema: public; Owner: mads
--

ALTER TABLE ONLY public.equipos_usuarios
    ADD CONSTRAINT fkprnt7fvxies9rgwvjcs9y3b40 FOREIGN KEY (usuario_id) REFERENCES public.usuarios(id);


--
-- Name: checklist_items fkqp2y21wsjvndsucixxi8a85vd; Type: FK CONSTRAINT; Schema: public; Owner: mads
--

ALTER TABLE ONLY public.checklist_items
    ADD CONSTRAINT fkqp2y21wsjvndsucixxi8a85vd FOREIGN KEY (tarea_id) REFERENCES public.tareas(id);


--
-- PostgreSQL database dump complete
--

\unrestrict dxRzZ5estZD6vLBQ0ENejyMysc2dV7twjnBxEPqAELNFQBEXzRbO3nZoBdG8Oo9

