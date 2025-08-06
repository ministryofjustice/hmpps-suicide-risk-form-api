package uk.gov.justice.digital.hmpps.suicideriskformapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ContentDisposition
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.suicideriskformapi.model.SuicideRisk
import uk.gov.justice.digital.hmpps.suicideriskformapi.repository.SuicideRiskRepository
import java.time.Duration
import java.util.UUID

class PdfGenerationTests : IntegrationTestBase() {

  @Autowired
  private lateinit var suicideRiskRepository: SuicideRiskRepository

  @Test
  fun `get PDF should return a 200 response`() {
    webTestClient.post()
      .uri("/suicide-risk")
      .headers(setAuthorisation(roles = listOf("ROLE_SUICIDE_RISK")))
      .bodyValue(SuicideRisk(crn = "X800001"))
      .exchange()
      .expectStatus()
      .isCreated

    val suicideRisk = suicideRiskRepository.findByCrn("X800001")
    assertThat(suicideRisk.first().crn).isEqualTo("X800001")

    webTestClient
      .mutate().responseTimeout(Duration.ofSeconds(30)).build()
      .get()
      .uri("/suicide-risk/" + suicideRisk[0].id + "/pdf")
      .headers(setAuthorisation(roles = listOf("ROLE_SUICIDE_RISK")))
      .exchange()
      .expectStatus()
      .isOk
      .expectHeader()
      .contentType(MediaType.APPLICATION_PDF)
      .expectHeader()
      .contentDisposition(ContentDisposition.attachment().filename("Suicide_Risk_Form_" + suicideRisk[0].crn + ".pdf").build())
  }

  @Test
  fun `get PDF should return a 404 response if breach not found`() {
    webTestClient.post()
      .uri("/suicide-risk")
      .headers(setAuthorisation(roles = listOf("ROLE_SUICIDE_RISK")))
      .bodyValue(SuicideRisk(crn = "X800002"))
      .exchange()
      .expectStatus()
      .isCreated

    val suicideRisk = suicideRiskRepository.findByCrn("X800002")
    assertThat(suicideRisk.first().crn).isEqualTo("X800002")

    webTestClient.get()
      .uri("/suicide-risk/" + UUID.randomUUID() + "/pdf")
      .headers(setAuthorisation(roles = listOf("ROLE_SUICIDE_RISK")))
      .exchange()
      .expectStatus().isNotFound
  }
}
