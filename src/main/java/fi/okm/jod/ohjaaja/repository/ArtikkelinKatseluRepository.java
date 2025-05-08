/*
 * Copyright (c) 2025 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.repository;

import fi.okm.jod.ohjaaja.entity.ArtikkelinKatselu;
import fi.okm.jod.ohjaaja.entity.ArtikkelinKatseluId;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ArtikkelinKatseluRepository
    extends JpaRepository<ArtikkelinKatselu, ArtikkelinKatseluId> {
  @Modifying
  @Transactional
  @Query(
      value =
          """
          INSERT INTO artikkelin_katselu (artikkeli_id, paiva, maara)
          VALUES (:artikkeliId, :paiva, 1)
          ON CONFLICT (artikkeli_id, paiva)
          DO UPDATE SET maara = artikkelin_katselu.maara + 1
          """,
      nativeQuery = true)
  void upsertKatselu(Long artikkeliId, LocalDate paiva);
}
