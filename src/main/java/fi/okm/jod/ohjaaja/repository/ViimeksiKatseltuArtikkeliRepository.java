/*
 * Copyright (c) 2025 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.repository;

import fi.okm.jod.ohjaaja.entity.ViimeksiKatseltuArtikkeli;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ViimeksiKatseltuArtikkeliRepository
    extends JpaRepository<ViimeksiKatseltuArtikkeli, Long> {
  Optional<ViimeksiKatseltuArtikkeli> findByArtikkeliErcAndOhjaajaId(
      String artikkeliErc, UUID ohjaajaId);

  Page<ViimeksiKatseltuArtikkeli> findByOhjaajaIdOrderByViimeksiKatseltuDesc(
      UUID ohjaajaId, Pageable pageable);
}
