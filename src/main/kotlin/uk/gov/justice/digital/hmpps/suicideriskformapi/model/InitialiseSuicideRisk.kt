package uk.gov.justice.digital.hmpps.suicideriskformapi.model

import jakarta.validation.constraints.Pattern

data class InitialiseSuicideRisk(
  @field:Pattern(regexp = "^[A-Z][0-9]{6}")
  val crn: String,
)
