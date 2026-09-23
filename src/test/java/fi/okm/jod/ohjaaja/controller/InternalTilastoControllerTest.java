/*
 * Copyright (c) 2026 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fi.okm.jod.ohjaaja.config.ProdInternalApiSecurityConfig;
import fi.okm.jod.ohjaaja.dto.ArtikkeliMaaraDto;
import fi.okm.jod.ohjaaja.dto.KiinnostusMaaraDto;
import fi.okm.jod.ohjaaja.dto.TilastotDto;
import fi.okm.jod.ohjaaja.dto.TyoskentelyPaikkaMaaraDto;
import fi.okm.jod.ohjaaja.entity.TyoskentelyPaikka;
import fi.okm.jod.ohjaaja.errorhandler.ErrorInfoFactory;
import fi.okm.jod.ohjaaja.service.TilastoService;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(InternalTilastoController.class)
@Import({ErrorInfoFactory.class, ProdInternalApiSecurityConfig.class})
@TestPropertySource(properties = "jod.ohjaaja.internal-api.oauth2-scope=test")
class InternalTilastoControllerTest {

  private static final String URL = "/internal-api/tilastot";

  @MockitoBean private TilastoService service;
  @Autowired private MockMvc mockMvc;

  @Test
  void shouldReturnStatistics() throws Exception {
    var alku = LocalDate.of(2026, 1, 1);
    var loppu = LocalDate.of(2026, 1, 31);
    when(service.getTilastot(alku, loppu, 5))
        .thenReturn(
            new TilastotDto(
                alku,
                loppu,
                3,
                List.of(
                    new TyoskentelyPaikkaMaaraDto(TyoskentelyPaikka.PERUSASTE, 2),
                    new TyoskentelyPaikkaMaaraDto(null, 1)),
                List.of(new KiinnostusMaaraDto(42L, 2)),
                List.of(new ArtikkeliMaaraDto("suosikki", 4)),
                List.of(new ArtikkeliMaaraDto("kommentoitu", 5)),
                List.of(new ArtikkeliMaaraDto("katsottu", 6))));

    mockMvc
        .perform(
            get(URL)
                .param("alku", "2026-01-01")
                .param("loppu", "2026-01-31")
                .param("top", "5")
                .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_test"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.alku").value("2026-01-01"))
        .andExpect(jsonPath("$.loppu").value("2026-01-31"))
        .andExpect(jsonPath("$.kayttajienMaara").value(3))
        .andExpect(jsonPath("$.tyoskentelyPaikat[0].tyoskentelyPaikka").value("PERUSASTE"))
        .andExpect(jsonPath("$.tyoskentelyPaikat[0].maara").value(2))
        .andExpect(jsonPath("$.kiinnostukset[0].asiasanaId").value(42))
        .andExpect(jsonPath("$.suosituimmatArtikkelit[0].artikkeliErc").value("suosikki"))
        .andExpect(jsonPath("$.kommentoiduimmatArtikkelit[0].maara").value(5))
        .andExpect(jsonPath("$.katsotuimmatArtikkelit[0].artikkeliErc").value("katsottu"));
  }

  @Test
  void shouldUseDefaultsWhenNoParameters() throws Exception {
    mockMvc
        .perform(get(URL).with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_test"))))
        .andExpect(status().isOk());

    verify(service).getTilastot(isNull(), isNull(), eq(10));
  }

  @Test
  void shouldRejectInvalidTop() throws Exception {
    mockMvc
        .perform(
            get(URL)
                .param("top", "0")
                .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_test"))))
        .andExpect(status().isBadRequest());
    mockMvc
        .perform(
            get(URL)
                .param("top", "101")
                .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_test"))))
        .andExpect(status().isBadRequest());
  }

  @Test
  void shouldRejectRequestWithoutToken() throws Exception {
    mockMvc.perform(get(URL)).andExpect(status().isForbidden());
    verify(service, never()).getTilastot(isNull(), isNull(), anyInt());
  }

  @Test
  void shouldRejectRequestWithWrongScope() throws Exception {
    mockMvc
        .perform(get(URL).with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_other"))))
        .andExpect(status().isForbidden());
  }
}
