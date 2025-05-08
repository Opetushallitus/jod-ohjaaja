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
import fi.okm.jod.ohjaaja.dto.SivuDto;
import fi.okm.jod.ohjaaja.entity.ViimeksiKatseltuArtikkeli;
import fi.okm.jod.ohjaaja.repository.ArtikkelinKatseluRepository;
import fi.okm.jod.ohjaaja.repository.OhjaajaRepository;
import fi.okm.jod.ohjaaja.repository.ViimeksiKatseltuArtikkeliRepository;
import java.time.Instant;
import java.time.LocalDate;
import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ArtikkelinKatseluService {
  private final ArtikkelinKatseluRepository artikkelinKatselut;
  private final ViimeksiKatseltuArtikkeliRepository viimeksiKatseltuArtikkeli;
  private final OhjaajaRepository ohjaajat;

  public void add(@Nullable JodUser jodUser, Long artikkeliId) {
    LocalDate paiva = LocalDate.now();

    artikkelinKatselut.upsertKatselu(artikkeliId, paiva);

    if (jodUser != null) {
      var ohjaaja = ohjaajat.getReferenceById(jodUser.getId());

      viimeksiKatseltuArtikkeli
          .findByArtikkeliIdAndOhjaajaId(artikkeliId, ohjaaja.getId())
          .ifPresentOrElse(
              existing -> {
                existing.setViimeksiKatseltu(Instant.now());
                viimeksiKatseltuArtikkeli.save(existing);
              },
              () -> {
                var uusiViimeksiKatseltu =
                    new ViimeksiKatseltuArtikkeli(artikkeliId, ohjaaja.getId());
                viimeksiKatseltuArtikkeli.save(uusiViimeksiKatseltu);
              });
    }
  }

  public SivuDto<Long> findMostRecentViewedArtikkeliIdsByUser(JodUser user, Pageable pageable) {
    var ohjaajaId = ohjaajat.getReferenceById(user.getId()).getId();
    return new SivuDto<>(
        viimeksiKatseltuArtikkeli
            .findByOhjaajaIdOrderByViimeksiKatseltuDesc(ohjaajaId, pageable)
            .map(ViimeksiKatseltuArtikkeli::getArtikkeliId));
  }
}
