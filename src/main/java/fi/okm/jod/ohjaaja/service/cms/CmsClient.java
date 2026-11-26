/*
 * Copyright (c) 2026 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.service.cms;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;
import fi.okm.jod.ohjaaja.entity.SisaltoTyyppi;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Client for the CMS (Liferay) headless delivery API.
 *
 * <p>Failed lookups (content not found or an error) are remembered for a short time, so that
 * repeated views of the same content do not query the CMS again on every request.
 */
@Component
public class CmsClient {

  static final String STRUCTURED_CONTENT_BY_ERC =
      "/o/headless-delivery/v1.0/sites/{siteId}/structured-contents/"
          + "by-external-reference-code/{erc}?fields=contentType";

  private static final int FAILURE_CACHE_MAX_SIZE = 10_000;

  private final RestClient restClient;
  private final long siteId;
  private final Cache<String, Failure> failures;

  @Autowired
  public CmsClient(CmsProperties properties) {
    this(
        RestClient.builder().requestFactory(requestFactory(properties.timeout())),
        properties,
        Ticker.systemTicker());
  }

  CmsClient(RestClient.Builder builder, CmsProperties properties, Ticker ticker) {
    this.restClient =
        builder
            .baseUrl(properties.baseUrl().toString())
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .build();
    this.siteId = properties.siteId();
    this.failures =
        Caffeine.newBuilder()
            .expireAfterWrite(properties.failureCacheTtl())
            .maximumSize(FAILURE_CACHE_MAX_SIZE)
            .ticker(ticker)
            .build();
  }

  private static JdkClientHttpRequestFactory requestFactory(Duration timeout) {
    var httpClient = HttpClient.newBuilder().connectTimeout(timeout).build();
    var requestFactory = new JdkClientHttpRequestFactory(httpClient);
    requestFactory.setReadTimeout(timeout);
    return requestFactory;
  }

  /**
   * Fetches the type of a structured content by its external reference code.
   *
   * @return the content type, or empty if the CMS does not have content with the ERC
   * @throws CmsException if the CMS cannot be reached, returns an error, or the content type is
   *     missing or unknown
   */
  public Optional<SisaltoTyyppi> getSisaltoTyyppi(String erc) {
    var cached = failures.getIfPresent(erc);
    if (cached != null) {
      if (cached.error() == null) {
        return Optional.empty();
      }
      throw new CmsException(cached.error() + " (cached)");
    }
    try {
      var result = fetchSisaltoTyyppi(erc);
      if (result.isEmpty()) {
        failures.put(erc, Failure.NOT_FOUND);
      }
      return result;
    } catch (CmsException e) {
      failures.put(erc, new Failure(e.getMessage()));
      throw e;
    }
  }

  private Optional<SisaltoTyyppi> fetchSisaltoTyyppi(String erc) {
    try {
      return restClient
          .get()
          .uri(STRUCTURED_CONTENT_BY_ERC, siteId, erc)
          .exchange(
              (_, response) -> {
                var status = response.getStatusCode();
                if (status.isSameCodeAs(HttpStatus.NOT_FOUND)) {
                  return Optional.empty();
                }
                if (!status.is2xxSuccessful()) {
                  throw new CmsException("CMS responded with status " + status.value());
                }
                var content = response.bodyTo(StructuredContent.class);
                return Optional.of(toSisaltoTyyppi(content));
              });
    } catch (RestClientException e) {
      throw new CmsException("Fetching content type from CMS failed", e);
    }
  }

  private static SisaltoTyyppi toSisaltoTyyppi(StructuredContent content) {
    var contentType = content == null ? null : content.contentType();
    if (contentType == null) {
      throw new CmsException("CMS response is missing contentType");
    }
    return switch (contentType) {
      case "ARTICLE" -> SisaltoTyyppi.ARTIKKELI;
      case "NEWS" -> SisaltoTyyppi.UUTINEN;
      default -> throw new CmsException("Unknown contentType " + contentType);
    };
  }

  record StructuredContent(String contentType) {}

  /** A failed lookup; {@code error} is null when the content was not found. */
  private record Failure(String error) {
    static final Failure NOT_FOUND = new Failure(null);
  }
}
