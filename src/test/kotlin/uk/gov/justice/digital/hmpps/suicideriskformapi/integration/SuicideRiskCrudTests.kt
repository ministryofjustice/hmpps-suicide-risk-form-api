package uk.gov.justice.digital.hmpps.suicideriskformapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.suicideriskformapi.model.Address
import uk.gov.justice.digital.hmpps.suicideriskformapi.model.SuicideRisk
import uk.gov.justice.digital.hmpps.suicideriskformapi.repository.SuicideRiskRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime

class SuicideRiskCrudTests : IntegrationTestBase() {

  @Autowired
  private lateinit var suicideRiskRepository: SuicideRiskRepository

  @Test
  fun `should create a suicide risk`() {
    webTestClient.post()
      .uri("/suicide-risk")
      .headers(setAuthorisation(roles = listOf("ROLE_SUICIDE_RISK")))
      .bodyValue(SuicideRisk(crn = "X000001"))
      .exchange()
      .expectStatus()
      .isCreated

    val suicideRisk = suicideRiskRepository.findByCrn("X000001").single()
    assertThat(suicideRisk.crn).isEqualTo("X000001")
    assertThat(suicideRisk.id).isNotNull()
  }

  @Test
  fun `should update a Suicide Risk`() {
    webTestClient.post()
      .uri("/suicide-risk")
      .headers(setAuthorisation(roles = listOf("ROLE_SUICIDE_RISK")))
      .bodyValue(SuicideRisk(crn = "X000002"))
      .exchange()
      .expectStatus()
      .isCreated

    val suicideRisk = suicideRiskRepository.findByCrn("X000002").single()
    assertThat(suicideRisk.crn).isEqualTo("X000002")

    val suicideRiskBody = SuicideRisk(
      crn = "X000002",
      natureOfRisk = "Risky Business",
      riskIsGreatestWhen = "Risk Greatest",
      riskIncreasesWhen = "Bad Things Happen",
      riskDecreasesWhen = "Good Things Happen",
      sheetSentBy = "Joe Bloggs",
      titleAndFullName = "Mr Henry Bean",
      postalAddress = Address(
        addressId = 25,
        status = "Postal",
        officeDescription = null,
        buildingName = "MOO",
      ),
      completedDate = ZonedDateTime.now(),
      reviewEvent = "Merge",
      reviewRequiredDate = LocalDateTime.now(),
      basicDetailsSaved = true,
      informationSaved = false,
      contactSaved = false,
      treatmentSaved = false,
      signAndSendSaved = false,
      prisonNumber = "123456",
      dateOfLetter = LocalDate.now(),
      telephoneNumber = "01911234560",
      signature = "testsignature",
      additionalInfo = "someAdditionalInformation",
      currentPsychTreatment = "Frog removal",
      dateOfBirth = LocalDate.now(),
      workAddress = Address(
        addressId = 66,
        status = "Postal",
        officeDescription = "anOfficeDescription",
        buildingName = "MOO",
      ),
    )

    webTestClient.put()
      .uri("/suicide-risk/" + suicideRisk.id)
      .headers(setAuthorisation(roles = listOf("ROLE_SUICIDE_RISK")))
      .bodyValue(suicideRiskBody)
      .exchange()
      .expectStatus()
      .isOk

    val updatedSuicideRisk = suicideRiskRepository.findByCrn("X000002").single()
    assertThat(updatedSuicideRisk.crn).isEqualTo("X000002")
    assertThat(updatedSuicideRisk.reviewEvent).isEqualTo("Merge")
    assertThat(updatedSuicideRisk.currentPsychTreatment).isEqualTo("Frog removal")
    assertThat(updatedSuicideRisk.basicDetailsSaved).isEqualTo(true)
    assertThat(updatedSuicideRisk.workAddressEntity?.officeDescription).isEqualTo("anOfficeDescription")
  }

  @Test
  fun `should fail to create if the crn is too long`() {
    webTestClient.post()
      .uri("/suicide-risk")
      .headers(setAuthorisation(roles = listOf("ROLE_SUICIDE_RISK")))
      .bodyValue(SuicideRisk(crn = "X000001123456789123456"))
      .exchange()
      .expectStatus().isBadRequest
      .expectBody().jsonPath("$.userMessage").isEqualTo("""Field: crn - must match "^[A-Z][0-9]{6}"""")
  }

  @Test
  fun `should delete a suicide risk`() {
    webTestClient.post()
      .uri("/suicide-risk")
      .headers(setAuthorisation(roles = listOf("ROLE_SUICIDE_RISK")))
      .bodyValue(SuicideRisk(crn = "X000004"))
      .exchange()
      .expectStatus()
      .isCreated

    val suicideRisk = suicideRiskRepository.findByCrn("X000004")
    assertThat(suicideRisk.first().crn).isEqualTo("X000004")
    assertThat(suicideRisk.first().id).isNotNull()

    webTestClient.delete()
      .uri("/suicide-risk/" + suicideRisk.first().id)
      .headers(setAuthorisation(roles = listOf("ROLE_SUICIDE_RISK")))
      .exchange()
      .expectStatus()
      .isOk

    assertThat(suicideRiskRepository.findById(suicideRisk.first().id)).isNull()
  }
}
