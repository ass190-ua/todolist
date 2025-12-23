--
-- PostgreSQL database dump
--

\restrict IdogAYFOqrXQGdcMwyNFHEFQSB31PsxiTJzK0q3Als6dZRjWwxalbEUoZ9hTD3A

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

\unrestrict IdogAYFOqrXQGdcMwyNFHEFQSB31PsxiTJzK0q3Als6dZRjWwxalbEUoZ9hTD3A

