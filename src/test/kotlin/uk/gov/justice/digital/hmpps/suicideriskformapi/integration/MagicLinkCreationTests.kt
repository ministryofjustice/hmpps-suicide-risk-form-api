package uk.gov.justice.digital.hmpps.suicideriskformapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.suicideriskformapi.model.Address
import uk.gov.justice.digital.hmpps.suicideriskformapi.model.SuicideRisk
import uk.gov.justice.digital.hmpps.suicideriskformapi.repository.SuicideRiskRepository
import uk.gov.justice.digital.hmpps.suicideriskformapi.service.MagicLinkService
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime

class MagicLinkCreationTests : IntegrationTestBase() {

  @Autowired
  lateinit var suicideRiskRepository: SuicideRiskRepository

  @Autowired
  lateinit var magicLinkService: MagicLinkService

  var superSecretKey = "super_secret_key"
  //val baseurl: String = "http://localhost:3000/suicide-risk/files/"
  val baseurl: String = "/files/"
  val oneMonthInSeconds: Long = 2628000


  @Test
  fun `should create a Suicide Risk and create a magic link`() {
    webTestClient.post()
      .uri("/suicide-risk")
      .headers(setAuthorisation(roles = listOf("ROLE_SUICIDE_RISK")))
      .bodyValue(SuicideRisk(crn = "X909099"))
      .exchange()
      .expectStatus()
      .isCreated

    val suicideRisk = suicideRiskRepository.findByCrn("X909099").single()
    val suicideRiskUuid = suicideRisk.id
    assertThat(suicideRisk.crn).isEqualTo("X909099")

    val suicideRiskBody = SuicideRisk(
      crn = "X909099",
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
      .uri("/suicide-risk/" + suicideRiskUuid)
      .headers(setAuthorisation(roles = listOf("ROLE_SUICIDE_RISK")))
      .bodyValue(suicideRiskBody)
      .exchange()
      .expectStatus()
      .isOk

    val updatedSuicideRisk = suicideRiskRepository.findByCrn("X909099").single()
    assertThat(updatedSuicideRisk.crn).isEqualTo("X909099")
    assertThat(updatedSuicideRisk.reviewEvent).isEqualTo("Merge")
    assertThat(updatedSuicideRisk.currentPsychTreatment).isEqualTo("Frog removal")
    assertThat(updatedSuicideRisk.basicDetailsSaved).isEqualTo(true)
    assertThat(updatedSuicideRisk.workAddressEntity?.officeDescription).isEqualTo("anOfficeDescription")

    val urlToEncode: String = baseurl + suicideRiskUuid

    // need to create a signed link then try and use it to retrieve the file
    val magicLink = magicLinkService.generateLink(urlToEncode, oneMonthInSeconds)
    assertThat(magicLink).isNotBlank
    assertThat(magicLink).contains("/files/"+suicideRiskUuid)

    // see if the magic link works without auth
    webTestClient.get()
      .uri(magicLink)
      .headers(setAuthorisation(roles = listOf("ROLE_SUICIDE_RISK")))
      .exchange()
      .expectStatus()
      .isOk
  }
}
