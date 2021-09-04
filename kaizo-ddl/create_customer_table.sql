CREATE TABLE IF NOT EXISTS public.customer
(
    domain character varying(100) NOT NULL,
    name character varying(200),
    token character varying(200),
    PRIMARY KEY (domain)
);