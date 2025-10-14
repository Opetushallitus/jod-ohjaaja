/*
 * Copyright (c) 2025 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.config.suomifi;

import static java.util.Objects.requireNonNull;
import static org.springframework.transaction.TransactionDefinition.PROPAGATION_REQUIRES_NEW;

import fi.okm.jod.ohjaaja.config.logging.LogMarker;
import fi.okm.jod.ohjaaja.entity.Ohjaaja;
import fi.okm.jod.ohjaaja.repository.OhjaajaRepository;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml4AuthenticationProvider;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml4AuthenticationProvider.ResponseToken;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Component
@ConditionalOnBean(Saml2LoginConfig.class)
class ResponseTokenConverter implements Converter<ResponseToken, Saml2Authentication> {

  private final TransactionTemplate transactionTemplate;
  private final OhjaajaRepository ohjaajat;
  private final Converter<ResponseToken, Saml2Authentication> converter;
  private final JodAuthenticationProperties authenticationProperties;

  public ResponseTokenConverter(
      OhjaajaRepository ohjaajat,
      PlatformTransactionManager transactionManager,
      JodAuthenticationProperties authenticationProperties) {
    this.converter = OpenSaml4AuthenticationProvider.createDefaultResponseAuthenticationConverter();
    this.ohjaajat = ohjaajat;

    var template = new TransactionTemplate(transactionManager);
    template.setTimeout(10);
    template.setPropagationBehavior(PROPAGATION_REQUIRES_NEW);
    this.transactionTemplate = template;
    this.authenticationProperties = authenticationProperties;
  }

  @Override
  public Saml2Authentication convert(@NonNull ResponseToken responseToken) {
    if (converter.convert(responseToken) instanceof Saml2Authentication authentication
        && authentication.getPrincipal() instanceof Saml2AuthenticatedPrincipal principal) {

      // https://palveluhallinta.suomi.fi/fi/tuki/artikkelit/59116c3014bbb10001966f70
      // Tekninen rajapintakuvaus / Tunnistusvastaus / Tunnistustapahtuman vahvuus
      var level =
          URI.create(
              requireNonNull(
                  responseToken
                      .getResponse()
                      .getAssertions()
                      .getFirst()
                      .getAuthnStatements()
                      .getFirst()
                      .getAuthnContext()
                      .getAuthnContextClassRef()
                      .getURI()));

      var pid = authenticationProperties.getSupportedMethods().get(level);

      if (pid == null) {
        throw new BadCredentialsException("Unsupported authentication method: " + level);
      }

      var userId =
          resolveUser(
              getPersonId(principal, pid)
                  .orElseThrow(() -> new BadCredentialsException("Invalid person identifier")));

      return new Saml2Authentication(
          new JodSaml2Principal(
              principal.getName(),
              principal.getAttributes(),
              principal.getSessionIndexes(),
              principal.getRelyingPartyRegistrationId(),
              userId),
          authentication.getSaml2Response(),
          authentication.getAuthorities());
    }

    throw new BadCredentialsException("Invalid response token");
  }

  private UUID resolveUser(String personId) {

    return transactionTemplate.execute(
        status -> {
          var id = ohjaajat.findIdByHenkiloId(personId);
          return ohjaajat
              .findById(id)
              .orElseGet(
                  () -> {
                    log.atInfo().addMarker(LogMarker.AUDIT).log("Creating new user with id {}", id);
                    return ohjaajat.save(new Ohjaaja(id));
                  })
              .getId();
        });
  }

  private static Optional<String> getPersonId(
      Saml2AuthenticatedPrincipal principal, PersonIdentifier identifier) {
    return Optional.ofNullable(principal.getFirstAttribute(identifier.getAttribute().getUri()))
        .map(value -> identifier.name() + ":" + value);
  }
}
