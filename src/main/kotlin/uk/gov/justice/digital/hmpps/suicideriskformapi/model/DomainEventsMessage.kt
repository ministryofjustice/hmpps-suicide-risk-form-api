package uk.gov.justice.digital.hmpps.suicideriskformapi.model

import com.fasterxml.jackson.annotation.JsonIgnore
import java.time.ZonedDateTime

data class DomainEventsMessage(
  val eventType: String,
  val description: String,
  val personReference: PersonReference,
  val occurredAt: ZonedDateTime,
  val version: Long?,
  val detailUrl: String?,
  val additionalInformation: Map<String, Any>? = mapOf(),
) {
  @JsonIgnore
  val crn = personReference.identifiers.firstOrNull { it.type == "CRN" }?.value

  @JsonIgnore
  val sourceCrn = additionalInformation?.get("sourceCRN") as String?

  @JsonIgnore
  val targetCrn = additionalInformation?.get("targetCRN") as String?

  @JsonIgnore
  val unmergedCrn = additionalInformation?.get("unmergedCRN") as String?

  @JsonIgnore
  val reactivatedCrn = additionalInformation?.get("reactivatedCRN") as String?
}

data class PersonReference(
  val identifiers: List<Identifiers>,
)

data class Identifiers(
  val type: String,
  val value: String,
)
