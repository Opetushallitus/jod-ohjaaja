/*
 * Copyright (c) 2025 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.service;

import fi.okm.jod.ohjaaja.domain.JodUser;
import fi.okm.jod.ohjaaja.entity.ArtikkelinKatselu;
import fi.okm.jod.ohjaaja.repository.ArtikkelinKatseluRepository;
import fi.okm.jod.ohjaaja.repository.OhjaajaRepository;
import java.util.UUID;
import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ArtikkelinKatseluService {
  private final ArtikkelinKatseluRepository artikkelinKatselut;
  private final OhjaajaRepository ohjaajat;

  public UUID add(@Nullable UUID ohjaajaId, @Nullable String anonyymiId, Long artikkeliId) {
    if (anonyymiId == null && ohjaajaId == null) {
      throw new ServiceValidationException("Either anonyymiId or ohjaajaId must be provided");
    }
    if (anonyymiId != null && ohjaajaId != null) {
      throw new ServiceValidationException(
          "Either anonyymiId or ohjaajaId must be provided, not both");
    }
    return artikkelinKatselut
        .save(new ArtikkelinKatselu(artikkeliId, ohjaajaId, anonyymiId))
        .getId();
  }

  public Page<Long> findMostRecentViewedArtikkeliIdsByUser(JodUser user, Pageable pageable) {
    var ohjaaja = ohjaajat.getReferenceById(user.getId());
    return artikkelinKatselut.findMostRecentViewedArtikkeliIdsByOhjaajaId(
        ohjaaja.getId(), pageable);
  }
}
