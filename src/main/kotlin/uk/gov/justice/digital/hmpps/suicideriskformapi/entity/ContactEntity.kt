package uk.gov.justice.digital.hmpps.suicideriskformapi.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "contact")
@EntityListeners(AuditingEntityListener::class)
data class ContactEntity(
  @Id
  val id: UUID = UUID.randomUUID(),
  @JoinColumn(name = "suicide_risk_id")
  @ManyToOne
  var suicideRisk: SuicideRiskEntity? = null,
  val contactTypeDescription: String? = null,
  val contactPerson: String? = null,
  @OneToOne(cascade = [CascadeType.ALL], orphanRemoval = true)
  @JoinColumn(name = "contact_location_id", unique = true)
  val contactLocation: AddressEntity? = null,
  val formSent: Boolean? = null,
  val emailAddress: String? = null,
  @CreatedBy
  var createdByUser: String? = null,
  @CreatedDate
  var createdDatetime: LocalDateTime? = null,
  @LastModifiedDate
  var lastUpdatedDatetime: LocalDateTime? = null,
  @LastModifiedBy
  var lastUpdatedUser: String? = null,
)
