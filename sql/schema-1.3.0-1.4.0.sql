  \restrict IdogAYFOqrXQGdcMwyNFHEFQSB31PsxiTJzK0q3Als6dZRjWwxalbEUoZ9hTD3A
---
  \restrict Ms7FRqX8xLOt4MsNgDrwCEPTq7GgQtEjO5GEGe48WUj9HFWAj4TXY7n2U3DYZjM
26,
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
107,141d71
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
147,148c77
      estado character varying(255) NOT NULL,
      fecha_limite date,
---
      terminada boolean NOT NULL,
150,152c79
      equipo_id bigint,
      proyecto_id bigint,
      usuario_id bigint
---
      usuario_id bigint NOT NULL
218,224d144
  -- Name: checklist_items id; Type: DEFAULT; Schema: public; Owner: mads
  --
  
  ALTER TABLE ONLY public.checklist_items ALTER COLUMN id SET DEFAULT nextval('public.checklist_items_id_seq'::regclass);
  
  
  --
232,238d151
  -- Name: proyectos id; Type: DEFAULT; Schema: public; Owner: mads
  --
  
  ALTER TABLE ONLY public.proyectos ALTER COLUMN id SET DEFAULT nextval('public.proyectos_id_seq'::regclass);
  
  
  --
253,260d165
  -- Name: checklist_items checklist_items_pkey; Type: CONSTRAINT; Schema: public; Owner: mads
  --
  
  ALTER TABLE ONLY public.checklist_items
          ADD CONSTRAINT checklist_items_pkey PRIMARY KEY (id);
  
  
  --
277,284d181
  -- Name: proyectos proyectos_pkey; Type: CONSTRAINT; Schema: public; Owner: mads
  --
  
  ALTER TABLE ONLY public.proyectos
          ADD CONSTRAINT proyectos_pkey PRIMARY KEY (id);
  
  
  --
317,332d213
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
341,348d221
  -- Name: tareas fkft65ny3hi8vlpmybmhpndas8d; Type: FK CONSTRAINT; Schema: public; Owner: mads
  --
  
  ALTER TABLE ONLY public.tareas
          ADD CONSTRAINT fkft65ny3hi8vlpmybmhpndas8d FOREIGN KEY (proyecto_id) REFERENCES public.proyectos(id);
  
  
  --
357,364d229
  -- Name: checklist_items fkqp2y21wsjvndsucixxi8a85vd; Type: FK CONSTRAINT; Schema: public; Owner: mads
  --
  
  ALTER TABLE ONLY public.checklist_items
          ADD CONSTRAINT fkqp2y21wsjvndsucixxi8a85vd FOREIGN KEY (tarea_id) REFERENCES public.tareas(id);
  
  
  --
368c233
  \unrestrict IdogAYFOqrXQGdcMwyNFHEFQSB31PsxiTJzK0q3Als6dZRjWwxalbEUoZ9hTD3A
---
  \unrestrict Ms7FRqX8xLOt4MsNgDrwCEPTq7GgQtEjO5GEGe48WUj9HFWAj4TXY7n2U3DYZjM
