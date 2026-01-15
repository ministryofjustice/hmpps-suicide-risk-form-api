package uk.gov.justice.digital.hmpps.suicideriskformapi.listener
import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.awspring.cloud.sqs.annotation.SqsListener
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.suicideriskformapi.entity.SuicideRiskEntity
import uk.gov.justice.digital.hmpps.suicideriskformapi.enums.ReviewEventType
import uk.gov.justice.digital.hmpps.suicideriskformapi.model.DomainEventsMessage
import uk.gov.justice.digital.hmpps.suicideriskformapi.service.NDeliusIntegrationService
import uk.gov.justice.digital.hmpps.suicideriskformapi.service.SuicideRiskService
import java.time.ZonedDateTime

@Service
class DomainEventsListener(
  private val suicideRiskService: SuicideRiskService,
  private val objectMapper: ObjectMapper,
  private val nDeliusIntegrationService: NDeliusIntegrationService,
) {

  @Transactional
  @SqsListener("hmppssuicideriskformqueue", factory = "hmppsQueueContainerFactoryProxy")
  fun listen(msg: String) {
    val (message, attributes) = objectMapper.readValue<SQSMessage>(msg)
    val domainEventMessage = objectMapper.readValue<DomainEventsMessage>(message)
    handleMessage(domainEventMessage)
  }

  private fun handleMessage(message: DomainEventsMessage) {
    when (message.eventType) {
      "probation-case.merge.completed" -> {
        // Update CRNs where appropriate
        val breachNotices = suicideRiskService.getActiveSuicideRisksForCrn(message.sourceCrn)
        breachNotices.forEach {
          suicideRiskService.updateSuicideRiskCrn(it, requireNotNull(message.targetCrn))
        }

        updateReviewEvent(ReviewEventType.MERGE, breachNotices, message.occurredAt)
      }

      "probation-case.unmerge.completed" -> {
        // Update CRNs where appropriate
        val suicideRisks = suicideRiskService.getActiveSuicideRisksForCrn(message.unmergedCrn)
        suicideRisks.forEach {
          nDeliusIntegrationService.getCrnForSuicideRiskUuid(it.id.toString())?.crn?.let { crn ->
            suicideRiskService.updateSuicideRiskCrn(
              it,
              crn,
            )
          }
        }

        updateReviewEvent(ReviewEventType.UNMERGE, suicideRisks, message.occurredAt)
      }

      "probation-case.sentence.moved" -> {
        // Update CRNs where appropriate
        val suicideRisks = suicideRiskService.getActiveSuicideRisksForCrn(message.sourceCrn)
        suicideRisks.forEach {
          nDeliusIntegrationService.getCrnForSuicideRiskUuid(it.id.toString())?.crn?.let { crn ->
            suicideRiskService.updateSuicideRiskCrn(
              it,
              crn,
            )
          }
        }

        updateReviewEvent(ReviewEventType.EVENT_MOVE, suicideRisks, message.occurredAt)
      }

      "probation-case.deleted.gdpr" -> {
        message.crn?.let { suicideRiskService.deleteAllByCrn(it) }
      }

      "probation-case.non-statutory-intervention.moved" -> {
        // Update CRNs where appropriate
        val suicideRisks = suicideRiskService.getActiveSuicideRisksForCrn(message.sourceCrn)
        suicideRisks.forEach {
          nDeliusIntegrationService.getCrnForSuicideRiskUuid(it.id.toString())?.crn?.let { crn ->
            suicideRiskService.updateSuicideRiskCrn(
              it,
              crn,
            )
          }
        }

        updateReviewEvent(ReviewEventType.MOVE_NSI, suicideRisks, message.occurredAt)
      }
    }
  }

  private fun updateReviewEvent(eventType: ReviewEventType, breachNotices: Collection<SuicideRiskEntity>, occurredAt: ZonedDateTime) {
    breachNotices.forEach { breachNotice -> suicideRiskService.updateReviewEvent(eventType, breachNotice, occurredAt) }
  }
}

data class SQSMessage(
  @JsonProperty("Message") val message: String,
  @JsonProperty("MessageAttributes") val attributes: MessageAttributes = MessageAttributes(),
)

data class MessageAttributes(
  @JsonAnyGetter @JsonAnySetter
  private val attributes: MutableMap<String, MessageAttribute> = mutableMapOf(),
) : MutableMap<String, MessageAttribute> by attributes {

  val eventType = attributes[EVENT_TYPE_KEY]?.value

  companion object {
    private const val EVENT_TYPE_KEY = "eventType"
  }
}

data class MessageAttribute(@JsonProperty("Type") val type: String, @JsonProperty("Value") val value: String)
