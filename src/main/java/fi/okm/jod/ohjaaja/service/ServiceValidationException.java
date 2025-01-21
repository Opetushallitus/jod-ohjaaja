/*
 * Copyright (c) 2025 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.service;

@SuppressWarnings("serial")
public class ServiceValidationException extends ServiceException {
  public ServiceValidationException(String message) {
    super(message);
  }

  public ServiceValidationException(String message, Throwable cause) {
    super(message, cause);
  }

  public ServiceValidationException(Throwable cause) {
    super(cause);
  }
}
