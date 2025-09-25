package uk.gov.justice.digital.hmpps.suicideriskformapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.suicideriskformapi.entity.ContactEntity
import java.util.*

@Repository
interface ContactRepository : JpaRepository<ContactEntity, UUID> {
  fun findFirstBySuicideRisk_IdAndId(suicideRiskId: UUID, contactId: UUID): ContactEntity?
}
