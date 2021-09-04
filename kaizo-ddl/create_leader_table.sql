CREATE TABLE IF NOT EXISTS public.leader
(
    domain character varying(100) NOT NULL,
    updated timestamp with time zone,
    PRIMARY KEY (domain)
);