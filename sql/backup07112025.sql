--
-- PostgreSQL database dump
--

\restrict Zd1ucMYwsKt40Rc0H3NfZj8W74eJr5HFMOVJXPtEG3xwzCId9t9owO6sZseWOeT

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

ALTER TABLE ONLY public.equipos_usuarios DROP CONSTRAINT fkprnt7fvxies9rgwvjcs9y3b40;
ALTER TABLE ONLY public.tareas DROP CONSTRAINT fkdmoaxl7yv4q6vkc9h32wvbddr;
ALTER TABLE ONLY public.equipos_usuarios DROP CONSTRAINT fk2ur21xsvlpk9k0vnkqmg6ukg7;
ALTER TABLE ONLY public.usuarios DROP CONSTRAINT usuarios_pkey;
ALTER TABLE ONLY public.equipos DROP CONSTRAINT uk_4r8c31cj9qstoo4ra6ud41beh;
ALTER TABLE ONLY public.tareas DROP CONSTRAINT tareas_pkey;
ALTER TABLE ONLY public.equipos_usuarios DROP CONSTRAINT equipos_usuarios_pkey;
ALTER TABLE ONLY public.equipos DROP CONSTRAINT equipos_pkey;
ALTER TABLE public.usuarios ALTER COLUMN id DROP DEFAULT;
ALTER TABLE public.tareas ALTER COLUMN id DROP DEFAULT;
ALTER TABLE public.equipos ALTER COLUMN id DROP DEFAULT;
DROP SEQUENCE public.usuarios_id_seq;
DROP TABLE public.usuarios;
DROP SEQUENCE public.tareas_id_seq;
DROP TABLE public.tareas;
DROP TABLE public.equipos_usuarios;
DROP SEQUENCE public.equipos_id_seq;
DROP TABLE public.equipos;
SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: equipos; Type: TABLE; Schema: public; Owner: mads
--

CREATE TABLE public.equipos (
    id bigint NOT NULL,
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
-- Name: tareas; Type: TABLE; Schema: public; Owner: mads
--

CREATE TABLE public.tareas (
    id bigint NOT NULL,
    titulo character varying(255) NOT NULL,
    usuario_id bigint NOT NULL
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
-- Name: equipos id; Type: DEFAULT; Schema: public; Owner: mads
--

ALTER TABLE ONLY public.equipos ALTER COLUMN id SET DEFAULT nextval('public.equipos_id_seq'::regclass);


--
-- Name: tareas id; Type: DEFAULT; Schema: public; Owner: mads
--

ALTER TABLE ONLY public.tareas ALTER COLUMN id SET DEFAULT nextval('public.tareas_id_seq'::regclass);


--
-- Name: usuarios id; Type: DEFAULT; Schema: public; Owner: mads
--

ALTER TABLE ONLY public.usuarios ALTER COLUMN id SET DEFAULT nextval('public.usuarios_id_seq'::regclass);


--
-- Data for Name: equipos; Type: TABLE DATA; Schema: public; Owner: mads
--

COPY public.equipos (id, nombre) FROM stdin;
\.


--
-- Data for Name: equipos_usuarios; Type: TABLE DATA; Schema: public; Owner: mads
--

COPY public.equipos_usuarios (equipo_id, usuario_id) FROM stdin;
\.


--
-- Data for Name: tareas; Type: TABLE DATA; Schema: public; Owner: mads
--

COPY public.tareas (id, titulo, usuario_id) FROM stdin;
\.


--
-- Data for Name: usuarios; Type: TABLE DATA; Schema: public; Owner: mads
--

COPY public.usuarios (id, admin, bloqueado, email, fecha_nacimiento, nombre, password) FROM stdin;
1	f	f	alu1@alu.com	\N	alu1	123
2	f	f	alu2@alu.com	\N	alu2	123
3	f	f	alu3@alu.com	\N	alu3	123
\.


--
-- Name: equipos_id_seq; Type: SEQUENCE SET; Schema: public; Owner: mads
--

SELECT pg_catalog.setval('public.equipos_id_seq', 1, false);


--
-- Name: tareas_id_seq; Type: SEQUENCE SET; Schema: public; Owner: mads
--

SELECT pg_catalog.setval('public.tareas_id_seq', 1, false);


--
-- Name: usuarios_id_seq; Type: SEQUENCE SET; Schema: public; Owner: mads
--

SELECT pg_catalog.setval('public.usuarios_id_seq', 3, true);


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
-- Name: tareas fkdmoaxl7yv4q6vkc9h32wvbddr; Type: FK CONSTRAINT; Schema: public; Owner: mads
--

ALTER TABLE ONLY public.tareas
    ADD CONSTRAINT fkdmoaxl7yv4q6vkc9h32wvbddr FOREIGN KEY (usuario_id) REFERENCES public.usuarios(id);


--
-- Name: equipos_usuarios fkprnt7fvxies9rgwvjcs9y3b40; Type: FK CONSTRAINT; Schema: public; Owner: mads
--

ALTER TABLE ONLY public.equipos_usuarios
    ADD CONSTRAINT fkprnt7fvxies9rgwvjcs9y3b40 FOREIGN KEY (usuario_id) REFERENCES public.usuarios(id);


--
-- PostgreSQL database dump complete
--

\unrestrict Zd1ucMYwsKt40Rc0H3NfZj8W74eJr5HFMOVJXPtEG3xwzCId9t9owO6sZseWOeT

