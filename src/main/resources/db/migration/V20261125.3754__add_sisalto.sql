-- Content type (artikkeli or uutinen) per external reference code (ERC).
-- No foreign keys from other tables: a missing row is treated as ARTIKKELI.
CREATE TABLE ohjaaja.sisalto
(
  erc           character varying(255)      NOT NULL,
  sisaltotyyppi character varying(255)      NOT NULL,
  paivitetty    timestamp(6) with time zone NOT NULL,
  CONSTRAINT sisalto_pkey PRIMARY KEY (erc),
  CONSTRAINT sisalto_sisaltotyyppi_check CHECK (
    (sisaltotyyppi)::text = ANY ((ARRAY['ARTIKKELI'::character varying, 'UUTINEN'::character varying])::text[])
  )
);

-- All existing content is articles (news are not yet published).
INSERT INTO ohjaaja.sisalto (erc, sisaltotyyppi, paivitetty)
SELECT erc, 'ARTIKKELI', now()
FROM (
  SELECT artikkeli_erc AS erc FROM ohjaaja.artikkelin_katselu
  UNION
  SELECT artikkeli_erc FROM ohjaaja.viimeksi_katseltu_artikkeli
  UNION
  SELECT artikkeli_erc FROM ohjaaja.ohjaajan_suosikki
  UNION
  SELECT artikkeli_erc FROM ohjaaja.artikkelin_kommentti
) ercs
ON CONFLICT (erc) DO NOTHING;

-- New feature flag NEWS (ordinal 1)
ALTER TABLE ohjaaja.feature_flag
  DROP CONSTRAINT feature_flag_feature_check;

ALTER TABLE ohjaaja.feature_flag
  ADD CONSTRAINT feature_flag_feature_check CHECK (((feature >= 0) AND (feature <= 1)));
