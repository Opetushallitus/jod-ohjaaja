/*
 * Copyright (c) 2025 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.entity;

import jakarta.persistence.Embeddable;
import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Embeddable
public class ArtikkelinKommentinIlmiantoId implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  private UUID artikkelinKommenttiId;
  private boolean tunnistautunut;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ArtikkelinKommentinIlmiantoId that)) {
      return false;
    }
    return Objects.equals(artikkelinKommenttiId, that.artikkelinKommenttiId)
        && Objects.equals(tunnistautunut, that.tunnistautunut);
  }

  @Override
  public int hashCode() {
    return Objects.hash(artikkelinKommenttiId, tunnistautunut);
  }
}
