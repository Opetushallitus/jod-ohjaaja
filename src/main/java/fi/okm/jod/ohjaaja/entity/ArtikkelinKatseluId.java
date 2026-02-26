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
import java.time.LocalDate;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Embeddable
public class ArtikkelinKatseluId implements Serializable {

  @Serial private static final long serialVersionUID = 1L;
  private String artikkeliErc;
  private LocalDate paiva;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ArtikkelinKatseluId that)) {
      return false;
    }
    return Objects.equals(artikkeliErc, that.artikkeliErc) && Objects.equals(paiva, that.paiva);
  }

  @Override
  public int hashCode() {
    return Objects.hash(artikkeliErc, paiva);
  }
}
