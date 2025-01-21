--
-- Additional schema definitions
-- Eventually replaced by a migration tool
--
ALTER TABLE ohjaaja
DROP CONSTRAINT IF EXISTS fk_ohjaaja_id,
  ADD CONSTRAINT fk_ohjaaja_id FOREIGN KEY (id) REFERENCES tunnistus.henkilo (ohjaaja_id)
;;;
