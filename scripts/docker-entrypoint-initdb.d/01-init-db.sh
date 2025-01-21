#!/bin/bash

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<'EOSQL'
    CREATE ROLE ohjaaja WITH LOGIN PASSWORD 'ohjaaja';
    GRANT CONNECT,CREATE,TEMPORARY ON DATABASE ohjaaja TO ohjaaja;
EOSQL

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "ohjaaja" <<'EOSQL'
    BEGIN;
    CREATE SCHEMA IF NOT EXISTS ohjaaja;
    GRANT ALL PRIVILEGES ON SCHEMA ohjaaja TO ohjaaja;
    CREATE ROLE tunnistus;
    CREATE SCHEMA IF NOT EXISTS tunnistus AUTHORIZATION tunnistus;
    REVOKE ALL ON SCHEMA tunnistus FROM public;
    SET LOCAL ROLE tunnistus;
    CREATE TABLE IF NOT EXISTS henkilo(ohjaaja_id UUID PRIMARY KEY, henkilo_id VARCHAR(300) NOT NULL UNIQUE);

    CREATE OR REPLACE FUNCTION generate_ohjaaja_id(henkilo_id VARCHAR(300)) RETURNS UUID AS $$
    DECLARE
      id UUID;
    BEGIN
      INSERT INTO henkilo(ohjaaja_id, henkilo_id) VALUES (gen_random_uuid(), $1) ON CONFLICT DO NOTHING RETURNING ohjaaja_id INTO id;
      IF id IS NULL THEN
        SELECT h.ohjaaja_id FROM henkilo h WHERE h.henkilo_id = $1 INTO id;
      END IF;
      RETURN id;
    END $$ LANGUAGE PLPGSQL SECURITY DEFINER SET search_path = tunnistus, pg_temp;
    REVOKE ALL ON FUNCTION generate_ohjaaja_id FROM public;
    GRANT EXECUTE ON FUNCTION generate_ohjaaja_id TO ohjaaja;

    CREATE OR REPLACE FUNCTION remove_ohjaaja_id(ohjaaja_id UUID) RETURNS UUID AS $$
            DELETE FROM henkilo WHERE ohjaaja_id = $1 RETURNING ohjaaja_id
            $$ LANGUAGE SQL SECURITY DEFINER SET search_path = tunnistus, pg_temp;
    REVOKE ALL ON FUNCTION remove_ohjaaja_id FROM public;
    GRANT EXECUTE ON FUNCTION remove_ohjaaja_id TO ohjaaja;

    GRANT REFERENCES(ohjaaja_id) ON henkilo TO ohjaaja;
    GRANT USAGE ON SCHEMA tunnistus TO ohjaaja;

    -- Workaround for Hibernate ddl-auto
    ALTER TABLE henkilo ENABLE ROW LEVEL SECURITY;
    CREATE POLICY select_none_policy ON henkilo FOR SELECT TO ohjaaja USING (false);
    GRANT SELECT(ohjaaja_id) ON henkilo TO ohjaaja;

    RESET ROLE;
    END;
EOSQL
