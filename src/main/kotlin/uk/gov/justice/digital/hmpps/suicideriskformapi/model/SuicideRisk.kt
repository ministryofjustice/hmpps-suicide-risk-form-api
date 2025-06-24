package uk.gov.justice.digital.hmpps.suicideriskformapi.model

import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import jakarta.validation.constraints.Pattern
import uk.gov.justice.digital.hmpps.suicideriskformapi.entity.AddressEntity
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.*

data class SuicideRisk(
  @field:Pattern(regexp = "^[A-Z][0-9]{6}")
  var crn: String,
  var titleAndFullName: String? = null,
  var dateOfLetter: LocalDate? = null,
  var sheetSentBy: String? = null,
  var telephoneNumber: String? = null,
  var signature: String? = null,
  var completedDate: ZonedDateTime? = null,
  var natureOfRisk: String? = null,
  var riskIsGreatestWhen: String? = null,
  var riskIncreasesWhen: String? =null,
  var riskDecreasesWhen: String? = null,
  var additionalInfo: String? = null,
  var currentPsychTreatment: String? = null,
  var postalAddress: Address? = null,
  var dateOfBirth: LocalDate? = null,
  var prisonNumber: String? = null,
  var workAddress: Address? = null,
  var basicDetailsSaved: Boolean? = null,
  var informationSaved: Boolean? = null,
  var treatmentSaved: Boolean? = null,
  var signAndSendSaved: Boolean? = null,
  var contactSaved: Boolean? = null,
  var reviewRequiredDate: LocalDateTime? = null,
  var reviewEvent: String? = null,
  @field:JsonSetter(nulls = Nulls.AS_EMPTY)
  var suicideRiskContactList: List<Contact> = emptyList()
)
