/*
 * Copyright (c) 2025 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.repository;

import fi.okm.jod.ohjaaja.entity.Ohjaaja;
import fi.okm.jod.ohjaaja.entity.OhjaajanSuosikki;
import fi.okm.jod.ohjaaja.repository.projection.SummaPerArtikkeli;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OhjaajanSuosikkiRepository extends JpaRepository<OhjaajanSuosikki, UUID> {
  long deleteByOhjaajaAndId(Ohjaaja ohjaaja, UUID id);

  List<OhjaajanSuosikki> findByOhjaaja(Ohjaaja ohjaaja);

  Optional<OhjaajanSuosikki> findByOhjaajaAndArtikkeliErc(Ohjaaja ohjaaja, String artikkeliErc);

  @Query(
      """
      SELECT s.artikkeliErc AS artikkeliErc, COUNT(s) AS summa
      FROM OhjaajanSuosikki s
      WHERE s.luotu >= :alku AND s.luotu < :loppu
      GROUP BY s.artikkeliErc
      ORDER BY COUNT(s) DESC, s.artikkeliErc
      """)
  List<SummaPerArtikkeli> findMostFavorited(Instant alku, Instant loppu, Pageable pageable);
}
