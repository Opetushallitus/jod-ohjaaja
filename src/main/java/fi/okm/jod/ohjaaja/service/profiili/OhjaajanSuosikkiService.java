/*
 * Copyright (c) 2025 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.service.profiili;

import fi.okm.jod.ohjaaja.config.logging.LogMarker;
import fi.okm.jod.ohjaaja.domain.JodUser;
import fi.okm.jod.ohjaaja.dto.profiili.SuosikkiDto;
import fi.okm.jod.ohjaaja.entity.OhjaajanSuosikki;
import fi.okm.jod.ohjaaja.repository.OhjaajaRepository;
import fi.okm.jod.ohjaaja.repository.OhjaajanSuosikkiRepository;
import fi.okm.jod.ohjaaja.service.NotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class OhjaajanSuosikkiService {
  private final OhjaajaRepository ohjaajat;
  private final OhjaajanSuosikkiRepository suosikit;

  @Transactional(readOnly = true)
  public List<SuosikkiDto> findAll(JodUser user) {
    var ohjaaja = ohjaajat.getReferenceById(user.getId());
    return suosikit.findByOhjaaja(ohjaaja).stream()
        .map(
            suosikki ->
                new SuosikkiDto(suosikki.getId(), suosikki.getArtikkeliErc(), suosikki.getLuotu()))
        .toList();
  }

  public UUID add(JodUser user, String artikkeliErc) {
    var ohjaaja = ohjaajat.getReferenceById(user.getId());
    var id =
        suosikit
            .findByOhjaajaAndArtikkeliErc(ohjaaja, artikkeliErc)
            .map(OhjaajanSuosikki::getId)
            .orElseGet(() -> suosikit.save(new OhjaajanSuosikki(artikkeliErc, ohjaaja)).getId());
    log.atInfo()
        .addMarker(LogMarker.AUDIT)
        .addKeyValue("userId", user.getId())
        .log("Favorite added");
    return id;
  }

  public void delete(JodUser user, UUID id) {
    if (suosikit.deleteByOhjaajaAndId(ohjaajat.getReferenceById(user.getId()), id) == 0) {
      throw new NotFoundException("Suosikki not found");
    }
    log.atInfo()
        .addMarker(LogMarker.AUDIT)
        .addKeyValue("userId", user.getId())
        .log("Favorite deleted");
  }
}
