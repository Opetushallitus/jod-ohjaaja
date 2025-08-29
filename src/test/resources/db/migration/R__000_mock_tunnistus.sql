-- mock tunnistus schema for tests
CREATE SCHEMA IF NOT EXISTS tunnistus;

CREATE TABLE IF NOT EXISTS tunnistus.henkilo (
   ohjaaja_id  UUID PRIMARY KEY,
   henkilo_id VARCHAR(300) NOT NULL UNIQUE
);

CREATE OR REPLACE FUNCTION tunnistus.generate_ohjaaja_id(henkilo_id VARCHAR(300)) RETURNS UUID AS
$$
DECLARE
  id UUID;
BEGIN
  INSERT INTO tunnistus.henkilo(ohjaaja_id, henkilo_id)
  VALUES (gen_random_uuid(), $1)
  ON CONFLICT DO NOTHING
  RETURNING ohjaaja_id INTO id;
  IF id IS NULL THEN
    SELECT h.ohjaaja_id FROM tunnistus.henkilo h WHERE h.henkilo_id = $1 INTO id;
  END IF;
  RETURN id;
END
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE FUNCTION tunnistus.remove_ohjaaja_id(ohjaaja_id UUID) RETURNS UUID AS
$$
DELETE
FROM tunnistus.henkilo
WHERE ohjaaja_id = $1
RETURNING ohjaaja_id
$$ LANGUAGE SQL;

