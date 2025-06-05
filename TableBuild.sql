-- Table: public.Satellite

-- DROP TABLE IF EXISTS public."Satellite";

CREATE TABLE IF NOT EXISTS public."Satellite"
(
    "Satellite_ID" integer NOT NULL DEFAULT nextval('"Satellite_Satellite_ID_seq"'::regclass),
    "SatelliteName" text COLLATE pg_catalog."default",
    "User_ID" bigint,
    "Mass" bigint,
    "Altitude" bigint,
    "Speed" bigint,
    "Area" bigint,
    CONSTRAINT "Satellite_pkey" PRIMARY KEY ("Satellite_ID")
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS public."Satellite"
    OWNER to postgres;

REVOKE ALL ON TABLE public."Satellite" FROM satellite_user;

GRANT ALL ON TABLE public."Satellite" TO postgres;

GRANT DELETE, UPDATE, INSERT, SELECT ON TABLE public."Satellite" TO satellite_user;
