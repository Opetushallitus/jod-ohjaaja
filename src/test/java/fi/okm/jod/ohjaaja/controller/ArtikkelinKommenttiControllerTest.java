/*
 * Copyright (c) 2025 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.okm.jod.ohjaaja.config.ApiSecurityConfig;
import fi.okm.jod.ohjaaja.config.mocklogin.MockJodUserImpl;
import fi.okm.jod.ohjaaja.domain.JodUser;
import fi.okm.jod.ohjaaja.dto.ArtikkelinKommenttiDto;
import fi.okm.jod.ohjaaja.dto.SivuDto;
import fi.okm.jod.ohjaaja.errorhandler.ErrorInfoFactory;
import fi.okm.jod.ohjaaja.service.ArtikkelinKommenttiService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(value = ArtikkelinKommenttiController.class)
@Import({ErrorInfoFactory.class, ApiSecurityConfig.class})
class ArtikkelinKommenttiControllerTest {
  @MockitoBean private ArtikkelinKommenttiService service;
  @Autowired private MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;

  @TestConfiguration
  static class TestConfig {
    @Bean
    UserDetailsService mockUserDetailsService() {
      return username -> username != null ? new MockJodUserImpl(username, UUID.randomUUID()) : null;
    }
  }

  @Test
  void findAllArtikkelinKommentitReturnsPagedComments() throws Exception {
    UUID kommenttiId = UUID.randomUUID();
    var kommenttiDto =
        new ArtikkelinKommenttiDto(
            kommenttiId, "external-reference-code", UUID.randomUUID(), "Kommentti", Instant.now());
    var sivuDto = new SivuDto<>(List.of(kommenttiDto), 1, 1);

    when(service.findByArtikkeliErc(eq("external-reference-code"), any(PageRequest.class)))
        .thenReturn(sivuDto);

    mockMvc
        .perform(
            get("/api/artikkeli/kommentit")
                .param("artikkeliErc", "external-reference-code")
                .param("sivu", "0")
                .param("koko", "10")
                .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.sisalto[0].id").value(kommenttiId.toString()))
        .andExpect(jsonPath("$.sisalto[0].artikkeliErc").value("external-reference-code"))
        .andExpect(jsonPath("$.sisalto[0].kommentti").value("Kommentti"));
  }

  @Test
  @WithUserDetails("test")
  void createArtikkelinKommenttiSucceeds() throws Exception {
    var kommenttiDto =
        new ArtikkelinKommenttiDto(null, "external-reference-code", null, "Uusi kommentti", null);

    mockMvc
        .perform(
            post("/api/artikkeli/kommentit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(kommenttiDto))
                .with(csrf()))
        .andExpect(status().isOk());

    verify(service).add(any(JodUser.class), eq("external-reference-code"), eq("Uusi kommentti"));
  }

  @Test
  @WithUserDetails("test")
  void createArtikkelinKommenttiFailsWithInvalidData() throws Exception {
    var invalidKommenttiDto = new ArtikkelinKommenttiDto(null, null, null, "", null);

    mockMvc
        .perform(
            post("/api/artikkeli/kommentit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidKommenttiDto))
                .with(csrf()))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithUserDetails("test")
  void deleteArtikkelinKommenttiSucceeds() throws Exception {
    UUID kommenttiId = UUID.randomUUID();

    mockMvc
        .perform(delete("/api/artikkeli/kommentit/{id}", kommenttiId).with(csrf()))
        .andExpect(status().isOk());

    verify(service).delete(any(JodUser.class), eq(kommenttiId));
  }

  @Test
  @WithAnonymousUser
  void unauthenticatedUserCannotCreateEndpoint() throws Exception {
    var kommenttiDto =
        new ArtikkelinKommenttiDto(null, "external-reference-code", null, "Uusi kommentti", null);
    mockMvc
        .perform(
            post("/api/artikkeli/kommentit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(kommenttiDto))
                .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(forwardedUrl("/error"));
  }

  @Test
  void unauthenticatedUserCannotDeleteEndpoint() throws Exception {
    UUID kommenttiId = UUID.randomUUID();

    mockMvc
        .perform(delete("/api/artikkeli/kommentit/{id}", kommenttiId).with(csrf()))
        .andExpect(status().isOk())
        .andExpect(forwardedUrl("/error"));
  }
}
