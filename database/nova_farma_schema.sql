--
-- PostgreSQL database dump
--

-- Dumped from database version 17.5
-- Dumped by pg_dump version 17.5

-- Started on 2025-11-22 22:18:17

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET SESSION AUTHORIZATION 'postgres';

DROP DATABASE IF EXISTS "nova_farma_db";
--
-- TOC entry 4943 (class 1262 OID 16654)
-- Name: nova_farma_db; Type: DATABASE; Schema: -; Owner: postgres
--

CREATE DATABASE "nova_farma_db" WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc LOCALE = 'Spanish_Peru.utf8';


\connect "nova_farma_db"

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET SESSION AUTHORIZATION 'pg_database_owner';

--
-- TOC entry 4 (class 2615 OID 2200)
-- Name: public; Type: SCHEMA; Schema: -; Owner: pg_database_owner
--

CREATE SCHEMA "public";


--
-- TOC entry 4944 (class 0 OID 0)
-- Dependencies: 4
-- Name: SCHEMA "public"; Type: COMMENT; Schema: -; Owner: pg_database_owner
--

COMMENT ON SCHEMA "public" IS 'standard public schema';


SET SESSION AUTHORIZATION 'postgres';

--
-- TOC entry 224 (class 1255 OID 16707)
-- Name: actualizar_stock_venta(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION "public"."actualizar_stock_venta"() RETURNS "trigger"
    LANGUAGE "plpgsql"
    AS '
BEGIN
    UPDATE productos 
    SET stock = stock - NEW.cantidad
    WHERE id = NEW.producto_id;
    
    -- Validar stock negativo
    IF (SELECT stock FROM productos WHERE id = NEW.producto_id) < 0 THEN
        RAISE EXCEPTION ''Stock insuficiente para el producto ID %'', NEW.producto_id;
    END IF;
    
    RETURN NEW;
END;
';


SET default_tablespace = '';

SET default_table_access_method = "heap";

--
-- TOC entry 220 (class 1259 OID 16668)
-- Name: productos; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE "public"."productos" (
    "id" integer NOT NULL,
    "nombre" character varying(100) NOT NULL,
    "descripcion" "text",
    "precio" numeric(10,2) NOT NULL,
    "stock" integer DEFAULT 0 NOT NULL,
    "fecha_vencimiento" "date",
    "fecha_creacion" timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    "fecha_modificacion" timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    "activo" boolean DEFAULT true,
    CONSTRAINT "productos_precio_check" CHECK (("precio" >= (0)::numeric)),
    CONSTRAINT "productos_stock_check" CHECK (("stock" >= 0))
);


--
-- TOC entry 219 (class 1259 OID 16667)
-- Name: productos_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE "public"."productos_id_seq"
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4945 (class 0 OID 0)
-- Dependencies: 219
-- Name: productos_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE "public"."productos_id_seq" OWNED BY "public"."productos"."id";


--
-- TOC entry 223 (class 1259 OID 16703)
-- Name: productos_por_vencer; Type: VIEW; Schema: public; Owner: postgres
--

CREATE VIEW "public"."productos_por_vencer" AS
 SELECT "id",
    "nombre",
    "descripcion",
    "precio",
    "stock",
    "fecha_vencimiento",
    ("fecha_vencimiento" - CURRENT_DATE) AS "dias_restantes"
   FROM "public"."productos"
  WHERE (("fecha_vencimiento" IS NOT NULL) AND ("fecha_vencimiento" <= (CURRENT_DATE + '30 days'::interval)) AND ("fecha_vencimiento" >= CURRENT_DATE))
  ORDER BY "fecha_vencimiento";


--
-- TOC entry 218 (class 1259 OID 16656)
-- Name: usuarios; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE "public"."usuarios" (
    "id" integer NOT NULL,
    "username" character varying(50) NOT NULL,
    "password_hash" character varying(64) NOT NULL,
    "rol" character varying(20) NOT NULL,
    "fecha_creacion" timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "usuarios_rol_check" CHECK ((("rol")::"text" = ANY ((ARRAY['ADMINISTRADOR'::character varying, 'TRABAJADOR'::character varying])::"text"[])))
);


--
-- TOC entry 217 (class 1259 OID 16655)
-- Name: usuarios_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE "public"."usuarios_id_seq"
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4946 (class 0 OID 0)
-- Dependencies: 217
-- Name: usuarios_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE "public"."usuarios_id_seq" OWNED BY "public"."usuarios"."id";


--
-- TOC entry 222 (class 1259 OID 16684)
-- Name: ventas; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE "public"."ventas" (
    "id" integer NOT NULL,
    "producto_id" integer NOT NULL,
    "usuario_id" integer NOT NULL,
    "cantidad" integer NOT NULL,
    "precio_unitario" numeric(10,2) NOT NULL,
    "total" numeric(10,2) NOT NULL,
    "fecha_venta" timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT "ventas_cantidad_check" CHECK (("cantidad" > 0))
);


--
-- TOC entry 221 (class 1259 OID 16683)
-- Name: ventas_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE "public"."ventas_id_seq"
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- TOC entry 4947 (class 0 OID 0)
-- Dependencies: 221
-- Name: ventas_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE "public"."ventas_id_seq" OWNED BY "public"."ventas"."id";


--
-- TOC entry 4759 (class 2604 OID 16671)
-- Name: productos id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY "public"."productos" ALTER COLUMN "id" SET DEFAULT "nextval"('"public"."productos_id_seq"'::"regclass");


--
-- TOC entry 4757 (class 2604 OID 16659)
-- Name: usuarios id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY "public"."usuarios" ALTER COLUMN "id" SET DEFAULT "nextval"('"public"."usuarios_id_seq"'::"regclass");


--
-- TOC entry 4764 (class 2604 OID 16687)
-- Name: ventas id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY "public"."ventas" ALTER COLUMN "id" SET DEFAULT "nextval"('"public"."ventas_id_seq"'::"regclass");


--
-- TOC entry 4935 (class 0 OID 16668)
-- Dependencies: 220
-- Data for Name: productos; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO "public"."productos" ("id", "nombre", "descripcion", "precio", "stock", "fecha_vencimiento", "fecha_creacion", "fecha_modificacion", "activo") VALUES (1, 'Paracetamol 500mg', 'Analgésico y antipirético', 5.50, 85, '2025-12-31', '2025-11-21 16:33:41.543318', '2025-11-21 16:33:41.543318', true);
INSERT INTO "public"."productos" ("id", "nombre", "descripcion", "precio", "stock", "fecha_vencimiento", "fecha_creacion", "fecha_modificacion", "activo") VALUES (4, 'Vitamina C 1000mg', 'Suplemento vitamínico', 15.00, 200, '2026-03-20', '2025-11-21 16:33:41.543318', '2025-11-21 16:33:41.543318', true);
INSERT INTO "public"."productos" ("id", "nombre", "descripcion", "precio", "stock", "fecha_vencimiento", "fecha_creacion", "fecha_modificacion", "activo") VALUES (8, 'Termómetro Digital', 'Medición de temperatura corporal', 25.00, 30, NULL, '2025-11-21 16:33:41.543318', '2025-11-21 16:33:41.543318', true);
INSERT INTO "public"."productos" ("id", "nombre", "descripcion", "precio", "stock", "fecha_vencimiento", "fecha_creacion", "fecha_modificacion", "activo") VALUES (6, 'Loratadina 10mg', 'Antihistamínico', 6.50, 117, '2025-11-25', '2025-11-21 16:33:41.543318', '2025-11-21 16:33:41.543318', true);
INSERT INTO "public"."productos" ("id", "nombre", "descripcion", "precio", "stock", "fecha_vencimiento", "fecha_creacion", "fecha_modificacion", "activo") VALUES (3, '[DESCONTINUADO] Amoxicilina 500mg', 'Antibiótico de amplio espectro', 12.50, 0, '2025-06-30', '2025-11-21 16:33:41.543318', '2025-11-21 16:33:41.543318', false);
INSERT INTO "public"."productos" ("id", "nombre", "descripcion", "precio", "stock", "fecha_vencimiento", "fecha_creacion", "fecha_modificacion", "activo") VALUES (17, 'Producto Vencido TEST', 'Para prueba', 5.00, 10, '2024-01-01', '2025-11-21 19:37:43.704451', '2025-11-21 19:37:43.704451', false);
INSERT INTO "public"."productos" ("id", "nombre", "descripcion", "precio", "stock", "fecha_vencimiento", "fecha_creacion", "fecha_modificacion", "activo") VALUES (7, 'Alcohol en Gel 500ml', 'Desinfectante para manos', 4.20, 139, '2026-12-31', '2025-11-21 16:33:41.543318', '2025-11-21 16:33:41.543318', true);
INSERT INTO "public"."productos" ("id", "nombre", "descripcion", "precio", "stock", "fecha_vencimiento", "fecha_creacion", "fecha_modificacion", "activo") VALUES (18, 'omeprasol', 'Utilizado para reducir la producción de ácido estomacal', 6.00, 12, '2025-12-31', '2025-11-21 22:07:19.884072', '2025-11-21 22:07:19.884072', true);
INSERT INTO "public"."productos" ("id", "nombre", "descripcion", "precio", "stock", "fecha_vencimiento", "fecha_creacion", "fecha_modificacion", "activo") VALUES (19, 'vitamina D', 'Bueno para la vitalidad', 9.00, 150, '2026-11-05', '2025-11-22 00:48:47.481174', '2025-11-22 00:48:47.481174', true);
INSERT INTO "public"."productos" ("id", "nombre", "descripcion", "precio", "stock", "fecha_vencimiento", "fecha_creacion", "fecha_modificacion", "activo") VALUES (20, 'Pseudoefedrina', 'analgesico', 5.00, 120, '2025-12-31', '2025-11-22 13:40:04.353005', '2025-11-22 13:40:04.353005', true);
INSERT INTO "public"."productos" ("id", "nombre", "descripcion", "precio", "stock", "fecha_vencimiento", "fecha_creacion", "fecha_modificacion", "activo") VALUES (22, 'Aspirina Forte 650mg', 'Alivio para dolor de cabeza severo y migraña', 2.50, 200, '2027-05-20', '2025-11-22 18:15:39.35078', '2025-11-22 18:15:39.35078', true);
INSERT INTO "public"."productos" ("id", "nombre", "descripcion", "precio", "stock", "fecha_vencimiento", "fecha_creacion", "fecha_modificacion", "activo") VALUES (23, 'Ibuprofeno 600mg', 'Antiinflamatorio para dolores musculares', 8.50, 150, '2026-08-15', '2025-11-22 18:15:39.35078', '2025-11-22 18:15:39.35078', true);
INSERT INTO "public"."productos" ("id", "nombre", "descripcion", "precio", "stock", "fecha_vencimiento", "fecha_creacion", "fecha_modificacion", "activo") VALUES (24, 'Apronax 275mg', 'Naproxeno sódico para inflamación', 4.20, 120, '2026-12-01', '2025-11-22 18:15:39.35078', '2025-11-22 18:15:39.35078', true);
INSERT INTO "public"."productos" ("id", "nombre", "descripcion", "precio", "stock", "fecha_vencimiento", "fecha_creacion", "fecha_modificacion", "activo") VALUES (25, 'Panadol Antigripal', 'Alivio de síntomas de gripe y resfriado', 3.50, 180, '2027-01-10', '2025-11-22 18:15:39.35078', '2025-11-22 18:15:39.35078', true);
INSERT INTO "public"."productos" ("id", "nombre", "descripcion", "precio", "stock", "fecha_vencimiento", "fecha_creacion", "fecha_modificacion", "activo") VALUES (26, 'Bismutol 150ml', 'Suspensión para alivio estomacal y acidez', 18.90, 40, '2026-03-15', '2025-11-22 18:15:39.35078', '2025-11-22 18:15:39.35078', true);
INSERT INTO "public"."productos" ("id", "nombre", "descripcion", "precio", "stock", "fecha_vencimiento", "fecha_creacion", "fecha_modificacion", "activo") VALUES (27, 'Ambroxol Jarabe 120ml', 'Expectorante pediátrico sabor fresa', 12.00, 25, '2025-12-10', '2025-11-22 18:17:52.386841', '2025-11-22 18:17:52.386841', true);
INSERT INTO "public"."productos" ("id", "nombre", "descripcion", "precio", "stock", "fecha_vencimiento", "fecha_creacion", "fecha_modificacion", "activo") VALUES (28, 'Suero Oral Electrolitos', 'Rehidratante sabor naranja 1L', 9.50, 60, '2025-12-05', '2025-11-22 18:17:52.386841', '2025-11-22 18:17:52.386841', true);
INSERT INTO "public"."productos" ("id", "nombre", "descripcion", "precio", "stock", "fecha_vencimiento", "fecha_creacion", "fecha_modificacion", "activo") VALUES (29, 'Gotas para Ojos Visine', 'Alivio para ojos rojos e irritados', 22.00, 15, '2025-12-15', '2025-11-22 18:17:52.386841', '2025-11-22 18:17:52.386841', true);
INSERT INTO "public"."productos" ("id", "nombre", "descripcion", "precio", "stock", "fecha_vencimiento", "fecha_creacion", "fecha_modificacion", "activo") VALUES (30, 'Jarabe Tos Eucalipto', 'Jarabe natural reforzado - Lote antiguo', 15.00, 5, '2025-10-01', '2025-11-22 18:17:52.386841', '2025-11-22 18:17:52.386841', true);
INSERT INTO "public"."productos" ("id", "nombre", "descripcion", "precio", "stock", "fecha_vencimiento", "fecha_creacion", "fecha_modificacion", "activo") VALUES (31, 'Alcohol Medicinal 70°', 'Desinfectante botella pequeña - Lote remate', 2.00, 10, '2025-09-15', '2025-11-22 18:17:52.386841', '2025-11-22 18:17:52.386841', true);
INSERT INTO "public"."productos" ("id", "nombre", "descripcion", "precio", "stock", "fecha_vencimiento", "fecha_creacion", "fecha_modificacion", "activo") VALUES (32, 'Protector Solar Eucerin 50+', 'Bloqueador solar toque seco facial', 95.00, 20, '2027-11-30', '2025-11-22 18:17:52.386841', '2025-11-22 18:17:52.386841', true);
INSERT INTO "public"."productos" ("id", "nombre", "descripcion", "precio", "stock", "fecha_vencimiento", "fecha_creacion", "fecha_modificacion", "activo") VALUES (33, 'Crema Hidratante CeraVe', 'Para piel seca y sensible 473ml', 65.00, 30, '2028-01-01', '2025-11-22 18:17:52.386841', '2025-11-22 18:17:52.386841', true);
INSERT INTO "public"."productos" ("id", "nombre", "descripcion", "precio", "stock", "fecha_vencimiento", "fecha_creacion", "fecha_modificacion", "activo") VALUES (34, 'Shampoo Tio Nacho', 'Jalea Real Aclarante', 28.50, 45, '2027-06-20', '2025-11-22 18:17:52.386841', '2025-11-22 18:17:52.386841', true);
INSERT INTO "public"."productos" ("id", "nombre", "descripcion", "precio", "stock", "fecha_vencimiento", "fecha_creacion", "fecha_modificacion", "activo") VALUES (35, 'Desodorante Rexona Clinical', 'Protección avanzada en crema', 16.50, 80, '2026-09-10', '2025-11-22 18:17:52.386841', '2025-11-22 18:17:52.386841', true);
INSERT INTO "public"."productos" ("id", "nombre", "descripcion", "precio", "stock", "fecha_vencimiento", "fecha_creacion", "fecha_modificacion", "activo") VALUES (36, 'Pañales Huggies G x50', 'Pañales etapa gateo ultra absorción', 52.00, 15, '2030-01-01', '2025-11-22 18:17:52.386841', '2025-11-22 18:17:52.386841', true);
INSERT INTO "public"."productos" ("id", "nombre", "descripcion", "precio", "stock", "fecha_vencimiento", "fecha_creacion", "fecha_modificacion", "activo") VALUES (37, 'Fórmula Enfamil Premium 1', 'Lata de 800g para recién nacidos', 110.00, 12, '2026-07-15', '2025-11-22 18:17:52.386841', '2025-11-22 18:17:52.386841', true);
INSERT INTO "public"."productos" ("id", "nombre", "descripcion", "precio", "stock", "fecha_vencimiento", "fecha_creacion", "fecha_modificacion", "activo") VALUES (38, 'Toallitas Húmedas Johnson', 'Paquete x100 unidades sin alcohol', 14.00, 200, '2026-12-31', '2025-11-22 18:17:52.386841', '2025-11-22 18:17:52.386841', true);
INSERT INTO "public"."productos" ("id", "nombre", "descripcion", "precio", "stock", "fecha_vencimiento", "fecha_creacion", "fecha_modificacion", "activo") VALUES (39, 'Crema Dr. Zaidman', 'Para escaldaduras de bebé', 18.00, 35, '2026-04-20', '2025-11-22 18:17:52.386841', '2025-11-22 18:17:52.386841', true);
INSERT INTO "public"."productos" ("id", "nombre", "descripcion", "precio", "stock", "fecha_vencimiento", "fecha_creacion", "fecha_modificacion", "activo") VALUES (40, 'Losartan 50mg', 'Tratamiento para la hipertensión arterial', 35.00, 300, '2027-02-28', '2025-11-22 18:17:52.386841', '2025-11-22 18:17:52.386841', true);
INSERT INTO "public"."productos" ("id", "nombre", "descripcion", "precio", "stock", "fecha_vencimiento", "fecha_creacion", "fecha_modificacion", "activo") VALUES (41, 'Metformina 850mg', 'Control de glucosa para diabéticos', 28.00, 250, '2026-11-15', '2025-11-22 18:17:52.386841', '2025-11-22 18:17:52.386841', true);
INSERT INTO "public"."productos" ("id", "nombre", "descripcion", "precio", "stock", "fecha_vencimiento", "fecha_creacion", "fecha_modificacion", "activo") VALUES (42, 'Atorvastatina 20mg', 'Control del colesterol', 45.00, 100, '2026-10-05', '2025-11-22 18:17:52.386841', '2025-11-22 18:17:52.386841', true);
INSERT INTO "public"."productos" ("id", "nombre", "descripcion", "precio", "stock", "fecha_vencimiento", "fecha_creacion", "fecha_modificacion", "activo") VALUES (43, 'Vick Vaporub 50g', 'Ungüento para descongestionar', 10.50, 90, '2027-06-01', '2025-11-22 18:19:00.446985', '2025-11-22 18:19:00.446985', true);
INSERT INTO "public"."productos" ("id", "nombre", "descripcion", "precio", "stock", "fecha_vencimiento", "fecha_creacion", "fecha_modificacion", "activo") VALUES (44, 'Curitas Caja x100', 'Vendas adhesivas standard', 8.00, 500, '2029-01-01', '2025-11-22 18:19:00.446985', '2025-11-22 18:19:00.446985', true);
INSERT INTO "public"."productos" ("id", "nombre", "descripcion", "precio", "stock", "fecha_vencimiento", "fecha_creacion", "fecha_modificacion", "activo") VALUES (45, 'Gasa Estéril 10x10', 'Paquete individual', 1.50, 1000, '2028-05-20', '2025-11-22 18:19:00.446985', '2025-11-22 18:19:00.446985', true);
INSERT INTO "public"."productos" ("id", "nombre", "descripcion", "precio", "stock", "fecha_vencimiento", "fecha_creacion", "fecha_modificacion", "activo") VALUES (46, 'Agua Oxigenada 120ml', 'Antiséptico para heridas', 3.00, 150, '2027-08-10', '2025-11-22 18:19:00.446985', '2025-11-22 18:19:00.446985', true);
INSERT INTO "public"."productos" ("id", "nombre", "descripcion", "precio", "stock", "fecha_vencimiento", "fecha_creacion", "fecha_modificacion", "activo") VALUES (47, 'Ensure Advance Vainilla', 'Suplemento nutricional para adultos', 68.00, 25, '2026-09-30', '2025-11-22 18:19:00.446985', '2025-11-22 18:19:00.446985', true);
INSERT INTO "public"."productos" ("id", "nombre", "descripcion", "precio", "stock", "fecha_vencimiento", "fecha_creacion", "fecha_modificacion", "activo") VALUES (48, 'Preservativos Durex x3', 'Caja de 3 unidades texturizados', 7.00, 100, '2028-12-12', '2025-11-22 18:19:00.446985', '2025-11-22 18:19:00.446985', true);
INSERT INTO "public"."productos" ("id", "nombre", "descripcion", "precio", "stock", "fecha_vencimiento", "fecha_creacion", "fecha_modificacion", "activo") VALUES (49, 'Prueba de Embarazo', 'Test rápido de orina', 15.00, 80, '2027-03-15', '2025-11-22 18:19:00.446985', '2025-11-22 18:19:00.446985', true);


--
-- TOC entry 4933 (class 0 OID 16656)
-- Dependencies: 218
-- Data for Name: usuarios; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO "public"."usuarios" ("id", "username", "password_hash", "rol", "fecha_creacion") VALUES (1, 'admin', '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', 'ADMINISTRADOR', '2025-11-21 16:33:41.543318');
INSERT INTO "public"."usuarios" ("id", "username", "password_hash", "rol", "fecha_creacion") VALUES (2, 'trabajador1', '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', 'TRABAJADOR', '2025-11-21 16:33:41.543318');
INSERT INTO "public"."usuarios" ("id", "username", "password_hash", "rol", "fecha_creacion") VALUES (5, 'trabajador2', '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', 'TRABAJADOR', '2025-11-22 14:29:31.320332');
INSERT INTO "public"."usuarios" ("id", "username", "password_hash", "rol", "fecha_creacion") VALUES (7, 'pepito', '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', 'TRABAJADOR', '2025-11-22 19:11:40.417589');


--
-- TOC entry 4937 (class 0 OID 16684)
-- Dependencies: 222
-- Data for Name: ventas; Type: TABLE DATA; Schema: public; Owner: postgres
--

INSERT INTO "public"."ventas" ("id", "producto_id", "usuario_id", "cantidad", "precio_unitario", "total", "fecha_venta") VALUES (1, 7, 1, 1, 4.20, 4.20, '2025-11-21 18:07:01.224161');
INSERT INTO "public"."ventas" ("id", "producto_id", "usuario_id", "cantidad", "precio_unitario", "total", "fecha_venta") VALUES (2, 3, 1, 2, 12.50, 25.00, '2025-11-21 18:07:01.239867');
INSERT INTO "public"."ventas" ("id", "producto_id", "usuario_id", "cantidad", "precio_unitario", "total", "fecha_venta") VALUES (3, 7, 1, 2, 4.20, 8.40, '2025-11-21 18:07:57.039246');
INSERT INTO "public"."ventas" ("id", "producto_id", "usuario_id", "cantidad", "precio_unitario", "total", "fecha_venta") VALUES (4, 3, 1, 1, 12.50, 12.50, '2025-11-21 18:07:57.043572');
INSERT INTO "public"."ventas" ("id", "producto_id", "usuario_id", "cantidad", "precio_unitario", "total", "fecha_venta") VALUES (5, 1, 1, 5, 5.50, 27.50, '2025-11-21 19:56:31.962472');
INSERT INTO "public"."ventas" ("id", "producto_id", "usuario_id", "cantidad", "precio_unitario", "total", "fecha_venta") VALUES (6, 6, 2, 3, 6.50, 19.50, '2025-11-21 21:23:31.460083');
INSERT INTO "public"."ventas" ("id", "producto_id", "usuario_id", "cantidad", "precio_unitario", "total", "fecha_venta") VALUES (7, 7, 2, 4, 4.20, 16.80, '2025-11-21 22:18:06.4441');
INSERT INTO "public"."ventas" ("id", "producto_id", "usuario_id", "cantidad", "precio_unitario", "total", "fecha_venta") VALUES (8, 7, 2, 3, 4.20, 12.60, '2025-11-21 23:32:47.429724');
INSERT INTO "public"."ventas" ("id", "producto_id", "usuario_id", "cantidad", "precio_unitario", "total", "fecha_venta") VALUES (9, 7, 2, 1, 4.20, 4.20, '2025-11-22 00:44:55.60298');
INSERT INTO "public"."ventas" ("id", "producto_id", "usuario_id", "cantidad", "precio_unitario", "total", "fecha_venta") VALUES (10, 18, 2, 3, 6.00, 18.00, '2025-11-22 00:45:46.980826');
INSERT INTO "public"."ventas" ("id", "producto_id", "usuario_id", "cantidad", "precio_unitario", "total", "fecha_venta") VALUES (11, 1, 1, 3, 5.50, 16.50, '2025-11-22 08:17:16.75566');
INSERT INTO "public"."ventas" ("id", "producto_id", "usuario_id", "cantidad", "precio_unitario", "total", "fecha_venta") VALUES (12, 1, 2, 2, 5.50, 11.00, '2025-11-22 14:22:07.242513');
INSERT INTO "public"."ventas" ("id", "producto_id", "usuario_id", "cantidad", "precio_unitario", "total", "fecha_venta") VALUES (13, 1, 7, 5, 5.50, 27.50, '2025-11-22 19:14:46.043614');


--
-- TOC entry 4948 (class 0 OID 0)
-- Dependencies: 219
-- Name: productos_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('"public"."productos_id_seq"', 49, true);


--
-- TOC entry 4949 (class 0 OID 0)
-- Dependencies: 217
-- Name: usuarios_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('"public"."usuarios_id_seq"', 7, true);


--
-- TOC entry 4950 (class 0 OID 0)
-- Dependencies: 221
-- Name: ventas_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('"public"."ventas_id_seq"', 13, true);


--
-- TOC entry 4779 (class 2606 OID 16680)
-- Name: productos productos_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY "public"."productos"
    ADD CONSTRAINT "productos_pkey" PRIMARY KEY ("id");


--
-- TOC entry 4772 (class 2606 OID 16663)
-- Name: usuarios usuarios_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY "public"."usuarios"
    ADD CONSTRAINT "usuarios_pkey" PRIMARY KEY ("id");


--
-- TOC entry 4774 (class 2606 OID 16665)
-- Name: usuarios usuarios_username_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY "public"."usuarios"
    ADD CONSTRAINT "usuarios_username_key" UNIQUE ("username");


--
-- TOC entry 4782 (class 2606 OID 16691)
-- Name: ventas ventas_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY "public"."ventas"
    ADD CONSTRAINT "ventas_pkey" PRIMARY KEY ("id");


--
-- TOC entry 4775 (class 1259 OID 16730)
-- Name: idx_productos_activo; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX "idx_productos_activo" ON "public"."productos" USING "btree" ("activo");


--
-- TOC entry 4776 (class 1259 OID 16681)
-- Name: idx_productos_nombre; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX "idx_productos_nombre" ON "public"."productos" USING "btree" ("nombre");


--
-- TOC entry 4777 (class 1259 OID 16682)
-- Name: idx_productos_vencimiento; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX "idx_productos_vencimiento" ON "public"."productos" USING "btree" ("fecha_vencimiento");


--
-- TOC entry 4770 (class 1259 OID 16666)
-- Name: idx_usuarios_username; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX "idx_usuarios_username" ON "public"."usuarios" USING "btree" ("username");


--
-- TOC entry 4780 (class 1259 OID 16702)
-- Name: idx_ventas_fecha; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX "idx_ventas_fecha" ON "public"."ventas" USING "btree" ("fecha_venta");


--
-- TOC entry 4785 (class 2620 OID 16708)
-- Name: ventas trigger_actualizar_stock; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER "trigger_actualizar_stock" AFTER INSERT ON "public"."ventas" FOR EACH ROW EXECUTE FUNCTION "public"."actualizar_stock_venta"();


--
-- TOC entry 4783 (class 2606 OID 16692)
-- Name: ventas ventas_producto_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY "public"."ventas"
    ADD CONSTRAINT "ventas_producto_id_fkey" FOREIGN KEY ("producto_id") REFERENCES "public"."productos"("id");


--
-- TOC entry 4784 (class 2606 OID 16697)
-- Name: ventas ventas_usuario_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY "public"."ventas"
    ADD CONSTRAINT "ventas_usuario_id_fkey" FOREIGN KEY ("usuario_id") REFERENCES "public"."usuarios"("id");


-- Completed on 2025-11-22 22:18:18

--
-- PostgreSQL database dump complete
--

