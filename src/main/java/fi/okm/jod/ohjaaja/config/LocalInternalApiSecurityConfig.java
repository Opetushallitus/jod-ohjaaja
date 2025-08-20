/*
 * Copyright (c) 2025 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.config;

import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Profile("local")
public class LocalInternalApiSecurityConfig {
  @Bean
  @SuppressWarnings("java:S4502")
  public SecurityFilterChain internalApiSecurityFilterChain(
      HttpSecurity http, @Value("SCOPE_${jod.ohjaaja.internal-api.oauth2-scope:}") String scope)
      throws Exception {

    http.securityMatcher("/internal-api/**")
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
        .addFilterBefore(
            internalApiMockAuthFilter(scope), UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  public Filter internalApiMockAuthFilter(String scope) {
    return (request, response, chain) -> {
      var httpServletRequest = (HttpServletRequest) request;
      var path = httpServletRequest.getRequestURI();
      var authHeader = httpServletRequest.getHeader("Authorization");
      if (path.startsWith("/ohjaaja/internal-api/")
          && (authHeader != null && authHeader.startsWith("Bearer "))) {
        var authorities = List.of(new SimpleGrantedAuthority(scope));
        var authentication =
            new UsernamePasswordAuthenticationToken("local-user", null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
      }
      chain.doFilter(request, response);
    };
  }
}
