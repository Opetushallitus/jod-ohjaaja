/*
 * Copyright (c) 2025 The Finnish Ministry of Education and Culture, The Finnish
 * The Ministry of Economic Affairs and Employment, The Finnish National Agency of
 * Education (Opetushallitus) and The Finnish Development and Administration centre
 * for ELY Centres and TE Offices (KEHA).
 *
 * Licensed under the EUPL-1.2-or-later.
 */

package fi.okm.jod.ohjaaja.service;

import fi.okm.jod.ohjaaja.dto.PalauteViestiDto;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.arns.Arn;

@Service
@Profile("cloud")
public class PalauteKanavaService {
  private final SqsTemplate sqsTemplate;
  private final String queueUrl;

  public PalauteKanavaService(
      SqsTemplate sqsTemplate, @Value("${jod.feedback.queue-arn}") String queueArn) {
    this.sqsTemplate = sqsTemplate;
    this.queueUrl = arnToQueueUrl(queueArn);
  }

  public void sendMessage(PalauteViestiDto payload) {
    sqsTemplate.send(to -> to.queue(queueUrl).messageGroupId("ohjaaja").payload(payload));
  }

  private static String arnToQueueUrl(String arn) {
    Arn parsedArn = Arn.fromString(arn);
    String domain = "aws-cn".equals(parsedArn.partition()) ? "amazonaws.com.cn" : "amazonaws.com";

    return String.format(
        "https://sqs.%s.%s/%s/%s",
        parsedArn.region().orElseThrow(IllegalStateException::new),
        domain,
        parsedArn.accountId().orElseThrow(IllegalStateException::new),
        parsedArn.resourceAsString());
  }
}
