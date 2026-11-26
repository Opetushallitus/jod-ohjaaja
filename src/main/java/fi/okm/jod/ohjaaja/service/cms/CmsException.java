/*
 * Copyright (c) 2026 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.service.cms;

/** Thrown when the CMS cannot be queried or returns an unexpected response. */
public class CmsException extends RuntimeException {
  public CmsException(String message) {
    super(message);
  }

  public CmsException(String message, Throwable cause) {
    super(message, cause);
  }
}
