package uk.gov.justice.digital.hmpps.suicideriskformapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import software.amazon.awssdk.services.sns.model.MessageAttributeValue
import software.amazon.awssdk.services.sns.model.PublishRequest
import uk.gov.justice.digital.hmpps.suicideriskformapi.entity.SuicideRiskEntity
import uk.gov.justice.digital.hmpps.suicideriskformapi.model.SuicideRisk
import uk.gov.justice.digital.hmpps.suicideriskformapi.repository.SuicideRiskRepository
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

class DomainEventTest : IntegrationTestBase() {

  @Autowired
  private lateinit var suicideRiskRepository: SuicideRiskRepository

  @Nested
  @DisplayName("GET /suicide-risk/{parameter}")
  inner class SuicideRiskTestEntityEndpoint {

    @Test
    fun `merge event should update CRN for active suicide risk`() {
      webTestClient.post()
        .uri("/suicide-risk")
        .headers(setAuthorisation(roles = listOf("ROLE_SUICIDE_RISK","ROLE_TEMPLATE_KOTLIN__UI")))
        .bodyValue(SuicideRisk(crn = "X000101"))
        .exchange()
        .expectStatus()
        .isCreated

      val suicideRisk = suicideRiskRepository.findByCrn("X000101").single()
      assertThat(suicideRisk.crn).isEqualTo("X000101")
      assertThat(suicideRisk.id).isNotNull()

      val message = "{\"eventType\":\"probation-case.merge.completed\",\"version\":1,\"occurredAt\":\"2025-04-15T09:49:55.560241+01:00\",\"description\":\"A merge has been completed on the probation case\",\"additionalInformation\":{\"sourceCRN\":\"X000101\",\"targetCRN\":\"X000102\"},\"personReference\":{\"identifiers\":[{\"type\":\"CRN\",\"value\":\"X000102\"}]}}\n"

      val responseFuture = inboundSnsClient.publish(
        PublishRequest.builder().topicArn("arn:aws:sns:eu-west-2:000000000000:hmppssuicideriskformtopic").message(message).messageAttributes(
          mapOf("eventType" to MessageAttributeValue.builder().dataType("String").stringValue("probation-case.merge.completed").build()),
        ).build(),
      )
      val response = responseFuture.get(10, TimeUnit.SECONDS)

      assertThat(response.messageId()).isNotNull()

      Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted {
        val suicideRiskUpdated: SuicideRiskEntity = suicideRiskRepository.findById(suicideRisk.id).orElse(null)
        assertThat(suicideRiskUpdated).isNotNull
        assertThat(suicideRiskUpdated.crn).isEqualTo("X000102")
        assertThat(suicideRiskUpdated.id).isNotNull()
        assertThat(suicideRiskUpdated.reviewRequiredDate).isNotNull()
        assertThat(suicideRiskUpdated.reviewEvent).isEqualTo("MERGE")
      }
    }

    @Test
    fun `merge event should not update CRN for completed suicide risk`() {
      webTestClient.post()
        .uri("/suicide-risk")
        .headers(setAuthorisation(roles = listOf("ROLE_SUICIDE_RISK","ROLE_TEMPLATE_KOTLIN__UI")))
        .bodyValue(SuicideRisk(crn = "X000111"))
        .exchange()
        .expectStatus()
        .isCreated

      val suicideRisk = suicideRiskRepository.findByCrn("X000111").single()
      assertThat(suicideRisk.crn).isEqualTo("X000111")
      assertThat(suicideRisk.id).isNotNull()

      suicideRisk.completedDate = ZonedDateTime.now()
      suicideRiskRepository.save(suicideRisk)

      val message = "{\"eventType\":\"probation-case.merge.completed\",\"version\":1,\"occurredAt\":\"2025-04-15T09:49:55.560241+01:00\",\"description\":\"A merge has been completed on the probation case\",\"additionalInformation\":{\"sourceCRN\":\"X000111\",\"targetCRN\":\"X000102\"},\"personReference\":{\"identifiers\":[{\"type\":\"CRN\",\"value\":\"X000102\"}]}}\n"

      val responseFuture = inboundSnsClient.publish(
        PublishRequest.builder().topicArn("arn:aws:sns:eu-west-2:000000000000:hmppssuicideriskformtopic").message(message).messageAttributes(
          mapOf("eventType" to MessageAttributeValue.builder().dataType("String").stringValue("probation-case.merge.completed").build()),
        ).build(),
      )
      val response = responseFuture.get(10, TimeUnit.SECONDS)

      assertThat(response.messageId()).isNotNull()

      Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted {
        val suicideRiskUpdated: SuicideRiskEntity = suicideRiskRepository.findById(suicideRisk.id).orElse(null)
        assertThat(suicideRiskUpdated).isNotNull
        assertThat(suicideRiskUpdated.crn).isEqualTo("X000111")
        assertThat(suicideRiskUpdated.id).isNotNull()
        assertThat(suicideRiskUpdated.reviewRequiredDate).isNull()
        assertThat(suicideRiskUpdated.reviewEvent).isNull()
      }
    }

    @Test
    fun `unmerge event should update CRN for active suicide risk`() {
      webTestClient.post()
        .uri("/suicide-risk")
        .headers(setAuthorisation(roles = listOf("ROLE_SUICIDE_RISK","ROLE_TEMPLATE_KOTLIN__UI")))
        .bodyValue(SuicideRisk(crn = "X000121"))
        .exchange()
        .expectStatus()
        .isCreated

      val suicideRisk = suicideRiskRepository.findByCrn("X000121").single()
      assertThat(suicideRisk.crn).isEqualTo("X000121")
      assertThat(suicideRisk.id).isNotNull()

      // language=json
      val message = """{
        "eventType":"probation-case.unmerge.completed",
        "version":1,
        "occurredAt":"2025-04-15T09:49:55.560241+01:00",
        "description":"An unmerge has been completed on the probation case",
        "additionalInformation":{
          "reactivatedCRN":"X000103",
          "unmergedCRN":"X000121"
        },
        "personReference":{
          "identifiers":[
            {
              "type":"CRN",
              "value":"X000121"
            }
          ]
        }
      }
      """.trimIndent()

      val responseFuture = inboundSnsClient.publish(
        PublishRequest.builder().topicArn("arn:aws:sns:eu-west-2:000000000000:hmppssuicideriskformtopic").message(message).messageAttributes(
          mapOf("eventType" to MessageAttributeValue.builder().dataType("String").stringValue("probation-case.unmerge.completed").build()),
        ).build(),
      )
      val response = responseFuture.get(10, TimeUnit.SECONDS)

      assertThat(response.messageId()).isNotNull()

      Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted {
        val suicideRiskUpdated: SuicideRiskEntity = suicideRiskRepository.findById(suicideRisk.id).orElse(null)
        assertThat(suicideRiskUpdated).isNotNull
        assertThat(suicideRiskUpdated.crn).isEqualTo("X000103")
        assertThat(suicideRiskUpdated.id).isNotNull()
        assertThat(suicideRiskUpdated.reviewRequiredDate).isNotNull()
        assertThat(suicideRiskUpdated.reviewEvent).isEqualTo("UNMERGE")
      }
    }

    @Test
    fun `unmerge event should not update CRN for active suicide risk`() {
      webTestClient.post()
        .uri("/suicide-risk")
        .headers(setAuthorisation(roles = listOf("ROLE_SUICIDE_RISK","ROLE_TEMPLATE_KOTLIN__UI")))
        .bodyValue(SuicideRisk(crn = "X000131"))
        .exchange()
        .expectStatus()
        .isCreated

      val suicideRisk = suicideRiskRepository.findByCrn("X000131").single()
      assertThat(suicideRisk.crn).isEqualTo("X000131")
      assertThat(suicideRisk.id).isNotNull()

      suicideRisk.completedDate = ZonedDateTime.now()
      suicideRiskRepository.save(suicideRisk)

      val message: String = "{\n" +
        "  \"eventType\":\"probation-case.unmerge.completed\",\n" +
        "  \"version\":1,\n" +
        "  \"occurredAt\":\"2025-04-15T09:49:55.560241+01:00\",\n" +
        "  \"description\":\"An unmerge has been completed on the probation case\",\n" +
        "  \"additionalInformation\":{\n" +
        "    \"reactivatedCRN\":\"X000103\",\n" +
        "    \"unmergedCRN\":\"X000131\"},\n" +
        "  \"personReference\":{\n" +
        "    \"identifiers\":[\n" +
        "      {\n" +
        "        \"type\":\"CRN\",\n" +
        "        \"value\":\"X000131\"\n" +
        "      }\n" +
        "    ]\n" +
        "  }\n" +
        "}"

      val responseFuture = inboundSnsClient.publish(
        PublishRequest.builder().topicArn("arn:aws:sns:eu-west-2:000000000000:hmppssuicideriskformtopic").message(message).messageAttributes(
          mapOf("eventType" to MessageAttributeValue.builder().dataType("String").stringValue("probation-case.unmerge.completed").build()),
        ).build(),
      )
      val response = responseFuture.get(10, TimeUnit.SECONDS)

      assertThat(response.messageId()).isNotNull()

      Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted {
        val suicideRiskUpdated: SuicideRiskEntity = suicideRiskRepository.findById(suicideRisk.id).orElse(null)
        assertThat(suicideRiskUpdated).isNotNull
        assertThat(suicideRiskUpdated.crn).isEqualTo("X000131")
        assertThat(suicideRiskUpdated.id).isNotNull()
        assertThat(suicideRiskUpdated.reviewRequiredDate).isNull()
        assertThat(suicideRiskUpdated.reviewEvent).isNull()
      }
    }

    @Test
    fun `move event should update CRN for active suicide risk`() {
      webTestClient.post()
        .uri("/suicide-risk")
        .headers(setAuthorisation(roles = listOf("ROLE_SUICIDE_RISK","ROLE_TEMPLATE_KOTLIN__UI")))
        .bodyValue(SuicideRisk(crn = "X000141"))
        .exchange()
        .expectStatus()
        .isCreated

      val suicideRisk = suicideRiskRepository.findByCrn("X000141").single()
      assertThat(suicideRisk.crn).isEqualTo("X000141")
      assertThat(suicideRisk.id).isNotNull()

      val message = "{\"eventType\":\"probation-case.sentence.moved\",\"version\":1,\"occurredAt\":\"2025-04-15T09:49:55.560241+01:00\",\"description\":\"A merge has been completed on the probation case\",\"additionalInformation\":{\"sourceCRN\":\"X000141\",\"targetCRN\":\"X000102\"},\"personReference\":{\"identifiers\":[{\"type\":\"CRN\",\"value\":\"X000102\"}]}}\n"

      val responseFuture = inboundSnsClient.publish(
        PublishRequest.builder().topicArn("arn:aws:sns:eu-west-2:000000000000:hmppssuicideriskformtopic").message(message).messageAttributes(
          mapOf("eventType" to MessageAttributeValue.builder().dataType("String").stringValue("probation-case.sentence.moved").build()),
        ).build(),
      )
      val response = responseFuture.get(10, TimeUnit.SECONDS)

      assertThat(response.messageId()).isNotNull()

      Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted {
        val suicideRiskUpdated: SuicideRiskEntity = suicideRiskRepository.findById(suicideRisk.id).orElse(null)
        assertThat(suicideRiskUpdated).isNotNull
        assertThat(suicideRiskUpdated.crn).isEqualTo("X000103")
        assertThat(suicideRiskUpdated.id).isNotNull()
        assertThat(suicideRiskUpdated.reviewRequiredDate).isNotNull()
        assertThat(suicideRiskUpdated.reviewEvent).isEqualTo("EVENT_MOVE")
      }
    }

    @Test
    fun `move event should not update CRN for completed suicide risk`() {
      webTestClient.post()
        .uri("/suicide-risk")
        .headers(setAuthorisation(roles = listOf("ROLE_SUICIDE_RISK","ROLE_TEMPLATE_KOTLIN__UI")))
        .bodyValue(SuicideRisk(crn = "X000151"))
        .exchange()
        .expectStatus()
        .isCreated

      val suicideRisk = suicideRiskRepository.findByCrn("X000151").single()
      assertThat(suicideRisk.crn).isEqualTo("X000151")
      assertThat(suicideRisk.id).isNotNull()

      suicideRisk.completedDate = ZonedDateTime.now()
      suicideRiskRepository.save(suicideRisk)

      val message = "{\"eventType\":\"probation-case.sentence.moved\",\"version\":1,\"occurredAt\":\"2025-04-15T09:49:55.560241+01:00\",\"description\":\"A merge has been completed on the probation case\",\"additionalInformation\":{\"sourceCRN\":\"X000151\",\"targetCRN\":\"X000102\"},\"personReference\":{\"identifiers\":[{\"type\":\"CRN\",\"value\":\"X000102\"}]}}\n"

      val responseFuture = inboundSnsClient.publish(
        PublishRequest.builder().topicArn("arn:aws:sns:eu-west-2:000000000000:hmppssuicideriskformtopic").message(message).messageAttributes(
          mapOf("eventType" to MessageAttributeValue.builder().dataType("String").stringValue("probation-case.sentence.moved").build()),
        ).build(),
      )
      val response = responseFuture.get(10, TimeUnit.SECONDS)

      assertThat(response.messageId()).isNotNull()

      Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted {
        val suicideRiskUpdated: SuicideRiskEntity = suicideRiskRepository.findById(suicideRisk.id).orElse(null)
        assertThat(suicideRiskUpdated).isNotNull
        assertThat(suicideRiskUpdated.crn).isEqualTo("X000151")
        assertThat(suicideRiskUpdated.id).isNotNull()
        assertThat(suicideRiskUpdated.reviewRequiredDate).isNull()
        assertThat(suicideRiskUpdated.reviewEvent).isNull()
      }
    }

    @Test
    fun `gdpr event should remove all suicide risks`() {
      webTestClient.post()
        .uri("/suicide-risk")
        .headers(setAuthorisation(roles = listOf("ROLE_SUICIDE_RISK","ROLE_TEMPLATE_KOTLIN__UI")))
        .bodyValue(SuicideRisk(crn = "X000161"))
        .exchange()
        .expectStatus()
        .isCreated

      val suicideRisk = suicideRiskRepository.findByCrn("X000161").single()
      assertThat(suicideRisk.crn).isEqualTo("X000161")
      assertThat(suicideRisk.id).isNotNull()

      val message = "{\"eventType\":\"probation-case.deleted.gdpr\",\"version\":1,\"occurredAt\":\"2025-04-15T09:49:55.560241+01:00\",\"description\":\"A merge has been completed on the probation case\",\"personReference\":{\"identifiers\":[{\"type\":\"CRN\",\"value\":\"X000102\"}]}}\n"

      val responseFuture = inboundSnsClient.publish(
        PublishRequest.builder().topicArn("arn:aws:sns:eu-west-2:000000000000:hmppssuicideriskformtopic").message(message).messageAttributes(
          mapOf("eventType" to MessageAttributeValue.builder().dataType("String").stringValue("probation-case.deleted.gdpr").build()),
        ).build(),
      )
      val response = responseFuture.get(10, TimeUnit.SECONDS)

      assertThat(response.messageId()).isNotNull()

      Awaitility.await().atMost(10, TimeUnit.SECONDS).untilAsserted {
        val suicideRiskRefresh = suicideRiskRepository.findByCrn("X000141")
        assertThat(suicideRiskRefresh).isEmpty()
      }
    }
  }
}
