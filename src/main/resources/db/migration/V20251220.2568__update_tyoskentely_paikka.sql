ALTER TABLE ohjaaja.ohjaaja
DROP CONSTRAINT ohjaaja_tyoskentely_paikka_check;

UPDATE ohjaaja.ohjaaja
SET tyoskentely_paikka = 'OPPILAITOS_MUU'
WHERE tyoskentely_paikka = 'AIKUISKOULUTUS';

ALTER TABLE ohjaaja.ohjaaja
  ADD CONSTRAINT ohjaaja_tyoskentely_paikka_check CHECK (
    (tyoskentely_paikka)::text = ANY (
  (ARRAY[
  'PERUSASTE'::character varying,
  'TOINEN_ASTE'::character varying,
  'KORKEAKOULU'::character varying,
  'OPPILAITOS_MUU'::character varying,
  'TYOLLISYYSPALVELUT'::character varying,
  'KOLMAS_SEKTORI'::character varying,
  'YKSITYINEN'::character varying,
  'MUU'::character varying
  ])::text[]
  )
  );
