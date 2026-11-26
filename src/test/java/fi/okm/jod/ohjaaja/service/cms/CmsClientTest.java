/*
 * Copyright (c) 2026 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.service.cms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.ExpectedCount.times;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import fi.okm.jod.ohjaaja.entity.SisaltoTyyppi;
import java.net.URI;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class CmsClientTest {

  private static final String URL =
      "http://cms.test/o/headless-delivery/v1.0/sites/20117/structured-contents/"
          + "by-external-reference-code/erc-1?fields=contentType";

  private static final Duration FAILURE_CACHE_TTL = Duration.ofMinutes(1);

  private final AtomicLong nanos = new AtomicLong();
  private MockRestServiceServer server;
  private CmsClient client;

  @BeforeEach
  void setUp() {
    var builder = RestClient.builder();
    server = MockRestServiceServer.bindTo(builder).build();
    client =
        new CmsClient(
            builder,
            new CmsProperties(
                URI.create("http://cms.test"), 20117L, Duration.ofSeconds(1), FAILURE_CACHE_TTL),
            nanos::get);
  }

  private void advanceTime(Duration duration) {
    nanos.addAndGet(duration.toNanos());
  }

  @Test
  void shouldMapNewsContentType() {
    server
        .expect(requestTo(URL))
        .andRespond(withSuccess("{\"contentType\":\"NEWS\"}", MediaType.APPLICATION_JSON));
    assertEquals(Optional.of(SisaltoTyyppi.UUTINEN), client.getSisaltoTyyppi("erc-1"));
    server.verify();
  }

  @Test
  void shouldMapArticleContentType() {
    server
        .expect(requestTo(URL))
        .andRespond(withSuccess("{\"contentType\":\"ARTICLE\"}", MediaType.APPLICATION_JSON));
    assertEquals(Optional.of(SisaltoTyyppi.ARTIKKELI), client.getSisaltoTyyppi("erc-1"));
  }

  @Test
  void shouldReturnEmptyWhenNotFound() {
    server.expect(requestTo(URL)).andRespond(withResourceNotFound());
    assertEquals(Optional.empty(), client.getSisaltoTyyppi("erc-1"));
  }

  @Test
  void shouldFailOnServerError() {
    server.expect(requestTo(URL)).andRespond(withServerError());
    assertThrows(CmsException.class, () -> client.getSisaltoTyyppi("erc-1"));
  }

  @Test
  void shouldFailWhenContentTypeIsMissingOrUnknown() {
    server
        .expect(requestTo(URL))
        .andRespond(withSuccess("{\"contentStructureId\":123}", MediaType.APPLICATION_JSON));
    assertThrows(CmsException.class, () -> client.getSisaltoTyyppi("erc-1"));
    server.verify();

    server.reset();
    advanceTime(FAILURE_CACHE_TTL.plusSeconds(1));
    server
        .expect(requestTo(URL))
        .andRespond(withSuccess("{\"contentType\":\"BLOG\"}", MediaType.APPLICATION_JSON));
    assertThrows(CmsException.class, () -> client.getSisaltoTyyppi("erc-1"));
    server.verify();
  }

  @Test
  void shouldCacheNotFoundUntilTtlExpires() {
    server.expect(requestTo(URL)).andRespond(withResourceNotFound());
    assertEquals(Optional.empty(), client.getSisaltoTyyppi("erc-1"));
    assertEquals(Optional.empty(), client.getSisaltoTyyppi("erc-1"));
    server.verify();

    server.reset();
    advanceTime(FAILURE_CACHE_TTL.plusSeconds(1));
    server
        .expect(requestTo(URL))
        .andRespond(withSuccess("{\"contentType\":\"NEWS\"}", MediaType.APPLICATION_JSON));
    assertEquals(Optional.of(SisaltoTyyppi.UUTINEN), client.getSisaltoTyyppi("erc-1"));
    server.verify();
  }

  @Test
  void shouldCacheErrorUntilTtlExpires() {
    server.expect(requestTo(URL)).andRespond(withServerError());
    assertThrows(CmsException.class, () -> client.getSisaltoTyyppi("erc-1"));
    assertThrows(CmsException.class, () -> client.getSisaltoTyyppi("erc-1"));
    server.verify();

    server.reset();
    advanceTime(FAILURE_CACHE_TTL.plusSeconds(1));
    server
        .expect(requestTo(URL))
        .andRespond(withSuccess("{\"contentType\":\"ARTICLE\"}", MediaType.APPLICATION_JSON));
    assertEquals(Optional.of(SisaltoTyyppi.ARTIKKELI), client.getSisaltoTyyppi("erc-1"));
    server.verify();
  }

  @Test
  void shouldNotCacheSuccess() {
    server
        .expect(times(2), requestTo(URL))
        .andRespond(withSuccess("{\"contentType\":\"ARTICLE\"}", MediaType.APPLICATION_JSON));
    client.getSisaltoTyyppi("erc-1");
    client.getSisaltoTyyppi("erc-1");
    server.verify();
  }
}
