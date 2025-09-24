/*
 * Copyright (c) 2025 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.service;

import fi.okm.jod.ohjaaja.dto.PalauteViestiDto;
import fi.okm.jod.ohjaaja.dto.SisaltoEhdotusDto;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SisaltoEhdotusService {

  private final PalauteKanavaService service;

  public SisaltoEhdotusService(@Autowired(required = false) PalauteKanavaService service) {
    this.service = service;
  }

  public void sendSisaltoEhdotus(SisaltoEhdotusDto sisaltoEhdotusDto) {
    var sisaltoEhdotusViesti =
        new PalauteViestiDto(
            "Sisältöehdotus",
            "Ohjaajan osio",
            "Ohjaajan osio",
            sisaltoEhdotusDto.sahkoposti() != null && !sisaltoEhdotusDto.sahkoposti().isEmpty()
                ? sisaltoEhdotusDto.sahkoposti()
                : null,
            sisaltoEhdotusDto.kieli(),
            createMessage(sisaltoEhdotusDto),
            Instant.now());
    if (service != null) {
      service.sendMessage(sisaltoEhdotusViesti);
    } else {
      throw new IllegalStateException("PalauteKanavaService is not available");
    }
  }

  private String createMessage(SisaltoEhdotusDto dto) {
    StringBuilder message = new StringBuilder();
    message.append("Ehdotettu sisältö:\n").append(dto.sisalto()).append("\n\n");
    if (dto.sahkoposti() != null && !dto.sahkoposti().isEmpty()) {
      message.append("Sähköposti:\n").append(dto.sahkoposti()).append("\n\n");
    }
    if (dto.linkki() != null && !dto.linkki().isEmpty()) {
      message.append("Linkki:\n").append(dto.linkki()).append("\n\n");
    }
    message.append("Kuvaus:\n").append(dto.kuvaus()).append("\n\n");
    return message.toString();
  }
}
