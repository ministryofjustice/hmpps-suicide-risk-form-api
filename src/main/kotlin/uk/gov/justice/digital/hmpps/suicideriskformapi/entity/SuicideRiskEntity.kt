package uk.gov.justice.digital.hmpps.suicideriskformapi.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.*

@Entity
@Table(name = "suicide_risk")
@EntityListeners(AuditingEntityListener::class)
data class SuicideRiskEntity(
  @Id
  val id: UUID = UUID.randomUUID(),
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
  @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true)
  @JoinColumn(name = "postal_address_id", unique = true)
  var postalAddressEntity: AddressEntity? = null,
  var dateOfBirth: LocalDate? = null,
  var prisonNumber: String? = null,
  @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true)
  @JoinColumn(name = "work_address_id", unique = true)
  var workAddressEntity: AddressEntity? = null,
  var basicDetailsSaved: Boolean? = null,
  var informationSaved: Boolean? = null,
  var treatmentSaved: Boolean? = null,
  var signAndSendSaved: Boolean? = null,
  var contactSaved: Boolean? = null,
  var reviewRequiredDate: LocalDateTime? = null,
  var reviewEvent: String? = null,
  @OneToMany(cascade = [CascadeType.ALL], orphanRemoval = true, mappedBy = "suicideRisk")
  val suicideRiskContactList: List<ContactEntity> = emptyList(),
  @CreatedBy
  var createdByUser: String? = null,
  @CreatedDate
  var createdDatetime: LocalDateTime? = null,
  @LastModifiedDate
  var lastUpdatedDatetime: LocalDateTime? = null,
  @LastModifiedBy
  var lastUpdatedUser: String? = null,
)
