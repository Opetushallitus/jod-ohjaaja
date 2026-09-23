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
import fi.okm.jod.ohjaaja.entity.OhjaajanKiinnostus;
import fi.okm.jod.ohjaaja.repository.projection.KiinnostusMaara;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OhjaajanKiinnostusRepository extends JpaRepository<OhjaajanKiinnostus, UUID> {
  long deleteByOhjaajaAndId(Ohjaaja ohjaaja, UUID id);

  List<OhjaajanKiinnostus> findByOhjaaja(Ohjaaja ohjaaja);

  Optional<OhjaajanKiinnostus> findByOhjaajaAndAsiasanaId(Ohjaaja ohjaaja, Long asiasanaId);

  @Query(
      """
      SELECT k.asiasanaId AS asiasanaId, COUNT(k) AS maara
      FROM OhjaajanKiinnostus k
      GROUP BY k.asiasanaId
      ORDER BY COUNT(k) DESC, k.asiasanaId
      """)
  List<KiinnostusMaara> countByAsiasanaId();
}
