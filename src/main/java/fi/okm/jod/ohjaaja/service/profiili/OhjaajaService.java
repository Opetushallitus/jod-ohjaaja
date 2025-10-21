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
import fi.okm.jod.ohjaaja.dto.profiili.OhjaajaDto;
import fi.okm.jod.ohjaaja.dto.profiili.export.OhjaajaExportDto;
import fi.okm.jod.ohjaaja.entity.Ohjaaja;
import fi.okm.jod.ohjaaja.repository.OhjaajaRepository;
import fi.okm.jod.ohjaaja.service.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class OhjaajaService {
  private final OhjaajaRepository ohjaajat;

  @Transactional(readOnly = true)
  public OhjaajaDto get(JodUser user) {
    var ohjaaja = getOhjaaja(user);
    return new OhjaajaDto(ohjaaja.getId(), ohjaaja.getTyoskentelyPaikka());
  }

  public void update(JodUser user, OhjaajaDto dto) {
    var ohjaaja = getOhjaaja(user);
    ohjaaja.setTyoskentelyPaikka(dto.tyoskentelyPaikka());
    ohjaajat.save(ohjaaja);
  }

  public void delete(JodUser user) {
    ohjaajat.deleteById(user.getId());
    ohjaajat.removeId(user.getId());
    log.atInfo()
        .addMarker(LogMarker.AUDIT)
        .addKeyValue("userId", user.getId())
        .log("User profile deleted");
  }

  @Transactional(readOnly = true)
  public OhjaajaExportDto export(JodUser user) {
    var ohjaaja = getOhjaaja(user);
    log.atInfo()
        .addMarker(LogMarker.AUDIT)
        .addKeyValue("userId", user.getId())
        .log("User profile exported");
    return ExportMapper.mapOhjaaja(ohjaaja);
  }

  private Ohjaaja getOhjaaja(JodUser user) {
    return ohjaajat
        .findById(user.getId())
        .orElseThrow(() -> new NotFoundException("Profiili does not exist"));
  }
}
