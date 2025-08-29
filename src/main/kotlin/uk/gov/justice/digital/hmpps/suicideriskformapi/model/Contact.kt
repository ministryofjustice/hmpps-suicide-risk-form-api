package uk.gov.justice.digital.hmpps.suicideriskformapi.model

import java.time.LocalDateTime
import java.util.*

data class Contact(
  val id: UUID? = null,
  val contactDate: LocalDateTime? = null,
  val contactTypeDescription: String? = null,
  val contactPerson: String? = null,
  val contactLocation: Address? = null,
  val formSent: Boolean? = null,
  val emailAddress: String? = null,
)
