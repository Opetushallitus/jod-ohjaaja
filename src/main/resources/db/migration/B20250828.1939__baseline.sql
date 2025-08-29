-- Ohjaaja Database Baseline

CREATE TABLE ohjaaja.artikkelin_katselu
(
  maara         integer                NOT NULL,
  paiva         date                   NOT NULL,
  artikkeli_erc character varying(255) NOT NULL
);

CREATE TABLE ohjaaja.artikkelin_kommentin_ilmianto
(
  maara                   integer                     NOT NULL,
  tunnistautunut          boolean                     NOT NULL,
  viimeksi_ilmiannettu    timestamp(6) with time zone NOT NULL,
  artikkelin_kommentti_id uuid                        NOT NULL
);

CREATE TABLE ohjaaja.artikkelin_kommentti
(
  luotu         timestamp(6) with time zone NOT NULL,
  id            uuid                        NOT NULL,
  ohjaaja_id    uuid,
  kommentti     character varying(2000)     NOT NULL,
  artikkeli_erc character varying(255)      NOT NULL
);

CREATE TABLE ohjaaja.feature_flag
(
  enabled    boolean                     NOT NULL,
  feature    smallint                    NOT NULL,
  updated_at timestamp(6) with time zone NOT NULL,
  updated_by character varying(255)      NOT NULL,
  CONSTRAINT feature_flag_feature_check CHECK (((feature >= 0) AND (feature <= 0)))
);

CREATE TABLE ohjaaja.ohjaaja
(
  id                 uuid NOT NULL,
  tyoskentely_paikka character varying(255),
  CONSTRAINT ohjaaja_tyoskentely_paikka_check CHECK (((tyoskentely_paikka)::text = ANY
                                                      ((ARRAY['PERUSASTE':: character varying, 'TOINEN_ASTE':: character varying, 'KORKEAKOULU':: character varying, 'AIKUISKOULUTUS':: character varying, 'TYOLLISYYSPALVELUT':: character varying, 'KOLMAS_SEKTORI':: character varying, 'YKSITYINEN':: character varying, 'MUU':: character varying])::text[])
) )
);

CREATE TABLE ohjaaja.ohjaajan_kiinnostus
(
  asiasana_id bigint                      NOT NULL,
  luotu       timestamp(6) with time zone NOT NULL,
  id          uuid                        NOT NULL,
  ohjaaja_id  uuid                        NOT NULL
);

CREATE TABLE ohjaaja.ohjaajan_suosikki
(
  luotu         timestamp(6) with time zone NOT NULL,
  id            uuid                        NOT NULL,
  ohjaaja_id    uuid                        NOT NULL,
  artikkeli_erc character varying(255)      NOT NULL
);

CREATE TABLE ohjaaja.viimeksi_katseltu_artikkeli
(
  id                bigint                      NOT NULL,
  viimeksi_katseltu timestamp(6) with time zone NOT NULL,
  ohjaaja_id        uuid                        NOT NULL,
  artikkeli_erc     character varying(255)      NOT NULL
);

CREATE SEQUENCE ohjaaja.viimeksi_katseltu_artikkeli_seq
  START WITH 1
  INCREMENT BY 50
  NO MINVALUE
  NO MAXVALUE CACHE 1;

ALTER TABLE ONLY ohjaaja.artikkelin_katselu
  ADD CONSTRAINT artikkelin_katselu_pkey PRIMARY KEY (paiva, artikkeli_erc);

ALTER TABLE ONLY ohjaaja.artikkelin_kommentin_ilmianto
  ADD CONSTRAINT artikkelin_kommentin_ilmianto_pkey PRIMARY KEY (tunnistautunut, artikkelin_kommentti_id);

ALTER TABLE ONLY ohjaaja.artikkelin_kommentti
  ADD CONSTRAINT artikkelin_kommentti_pkey PRIMARY KEY (id);

ALTER TABLE ONLY ohjaaja.feature_flag
  ADD CONSTRAINT feature_flag_pkey PRIMARY KEY (feature);

ALTER TABLE ONLY ohjaaja.ohjaaja
  ADD CONSTRAINT ohjaaja_pkey PRIMARY KEY (id);

ALTER TABLE ONLY ohjaaja.ohjaajan_kiinnostus
  ADD CONSTRAINT ohjaajan_kiinnostus_pkey PRIMARY KEY (id);

ALTER TABLE ONLY ohjaaja.ohjaajan_suosikki
  ADD CONSTRAINT ohjaajan_suosikki_pkey PRIMARY KEY (id);

ALTER TABLE ONLY ohjaaja.viimeksi_katseltu_artikkeli
  ADD CONSTRAINT viimeksi_katseltu_artikkeli_artikkeli_erc_ohjaaja_id_key UNIQUE (artikkeli_erc, ohjaaja_id);

ALTER TABLE ONLY ohjaaja.viimeksi_katseltu_artikkeli
  ADD CONSTRAINT viimeksi_katseltu_artikkeli_pkey PRIMARY KEY (id);

CREATE INDEX idx4f3csqfq9ahrrtk5wcygv2a2u ON ohjaaja.artikkelin_kommentti USING btree (ohjaaja_id);

CREATE INDEX idx4s74j0wtucg24v939bkejswjx ON ohjaaja.viimeksi_katseltu_artikkeli USING btree (ohjaaja_id);

CREATE INDEX idx8y7bwn65bajrkhbh8mokeba3x ON ohjaaja.ohjaajan_suosikki USING btree (ohjaaja_id);

CREATE INDEX idxbney0t7fx95fbo3dt6m35a5e6 ON ohjaaja.ohjaajan_kiinnostus USING btree (ohjaaja_id);

CREATE INDEX idxilyg4am7rqsmc9cwxv3qf4xao ON ohjaaja.artikkelin_kommentti USING btree (artikkeli_erc);

ALTER TABLE ONLY ohjaaja.ohjaajan_suosikki
  ADD CONSTRAINT fk2kgwq21vvy1e1jab4svwk30i3 FOREIGN KEY (ohjaaja_id) REFERENCES ohjaaja.ohjaaja(id);

ALTER TABLE ONLY ohjaaja.artikkelin_kommentti
  ADD CONSTRAINT fk9dur3gjb771ide78eppnv4nvd FOREIGN KEY (ohjaaja_id) REFERENCES ohjaaja.ohjaaja(id) ON
DELETE
SET NULL;

ALTER TABLE ONLY ohjaaja.artikkelin_kommentin_ilmianto
  ADD CONSTRAINT fkcih7jao10og8rtebodw2it3ec FOREIGN KEY (artikkelin_kommentti_id) REFERENCES ohjaaja.artikkelin_kommentti(id);

ALTER TABLE ONLY ohjaaja.ohjaajan_kiinnostus
  ADD CONSTRAINT fko3ol73q7q10epsmmfdhxgnuu4 FOREIGN KEY (ohjaaja_id) REFERENCES ohjaaja.ohjaaja(id);
