/*
 * Copyright (c) 2026 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.service.cms;

import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration of the CMS (Liferay) headless API.
 *
 * @param baseUrl base URL of the CMS; the API is under {@code <baseUrl>/o/headless-delivery}
 * @param siteId Liferay site id
 * @param timeout connect and read timeout
 * @param failureCacheTtl how long a failed lookup (not found or error) of an ERC is remembered
 *     before the CMS is queried again
 */
@ConfigurationProperties(prefix = "jod.cms")
@Validated
public record CmsProperties(
    @NotNull URI baseUrl,
    @NotNull Long siteId,
    @DefaultValue("2s") Duration timeout,
    @DefaultValue("1m") Duration failureCacheTtl) {}
