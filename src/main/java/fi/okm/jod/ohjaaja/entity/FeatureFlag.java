/*
 * Copyright (c) 2025 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.Instant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class FeatureFlag {

  @Id
  @Column(updatable = false, nullable = false)
  private Feature feature;

  @Column(nullable = false)
  private boolean enabled;

  @Column(nullable = false)
  private String updatedBy;

  @Column(nullable = false)
  private Instant updatedAt;

  public FeatureFlag(Feature feature) {
    this.feature = feature;
    this.enabled = false;
    this.updatedBy = "system"; // Default value, can be changed later
    this.updatedAt = Instant.now();
  }

  public enum Feature {
    COMMENTS,
  }
}
