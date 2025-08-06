package uk.gov.justice.digital.hmpps.suicideriskformapi.model

import com.fasterxml.jackson.annotation.JsonProperty

data class Address(
  val addressId: Long?,
  val status: String? = null,
  val officeDescription: String? = null,
  val buildingName: String? = null,
  @JsonProperty("buildingNumber")
  val addressNumber: String? = null,
  val streetName: String? = null,
  val district: String? = null,
  val townCity: String? = null,
  val county: String? = null,
  val postcode: String? = null,
)
