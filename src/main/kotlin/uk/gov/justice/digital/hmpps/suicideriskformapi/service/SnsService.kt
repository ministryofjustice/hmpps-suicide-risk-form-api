package uk.gov.justice.digital.hmpps.suicideriskformapi.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.messaging.MessagingException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import software.amazon.awssdk.services.sns.model.MessageAttributeValue
import software.amazon.awssdk.services.sns.model.PublishRequest
import uk.gov.justice.digital.hmpps.suicideriskformapi.model.DomainEventsMessage
import uk.gov.justice.digital.hmpps.suicideriskformapi.model.Identifiers
import uk.gov.justice.digital.hmpps.suicideriskformapi.model.PersonReference
import uk.gov.justice.digital.hmpps.suicideriskformapi.model.SuicideRisk
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.MissingQueueException
import java.time.ZonedDateTime
import java.util.UUID
import java.util.concurrent.TimeUnit

@Service
class SnsService(
  val hmppsQueueService: HmppsQueueService,
  val objectMapper: ObjectMapper,
  @Value("\${hmpps.sqs.topics.hmppssuicideriskformpublishtopic.arn}") val outboundTopicArn: String,
) {
  fun sendPublishDomainEvent(breachNotice: SuicideRisk, id: UUID) {
    val outboundTopic = hmppsQueueService.findByTopicId("hmppssuicideriskformpublishtopic") ?: throw MissingQueueException("HmppsTopic hmppssuicideriskformpublishtopic not found")
    val messageObject = DomainEventsMessage(
      description = "A suicide risk form has been completed for a person on probation",
      version = 1,
      occurredAt = ZonedDateTime.now(),
      eventType = "probation-case.suicide-risk-form.created",
      personReference = PersonReference(listOf(Identifiers(type = "crn", value = breachNotice.crn))),
      detailUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString() + "/suicide-risk/" + id + "/pdf",
      additionalInformation = mapOf(
        "suicideRiskId" to id,
        "username" to SecurityContextHolder.getContext().authentication.name,
      ),

    )
    val publishResponse = outboundTopic.snsClient.publish(
      PublishRequest.builder().topicArn(outboundTopicArn).message(objectMapper.writeValueAsString(messageObject)).messageAttributes(
        mapOf("eventType" to MessageAttributeValue.builder().dataType("String").stringValue("probation-case.suicide-risk-form.created").build()),
      ).build(),
    )

    publishResponse.get(5, TimeUnit.SECONDS).messageId() ?: throw MessagingException("Unable to publish creation message")
  }
}
