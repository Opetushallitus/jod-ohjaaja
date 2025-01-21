/*
 * Copyright (c) 2025 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.config.mocklogin;

import fi.okm.jod.ohjaaja.config.LoginSuccessHandler;
import fi.okm.jod.ohjaaja.entity.Ohjaaja;
import fi.okm.jod.ohjaaja.repository.OhjaajaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.util.StringUtils;

@Configuration
@ConditionalOnProperty(
    name = "jod.authentication.provider",
    havingValue = "mock",
    matchIfMissing = true)
@Slf4j
public class MockLoginConfig {

  /** Mock authentication using form login. */
  @Bean
  SecurityFilterChain mockLoginFilterChain(HttpSecurity http) throws Exception {
    log.warn("WARNING: Using mock authentication.");

    var redirectStrategy = new DefaultRedirectStrategy();
    redirectStrategy.setStatusCode(HttpStatus.SEE_OTHER);

    var loginSuccessHandler = new LoginSuccessHandler(redirectStrategy);

    var logoutSuccessHandler = new SimpleUrlLogoutSuccessHandler();
    logoutSuccessHandler.setRedirectStrategy(redirectStrategy);

    return http.securityMatcher("/login/**", "/logout/**")
        .formLogin(login -> login.successHandler(loginSuccessHandler).loginPage("/login"))
        .logout(logout -> logout.logoutSuccessHandler(logoutSuccessHandler))
        .headers(
            headers ->
                headers.contentSecurityPolicy(
                    csp ->
                        csp.policyDirectives(
                            "default-src 'self'; frame-ancestors 'none'; style-src 'self' 'unsafe-inline';")))
        .build();
  }

  @Bean
  @SuppressWarnings("java:S5804")
  UserDetailsService mockUserDetailsService(OhjaajaRepository ohjaajat) {
    log.warn("WARNING: Using mock user details service.");
    return username -> {
      if (!StringUtils.hasLength(username)
          || username.length() > 100
          || !username.strip().equals(username)) {
        throw new UsernameNotFoundException("Invalid username");
      }
      try {
        var id = ohjaajat.findIdByHenkiloId("MOCK:" + username);
        var ohjaaja = ohjaajat.findById(id).orElseGet(() -> ohjaajat.save(new Ohjaaja(id)));
        return new MockJodUserImpl(username, ohjaaja.getId());
      } catch (Exception e) {
        throw new UsernameNotFoundException("Unable to find user", e);
      }
    };
  }
}
