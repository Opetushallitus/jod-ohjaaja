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
import fi.okm.jod.ohjaaja.repository.projection.SummaPerArtikkeli;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.Collection;
import javax.annotation.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

  @Query(
      """
      SELECT ak.artikkeliId AS artikkeliId, SUM(ak.maara) AS summa
      FROM ArtikkelinKatselu ak
      WHERE (:artikkeliIds IS NULL OR ak.artikkeliId IN :artikkeliIds)
      GROUP BY ak.artikkeliId
      """)
  Page<SummaPerArtikkeli> findSumKatselut(
      @Nullable Collection<Long> artikkeliIds, Pageable pageable);
}
