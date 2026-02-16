/*
 * Copyright (c) 2025 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.repository;

import fi.okm.jod.ohjaaja.entity.ArtikkelinKommentti;
import fi.okm.jod.ohjaaja.entity.Ohjaaja;
import fi.okm.jod.ohjaaja.repository.projection.KommentitPerArtikkelit;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArtikkelinKommenttiRepository extends JpaRepository<ArtikkelinKommentti, UUID> {

  Page<ArtikkelinKommentti> findByArtikkeliErc(String artikkeliErc, Pageable pageable);

  @Query(
      """
      SELECT k.artikkeliErc as artikkeliErc, MAX(k.luotu) as uusinKommenttiAika, MIN(k.luotu) as vanhinKommenttiAika, COUNT(k) as kommenttiMaara
      FROM ArtikkelinKommentti k
      WHERE k.ohjaaja = :ohjaaja
      GROUP BY k.artikkeliErc
      ORDER BY MAX(k.luotu) DESC
      """)
  Page<KommentitPerArtikkelit> findKommentoidutArtikkelitByOhjaaja(
      @Param("ohjaaja") Ohjaaja ohjaaja, Pageable pageable);
}
