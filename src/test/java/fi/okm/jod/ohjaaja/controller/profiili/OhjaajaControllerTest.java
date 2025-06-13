/*
 * Copyright (c) 2025 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.controller.profiili;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fi.okm.jod.ohjaaja.config.mocklogin.MockJodUserImpl;
import fi.okm.jod.ohjaaja.dto.profiili.OhjaajaDto;
import fi.okm.jod.ohjaaja.entity.TyoskentelyPaikka;
import fi.okm.jod.ohjaaja.errorhandler.ErrorInfoFactory;
import fi.okm.jod.ohjaaja.service.profiili.OhjaajaService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = OhjaajaController.class)
@Import({ErrorInfoFactory.class})
@EnableWebSecurity
class OhjaajaControllerTest {

  @MockitoBean private OhjaajaService ohjaajaService;
  @Autowired private MockMvc mockMvc;

  @TestConfiguration
  static class TestConfig {
    @Bean
    UserDetailsService mockUserDetailsService() {
      return username -> new MockJodUserImpl(username, UUID.randomUUID());
    }
  }

  @Test
  @WithUserDetails("test")
  void shouldReturnUserInformation() throws Exception {
    when(ohjaajaService.get(any())).thenReturn(new OhjaajaDto(TyoskentelyPaikka.TOINEN_ASTE));
    mockMvc
        .perform(get("/api/profiili/ohjaaja").with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.etunimi").isNotEmpty())
        .andExpect(jsonPath("$.csrf.token").isNotEmpty())
        .andExpect(jsonPath("$.tyoskentelyPaikka").value("TOINEN_ASTE"));
  }

  @Test
  void shouldNotReturnCsrfTokenIfUnauthenticated() throws Exception {
    mockMvc
        .perform(get("/api/profiili/ohjaaja").with(csrf()))
        .andExpect(jsonPath("$.csrf.token").doesNotExist());
  }
}
