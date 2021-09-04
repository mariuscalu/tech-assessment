CREATE TABLE IF NOT EXISTS public.status
(
    domain character varying(100) NOT NULL,
    start timestamp with time zone,
    "end" timestamp with time zone,
    PRIMARY KEY (domain)
);