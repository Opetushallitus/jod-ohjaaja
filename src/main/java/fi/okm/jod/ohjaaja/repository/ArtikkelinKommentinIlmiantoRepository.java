/*
 * Copyright (c) 2025 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.repository;

import fi.okm.jod.ohjaaja.entity.ArtikkelinKommentinIlmianto;
import fi.okm.jod.ohjaaja.entity.ArtikkelinKommentinIlmiantoId;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ArtikkelinKommentinIlmiantoRepository
    extends JpaRepository<ArtikkelinKommentinIlmianto, ArtikkelinKommentinIlmiantoId> {
  @Modifying
  @Transactional
  @Query(
      value =
          """
          INSERT INTO artikkelin_kommentin_ilmianto (artikkelin_kommentti_id, tunnistautunut, maara, viimeksi_ilmiannettu)
          VALUES (:artikkelinKommenttiId, :tunnistautunut, 1, CURRENT_TIMESTAMP)
          ON CONFLICT (artikkelin_kommentti_id, tunnistautunut)
          DO UPDATE SET maara = artikkelin_kommentin_ilmianto.maara + 1, viimeksi_ilmiannettu = CURRENT_TIMESTAMP
          """,
      nativeQuery = true)
  void upsertIlmianto(UUID artikkelinKommenttiId, boolean tunnistautunut);

  List<ArtikkelinKommentinIlmianto> findByArtikkelinKommenttiId(UUID artikkelinKommenttiId);
}
