/*
 * Copyright (c) 2025 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.config.suomifi;

import static fi.okm.jod.ohjaaja.config.SessionLoginAttribute.LANG;
import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static org.springframework.security.config.Customizer.withDefaults;

import fi.okm.jod.ohjaaja.config.LoginSuccessHandler;
import fi.okm.jod.ohjaaja.config.ProfileDeletionHandler;
import fi.okm.jod.ohjaaja.domain.Kieli;
import fi.okm.jod.ohjaaja.service.profiili.OhjaajaService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.metadata.Endpoint;
import org.opensaml.saml.security.impl.SAMLSignatureProfileValidator;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xmlsec.keyinfo.KeyInfoSupport;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.SignatureValidator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.ssl.pem.PemContent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.RequestCacheConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.saml2.core.Saml2X509Credential;
import org.springframework.security.saml2.core.Saml2X509Credential.Saml2X509CredentialType;
import org.springframework.security.saml2.provider.service.authentication.OpenSaml5AuthenticationProvider;
import org.springframework.security.saml2.provider.service.registration.InMemoryRelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.OpenSamlAssertingPartyDetails;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrations;
import org.springframework.security.saml2.provider.service.registration.Saml2MessageBinding;
import org.springframework.security.saml2.provider.service.web.authentication.OpenSaml5AuthenticationRequestResolver;
import org.springframework.security.saml2.provider.service.web.authentication.Saml2AuthenticationRequestResolver;
import org.springframework.security.saml2.provider.service.web.authentication.logout.OpenSaml5LogoutRequestResolver;
import org.springframework.security.saml2.provider.service.web.authentication.logout.Saml2LogoutRequestResolver;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "jod.authentication.provider", havingValue = "suomifi")
@Slf4j
@RequiredArgsConstructor
public class Saml2LoginConfig {
  private final VetumaExtensionBuilder vetumaExtensionBuilder = new VetumaExtensionBuilder();
  private final OhjaajaService ohjaajaService;

  @Bean
  RelyingPartyRegistrationRepository relyingPartyRegistrationRepository(
      RelyingPartyProperties properties) {

    // missing:
    // - making the repository refreshable (to support metadata and credential rotation)

    var metadata =
        (OpenSamlAssertingPartyDetails)
            RelyingPartyRegistrations.fromMetadataLocation(properties.getIdpMetadataUri())
                .build()
                .getAssertingPartyMetadata();

    var descriptor =
        requireNonNull(
            metadata.getEntityDescriptor().getIDPSSODescriptor(SAMLConstants.SAML20P_NS),
            "IDPSSODescriptor not found in metadata for entity");

    var expectedCert =
        requireNonNull(PemContent.of(properties.getIdpMetadataSigningCaCertificate()))
            .getCertificates()
            .getFirst();
    validateMetadataSignature(metadata, expectedCert);

    var slo = getRedirectionEndpoint(descriptor.getSingleLogoutServices());
    var sso = getRedirectionEndpoint(descriptor.getSingleSignOnServices());

    // we want to use redirect binding, the default is arbitrary depending on the order in
    // the metadata descriptor
    metadata =
        metadata
            .mutate()
            .singleSignOnServiceBinding(Saml2MessageBinding.REDIRECT)
            .singleSignOnServiceLocation(requireNonNull(sso.getLocation()))
            .singleLogoutServiceBinding(Saml2MessageBinding.REDIRECT)
            .singleLogoutServiceLocation(requireNonNull(slo.getLocation()))
            .singleLogoutServiceResponseLocation(
                requireNonNullElse(slo.getResponseLocation(), slo.getLocation()))
            .build();

    // only the active credential signs; both can decrypt, so that logins keep working while an
    // (out of band) IDP metadata update rolls the key over
    var signing = toSaml2Credential(properties.getCredential(), Saml2X509CredentialType.SIGNING);
    var decryption =
        Stream.of(properties.getCredential(), properties.getNextCredential())
            .filter(Objects::nonNull)
            .map(c -> toSaml2Credential(c, Saml2X509CredentialType.DECRYPTION))
            .toList();

    return new InMemoryRelyingPartyRegistrationRepository(
        RelyingPartyRegistration.withAssertingPartyMetadata(metadata)
            .registrationId(properties.getRegistrationId())
            .nameIdFormat(NameIDType.TRANSIENT)
            .assertionConsumerServiceBinding(Saml2MessageBinding.POST)
            .singleLogoutServiceLocation("{baseUrl}/logout/saml2/slo/{registrationId}")
            .singleLogoutServiceBinding(Saml2MessageBinding.POST)
            .signingX509Credentials(credentials -> credentials.add(signing))
            .decryptionX509Credentials(credentials -> credentials.addAll(decryption))
            .build());
  }

  private static Saml2X509Credential toSaml2Credential(
      RelyingPartyProperties.Credential credential, Saml2X509CredentialType type) {
    var cert = requireNonNull(PemContent.of(credential.getCertificate()));
    var key = requireNonNull(PemContent.of(credential.getPrivateKey()));
    return new Saml2X509Credential(key.getPrivateKey(), cert.getCertificates().getFirst(), type);
  }

  private static void validateMetadataSignature(
      OpenSamlAssertingPartyDetails metadata, X509Certificate trustedCa) {
    var signature = metadata.getEntityDescriptor().getSignature();
    if (signature == null) {
      throw new IllegalStateException("IDP metadata is not signed (ds:Signature missing)");
    }

    var signingCert = extractSigningCertificate(signature);
    validateTrustedCaCertificate(trustedCa);
    validateCertificateIssuedByCa(signingCert, trustedCa);

    try {
      new SAMLSignatureProfileValidator().validate(signature);
      SignatureValidator.validate(signature, new BasicX509Credential(signingCert));
    } catch (SignatureException e) {
      throw new IllegalStateException("IDP metadata signature validation failed", e);
    }
    log.info(
        "IDP metadata signature validated: subject={}, issuer={}",
        signingCert.getSubjectX500Principal().getName(),
        signingCert.getIssuerX500Principal().getName());
  }

  private static X509Certificate extractSigningCertificate(Signature signature) {
    var keyInfo = signature.getKeyInfo();
    if (keyInfo == null) {
      throw new IllegalStateException("ds:Signature is missing ds:KeyInfo");
    }
    try {
      var certs = KeyInfoSupport.getCertificates(keyInfo);
      if (certs.isEmpty()) {
        throw new IllegalStateException("No certificate found in ds:KeyInfo");
      }
      return certs.getFirst();
    } catch (java.security.cert.CertificateException e) {
      throw new IllegalStateException("Failed to parse certificate from ds:KeyInfo", e);
    }
  }

  private static void validateCertificateIssuedByCa(
      X509Certificate cert, X509Certificate trustedCa) {
    var keyUsage = cert.getKeyUsage();
    if (keyUsage != null && (keyUsage.length == 0 || !keyUsage[0])) {
      throw new IllegalStateException(
          "IDP signing certificate is not permitted for digital signatures: subject=%s"
              .formatted(cert.getSubjectX500Principal().getName()));
    }
    try {
      cert.checkValidity();
      cert.verify(trustedCa.getPublicKey());
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException(
          "IDP signing certificate not issued by trusted CA: subject=%s, ca=%s"
              .formatted(
                  cert.getSubjectX500Principal().getName(),
                  trustedCa.getSubjectX500Principal().getName()),
          e);
    }
  }

  private static void validateTrustedCaCertificate(X509Certificate trustedCa) {
    try {
      trustedCa.checkValidity();
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException(
          "Trusted IDP metadata signing CA certificate is not valid: subject=%s"
              .formatted(trustedCa.getSubjectX500Principal().getName()),
          e);
    }
  }

  private static <T extends Endpoint> T getRedirectionEndpoint(Collection<T> endpoints) {
    return endpoints.stream()
        .filter(s -> Objects.equals(s.getBinding(), SAMLConstants.SAML2_REDIRECT_BINDING_URI))
        .findAny()
        .orElseThrow();
  }

  @Bean
  @SuppressWarnings("java:S4502")
  SecurityFilterChain samlSecurityFilterChain(
      HttpSecurity http,
      ResponseTokenConverter converter,
      Saml2AuthenticationRequestResolver authenticationRequestResolver,
      Saml2LogoutRequestResolver logoutRequestResolver)
      throws Exception {

    log.info("Configuring Suomi.fi-tunnistus");

    var redirectStrategy = new DefaultRedirectStrategy();
    redirectStrategy.setStatusCode(HttpStatus.SEE_OTHER);

    var loginSuccessHandler = new LoginSuccessHandler(redirectStrategy);
    var authenticationEventHandler = new AuthenticationEventHandler(redirectStrategy);
    var profileDeletionHandler = new ProfileDeletionHandler(ohjaajaService);

    var authProvider = new OpenSaml5AuthenticationProvider();
    authProvider.setResponseAuthenticationConverter(converter);

    return http.securityMatcher("/saml2/**", "/login/**", "/logout/**")
        .requestCache(RequestCacheConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
        .csrf(csrf -> csrf.ignoringRequestMatchers(request -> request.getSession(false) == null))
        .saml2Metadata(withDefaults())
        .saml2Login(
            login ->
                login
                    .loginPage("/login")
                    .successHandler(loginSuccessHandler)
                    .failureHandler(authenticationEventHandler)
                    .authenticationRequestResolver(authenticationRequestResolver)
                    .authenticationManager(new ProviderManager(authProvider)))
        .saml2Logout(
            logout -> {
              logout.logoutResponse(
                  response -> response.logoutUrl("/logout/saml2/slo/{registrationId}"));
              logout.logoutRequest(
                  request -> {
                    request.logoutUrl("/logout/saml2/slo/{registrationId}");
                    request.logoutRequestResolver(logoutRequestResolver);
                  });
            })
        .logout(
            logout -> {
              logout.addLogoutHandler(profileDeletionHandler);
              logout.logoutSuccessHandler(authenticationEventHandler);
            })
        .headers(
            headers ->
                headers.contentSecurityPolicy(
                    csp ->
                        csp.policyDirectives(
                            "default-src 'self'; frame-ancestors 'self' https://tunnistautuminen.suomi.fi https://testi.apro.tunnistus.fi;")))
        .build();
  }

  @Bean
  Saml2AuthenticationRequestResolver authenticationRequestResolver(
      RelyingPartyRegistrationRepository registrations, JodAuthenticationProperties properties) {

    final var resolver = new OpenSaml5AuthenticationRequestResolver(registrations);
    final var builder = new AuthnContextBuilder();

    resolver.setAuthnRequestCustomizer(
        authnRequest -> {
          // https://palveluhallinta.suomi.fi/fi/tuki/artikkelit/59116c3014bbb10001966f70
          // Tekninen rajapintakuvaus / Tunnistuspyyntö

          // Hyväksytyt tunnistusvälineet
          authnRequest
              .getAuthnRequest()
              .setRequestedAuthnContext(builder.build(properties.getSupportedMethods().keySet()));

          // Käyttöliittymän kieli
          resolveKieli(authnRequest.getRequest())
              .ifPresent(
                  kieli ->
                      authnRequest
                          .getAuthnRequest()
                          .setExtensions(vetumaExtensionBuilder.build(kieli)));
        });

    return resolver;
  }

  @Bean
  Saml2LogoutRequestResolver logoutRequestResolver(
      RelyingPartyRegistrationRepository registrations) {
    var resolver = new OpenSaml5LogoutRequestResolver(registrations);
    resolver.setParametersConsumer(
        parameters -> {
          final LogoutRequest logoutRequest = parameters.getLogoutRequest();
          // Suomi.fi tunnistus requires that the nameId format is set to transient
          logoutRequest.getNameID().setFormat(NameIDType.TRANSIENT);
          resolveKieli(parameters.getRequest())
              .ifPresent(
                  kieli -> {
                    parameters
                        .getRequest()
                        .getSession()
                        .setAttribute(LANG.getKey(), kieli.toString());
                    logoutRequest.setExtensions(vetumaExtensionBuilder.build(kieli));
                  });
        });
    return resolver;
  }

  static Optional<Kieli> resolveKieli(HttpServletRequest req) {

    if (req.getSession(false) instanceof HttpSession session) {
      return Optional.ofNullable(
          switch (session.getAttribute(LANG.getKey())) {
            case null -> null;
            case String s when s.equals("fi") -> Kieli.FI;
            case String s when s.equals("sv") -> Kieli.SV;
            default -> Kieli.EN;
          });
    }
    return Optional.ofNullable(
        switch (req.getParameter("lang")) {
          case null -> null;
          case "fi" -> Kieli.FI;
          case "sv" -> Kieli.SV;
          default -> Kieli.EN;
        });
  }

  static class AuthenticationEventHandler
      implements AuthenticationFailureHandler, LogoutSuccessHandler {
    private final RedirectStrategy redirectStrategy;

    public AuthenticationEventHandler(RedirectStrategy redirectStrategy) {
      this.redirectStrategy = redirectStrategy;
    }

    void handle(
        HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
        throws IOException {

      var path = resolveKieli(request).map(k -> "/" + k).orElse("/");

      if (request.getSession(false) instanceof HttpSession s
          && SecurityContextHolder.getContext().getAuthentication() == null) {
        // clear the temporary session used for SAML logout
        s.invalidate();
      }
      var queryParam = "";
      if (exception != null) {
        queryParam = "?error=AUTHENTICATION_FAILURE";
        log.warn("Authentication failure: {}", exception.getMessage());
      }
      redirectStrategy.sendRedirect(request, response, path + queryParam);
    }

    @Override
    public void onAuthenticationFailure(
        HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
        throws IOException {
      handle(request, response, exception);
    }

    @Override
    public void onLogoutSuccess(
        HttpServletRequest request, HttpServletResponse response, Authentication authentication)
        throws IOException {
      handle(request, response, null);
    }
  }
}
