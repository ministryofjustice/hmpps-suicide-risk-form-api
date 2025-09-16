package uk.gov.justice.digital.hmpps.suicideriskformapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.suicideriskformapi.entity.SuicideRiskEntity
import java.util.*

@Repository
interface SuicideRiskRepository : JpaRepository<SuicideRiskEntity, UUID> {
  fun findByCrn(crn: String): List<SuicideRiskEntity>
  fun deleteByCrn(crn: String)
  fun findByCrnAndCompletedDateIsNull(crn: String?): List<SuicideRiskEntity>
}
