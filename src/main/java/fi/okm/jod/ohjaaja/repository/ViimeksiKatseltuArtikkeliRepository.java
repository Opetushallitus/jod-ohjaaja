/*
 * Copyright (c) 2025 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.repository;

import fi.okm.jod.ohjaaja.entity.SisaltoTyyppi;
import fi.okm.jod.ohjaaja.entity.ViimeksiKatseltuArtikkeli;
import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ViimeksiKatseltuArtikkeliRepository
    extends JpaRepository<ViimeksiKatseltuArtikkeli, Long> {

  @Modifying(flushAutomatically = true)
  @Query(
      value =
          """
          INSERT INTO viimeksi_katseltu_artikkeli (id, artikkeli_erc, ohjaaja_id, viimeksi_katseltu)
          VALUES (nextval('viimeksi_katseltu_artikkeli_seq'), :artikkeliErc, :ohjaajaId,
                  :viimeksiKatseltu)
          ON CONFLICT (artikkeli_erc, ohjaaja_id)
          DO UPDATE SET viimeksi_katseltu =
            GREATEST(viimeksi_katseltu_artikkeli.viimeksi_katseltu, EXCLUDED.viimeksi_katseltu)
          """,
      nativeQuery = true)
  void upsert(String artikkeliErc, UUID ohjaajaId, Instant viimeksiKatseltu);

  Optional<ViimeksiKatseltuArtikkeli> findByArtikkeliErcAndOhjaajaId(
      String artikkeliErc, UUID ohjaajaId);

  @Query(
      """
      SELECT v FROM ViimeksiKatseltuArtikkeli v
      LEFT JOIN Sisalto s ON s.erc = v.artikkeliErc
      WHERE v.ohjaajaId = :ohjaajaId
        AND COALESCE(s.sisaltotyyppi, fi.okm.jod.ohjaaja.entity.SisaltoTyyppi.ARTIKKELI) IN :tyypit
      ORDER BY v.viimeksiKatseltu DESC
      """)
  Page<ViimeksiKatseltuArtikkeli> findByOhjaajaIdAndTyypit(
      UUID ohjaajaId, Collection<SisaltoTyyppi> tyypit, Pageable pageable);
}
