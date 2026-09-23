/*
 * Copyright (c) 2026 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.service;

import fi.okm.jod.ohjaaja.dto.ArtikkeliMaaraDto;
import fi.okm.jod.ohjaaja.dto.KiinnostusMaaraDto;
import fi.okm.jod.ohjaaja.dto.TilastotDto;
import fi.okm.jod.ohjaaja.dto.TyoskentelyPaikkaMaaraDto;
import fi.okm.jod.ohjaaja.entity.TyoskentelyPaikka;
import fi.okm.jod.ohjaaja.repository.ArtikkelinKatseluRepository;
import fi.okm.jod.ohjaaja.repository.ArtikkelinKommenttiRepository;
import fi.okm.jod.ohjaaja.repository.OhjaajaRepository;
import fi.okm.jod.ohjaaja.repository.OhjaajanKiinnostusRepository;
import fi.okm.jod.ohjaaja.repository.OhjaajanSuosikkiRepository;
import fi.okm.jod.ohjaaja.repository.projection.SummaPerArtikkeli;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TilastoService {

  // Bounds used when the time range is open. Must stay within PostgreSQL date/timestamp range.
  private static final LocalDate MIN_PAIVA = LocalDate.EPOCH;
  private static final LocalDate MAX_PAIVA = LocalDate.of(9999, 12, 31);

  private final OhjaajaRepository ohjaajaRepository;
  private final OhjaajanKiinnostusRepository kiinnostusRepository;
  private final OhjaajanSuosikkiRepository suosikkiRepository;
  private final ArtikkelinKommenttiRepository kommenttiRepository;
  private final ArtikkelinKatseluRepository katseluRepository;

  public TilastotDto getTilastot(@Nullable LocalDate alku, @Nullable LocalDate loppu, int top) {
    if (alku != null && loppu != null && alku.isAfter(loppu)) {
      throw new ServiceValidationException("alku must not be after loppu");
    }

    var alkuPaiva = alku != null ? alku : MIN_PAIVA;
    var loppuPaiva = loppu != null ? loppu : MAX_PAIVA;
    // Article views are recorded per day in the system default time zone (see
    // ArtikkelinKatseluService), so use the same zone for timestamp-based data.
    var zone = ZoneId.systemDefault();
    Instant alkuAika = alkuPaiva.atStartOfDay(zone).toInstant();
    Instant loppuAika = loppuPaiva.plusDays(1).atStartOfDay(zone).toInstant();
    var sivu = PageRequest.of(0, top);

    return new TilastotDto(
        alku,
        loppu,
        ohjaajaRepository.count(),
        getTyoskentelyPaikat(),
        kiinnostusRepository.countByAsiasanaId().stream()
            .map(k -> new KiinnostusMaaraDto(k.getAsiasanaId(), k.getMaara()))
            .toList(),
        toDto(suosikkiRepository.findMostFavorited(alkuAika, loppuAika, sivu)),
        toDto(kommenttiRepository.findMostCommented(alkuAika, loppuAika, sivu)),
        toDto(katseluRepository.findMostViewed(alkuPaiva, loppuPaiva, sivu)));
  }

  /** Returns the count for every workplace (including zero counts), and for unset (null). */
  private List<TyoskentelyPaikkaMaaraDto> getTyoskentelyPaikat() {
    var maarat = new HashMap<TyoskentelyPaikka, Long>();
    ohjaajaRepository
        .countByTyoskentelyPaikka()
        .forEach(m -> maarat.put(m.getTyoskentelyPaikka(), m.getMaara()));
    return Stream.concat(
            Arrays.stream(TyoskentelyPaikka.values()), Stream.of((TyoskentelyPaikka) null))
        .map(paikka -> new TyoskentelyPaikkaMaaraDto(paikka, maarat.getOrDefault(paikka, 0L)))
        .toList();
  }

  private static List<ArtikkeliMaaraDto> toDto(List<SummaPerArtikkeli> summat) {
    return summat.stream()
        .map(s -> new ArtikkeliMaaraDto(s.getArtikkeliErc(), s.getSumma()))
        .toList();
  }
}
