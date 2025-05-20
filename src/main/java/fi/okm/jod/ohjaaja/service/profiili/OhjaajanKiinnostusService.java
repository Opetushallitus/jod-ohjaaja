/*
 * Copyright (c) 2025 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.service.profiili;

import fi.okm.jod.ohjaaja.domain.JodUser;
import fi.okm.jod.ohjaaja.dto.profiili.KiinnostusDto;
import fi.okm.jod.ohjaaja.entity.OhjaajanKiinnostus;
import fi.okm.jod.ohjaaja.repository.OhjaajaRepository;
import fi.okm.jod.ohjaaja.repository.OhjaajanKiinnostusRepository;
import fi.okm.jod.ohjaaja.service.NotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class OhjaajanKiinnostusService {
  private final OhjaajaRepository ohjaajat;
  private final OhjaajanKiinnostusRepository kiinnostukset;

  public List<KiinnostusDto> findAll(JodUser user) {
    var ohjaaja = ohjaajat.getReferenceById(user.getId());
    return kiinnostukset.findByOhjaaja(ohjaaja).stream()
        .map(
            kiinnostus ->
                new KiinnostusDto(
                    kiinnostus.getId(), kiinnostus.getAsiasanaId(), kiinnostus.getLuotu()))
        .toList();
  }

  public UUID add(JodUser user, long asiasanaId) {
    var ohjaaja = ohjaajat.getReferenceById(user.getId());
    return kiinnostukset
        .findByOhjaajaAndAsiasanaId(ohjaaja, asiasanaId)
        .map(OhjaajanKiinnostus::getId)
        .orElseGet(() -> kiinnostukset.save(new OhjaajanKiinnostus(asiasanaId, ohjaaja)).getId());
  }

  public void delete(JodUser user, UUID id) {
    if (kiinnostukset.deleteByOhjaajaAndId(ohjaajat.getReferenceById(user.getId()), id) == 0) {
      throw new NotFoundException("Kiinnostus not found");
    }
  }
}
