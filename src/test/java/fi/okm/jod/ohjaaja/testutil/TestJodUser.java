/*
 * Copyright (c) 2025 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.testutil;

import fi.okm.jod.ohjaaja.domain.JodUser;
import java.util.UUID;

public record TestJodUser(UUID id) implements JodUser {

  @Override
  public UUID getId() {
    return id();
  }

  @Override
  public String givenName() {
    return "Test";
  }

  @Override
  public String familyName() {
    return "User";
  }

  public static JodUser of(String uuid) {
    return new TestJodUser(UUID.fromString(uuid));
  }
}
