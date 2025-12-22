DELETE FROM ohjaaja.viimeksi_katseltu_artikkeli vka
WHERE NOT EXISTS (
  SELECT 1 FROM ohjaaja.ohjaaja o WHERE o.id = vka.ohjaaja_id
);

ALTER TABLE ONLY ohjaaja.viimeksi_katseltu_artikkeli
  ADD CONSTRAINT fk_viimeksi_katseltu_artikkeli_ohjaaja FOREIGN KEY (ohjaaja_id) REFERENCES ohjaaja.ohjaaja(id) ON DELETE CASCADE;
