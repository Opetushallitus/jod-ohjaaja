DO
$$
BEGIN
ALTER TABLE ONLY ohjaaja.ohjaaja
  ADD CONSTRAINT fk_ohjaaja_id FOREIGN KEY (id) REFERENCES tunnistus.henkilo (ohjaaja_id);
EXCEPTION
    WHEN duplicate_object THEN
      RAISE NOTICE 'Foreign key constraint fk_ohjaaja_id already exists, ignoring.';
END
$$ LANGUAGE plpgsql;
