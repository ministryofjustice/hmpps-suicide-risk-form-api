package uk.gov.justice.digital.hmpps.suicideriskformapi.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.suicideriskformapi.entity.AddressEntity
import uk.gov.justice.digital.hmpps.suicideriskformapi.entity.ContactEntity
import uk.gov.justice.digital.hmpps.suicideriskformapi.entity.SuicideRiskEntity
import uk.gov.justice.digital.hmpps.suicideriskformapi.enums.ReviewEventType
import uk.gov.justice.digital.hmpps.suicideriskformapi.exception.NotFoundException
import uk.gov.justice.digital.hmpps.suicideriskformapi.model.Address
import uk.gov.justice.digital.hmpps.suicideriskformapi.model.Contact
import uk.gov.justice.digital.hmpps.suicideriskformapi.model.CreateResponse
import uk.gov.justice.digital.hmpps.suicideriskformapi.model.InitialiseSuicideRisk
import uk.gov.justice.digital.hmpps.suicideriskformapi.model.SuicideRisk
import uk.gov.justice.digital.hmpps.suicideriskformapi.repository.SuicideRiskRepository
import java.time.ZonedDateTime
import java.util.*

@Service
class SuicideRiskService(
  val suicideRiskRepository: SuicideRiskRepository,
  val pdfGenerationService: PdfGenerationService,
  @Value("\${frontend.url}") val frontendUrl: String,
) {

  @Transactional
  fun initialiseSuicideRisk(initialiseSuicideRisk: InitialiseSuicideRisk) = suicideRiskRepository.save(
    SuicideRiskEntity(crn = initialiseSuicideRisk.crn),
  ).id.let {
    CreateResponse(it, "$frontendUrl/basic-details/$it")
  }

  fun findSuicideRiskById(id: UUID): SuicideRisk {
    val suicideRiskEntity: SuicideRiskEntity = suicideRiskRepository.findByIdOrNull(id) ?: throw NotFoundException(
      "SuicideRiskEntity",
      "id",
      id,
    )
    return suicideRiskEntity.toModel()
  }

  @Transactional
  fun updateSuicideRisk(id: UUID, suicideRisk: SuicideRisk): SuicideRisk {
    val suicideRiskEntity: SuicideRiskEntity = suicideRiskRepository.findByIdOrNull(id) ?: throw NotFoundException("SuicideRiskEntity", "id", id)
    return suicideRiskRepository.save(suicideRisk.toEntity(suicideRiskEntity)).toModel()
  }

  @Transactional
  fun deleteSuicideRisk(id: UUID): Any? {
    if (!suicideRiskRepository.existsById(id)) {
      throw NotFoundException("SuicideRiskEntity", "id", id)
    }

    return suicideRiskRepository.deleteById(id)
  }

  fun getSuicideRiskAsPdf(id: UUID, suicideRisk: SuicideRisk?, draft: Boolean): ByteArray? {
    val html = pdfGenerationService.generateHtml(suicideRisk)

    var pdfBytes = pdfGenerationService.generatePdf(html)

    if (draft) {
      pdfBytes = pdfGenerationService.addWatermark(pdfBytes)
    }

    return pdfBytes
  }

  private fun SuicideRisk.toEntity(existingEntity: SuicideRiskEntity? = null) = existingEntity?.copy(
    crn = crn,
    titleAndFullName = titleAndFullName,
    dateOfLetter = dateOfLetter,
    sheetSentBy = sheetSentBy,
    telephoneNumber = telephoneNumber,
    signature = signature,
    completedDate = completedDate,
    natureOfRisk = natureOfRisk,
    riskIsGreatestWhen = riskIsGreatestWhen,
    riskIncreasesWhen = riskIncreasesWhen,
    riskDecreasesWhen = riskDecreasesWhen,
    additionalInfo = additionalInfo,
    currentPsychTreatment = currentPsychTreatment,
    postalAddressEntity = postalAddress?.toEntity(),
    dateOfBirth = dateOfBirth,
    prisonNumber = prisonNumber,
    workAddressEntity = workAddress?.toEntity(),
    basicDetailsSaved = basicDetailsSaved,
    informationSaved = informationSaved,
    treatmentSaved = treatmentSaved,
    signAndSendSaved = signAndSendSaved,
    contactSaved = contactSaved,
    reviewRequiredDate = reviewRequiredDate,
    reviewEvent = reviewEvent,
    suicideRiskContactList = suicideRiskContactList.map {
      it.toEntity(
        existingEntity.suicideRiskContactList.find { existingContactEnitiy ->
          existingContactEnitiy.id == it.id
        },
      )
    },
  )?.also { suicideRisk ->
    suicideRisk.suicideRiskContactList.forEach { it.suicideRisk = suicideRisk }
  } ?: SuicideRiskEntity(
    crn = crn,
    titleAndFullName = titleAndFullName,
    dateOfLetter = dateOfLetter,
    sheetSentBy = sheetSentBy,
    telephoneNumber = telephoneNumber,
    signature = signature,
    completedDate = completedDate,
    natureOfRisk = natureOfRisk,
    riskIsGreatestWhen = riskIsGreatestWhen,
    riskIncreasesWhen = riskIncreasesWhen,
    riskDecreasesWhen = riskDecreasesWhen,
    additionalInfo = additionalInfo,
    currentPsychTreatment = currentPsychTreatment,
    postalAddressEntity = postalAddress?.toEntity(),
    dateOfBirth = dateOfBirth,
    prisonNumber = prisonNumber,
    workAddressEntity = workAddress?.toEntity(),
    basicDetailsSaved = basicDetailsSaved,
    informationSaved = informationSaved,
    treatmentSaved = treatmentSaved,
    signAndSendSaved = signAndSendSaved,
    contactSaved = contactSaved,
    reviewRequiredDate = reviewRequiredDate,
    reviewEvent = reviewEvent,
    suicideRiskContactList = suicideRiskContactList.map { it.toEntity() },
  )

  private fun SuicideRiskEntity.toModel() = SuicideRisk(
    crn = crn,
    titleAndFullName = titleAndFullName,
    dateOfLetter = dateOfLetter,
    sheetSentBy = sheetSentBy,
    telephoneNumber = telephoneNumber,
    signature = signature,
    completedDate = completedDate,
    natureOfRisk = natureOfRisk,
    riskIsGreatestWhen = riskIsGreatestWhen,
    riskIncreasesWhen = riskIncreasesWhen,
    riskDecreasesWhen = riskDecreasesWhen,
    additionalInfo = additionalInfo,
    currentPsychTreatment = currentPsychTreatment,
    postalAddress = postalAddressEntity?.toModel(),
    dateOfBirth = dateOfBirth,
    prisonNumber = prisonNumber,
    workAddress = workAddressEntity?.toModel(),
    basicDetailsSaved = basicDetailsSaved,
    informationSaved = informationSaved,
    treatmentSaved = treatmentSaved,
    signAndSendSaved = signAndSendSaved,
    contactSaved = contactSaved,
    reviewRequiredDate = reviewRequiredDate,
    reviewEvent = reviewEvent,
    suicideRiskContactList = suicideRiskContactList.map {
      it.toModel()
    },
  )

  private fun AddressEntity.toModel() = Address(
    addressId = addressId,
    officeDescription = officeDescription,
    status = status,
    buildingName = buildingName,
    addressNumber = addressNumber,
    streetName = streetName,
    district = district,
    townCity = townCity,
    county = county,
    postcode = postcode,
  )

  private fun Address.toEntity(existingEntity: AddressEntity? = null) = existingEntity?.copy(
    addressId = addressId,
    officeDescription = officeDescription,
    status = status,
    buildingName = buildingName,
    addressNumber = addressNumber,
    streetName = streetName,
    district = district,
    townCity = townCity,
    county = county,
    postcode = postcode,
  ) ?: AddressEntity(
    addressId = addressId,
    status = status,
    officeDescription = officeDescription,
    buildingName = buildingName,
    addressNumber = addressNumber,
    streetName = streetName,
    district = district,
    townCity = townCity,
    county = county,
    postcode = postcode,
  )

  private fun ContactEntity.toModel() = Contact(
    contactTypeDescription = contactTypeDescription,
    contactPerson = contactPerson,
    contactLocation = contactLocation?.toModel(),
    formSent = formSent,
  )

  private fun Contact.toEntity(existingEntity: ContactEntity? = null) = existingEntity?.copy(
    contactTypeDescription = contactTypeDescription,
    contactPerson = contactPerson,
    contactLocation = contactLocation?.toEntity(),
    formSent = formSent,
  ) ?: ContactEntity(
    contactTypeDescription = contactTypeDescription,
    contactPerson = contactPerson,
    contactLocation = contactLocation?.toEntity(),
    formSent = formSent,
  )

  fun getActiveSuicideRisksForCrn(crn: String?): Collection<SuicideRiskEntity> = suicideRiskRepository.findByCrnAndCompletedDateIsNull(crn)

  fun updateSuicideRiskCrn(suicideRisk: SuicideRiskEntity, crn: String) {
    suicideRisk.crn = crn
    suicideRiskRepository.save(suicideRisk)
  }

  fun updateReviewEvent(eventType: ReviewEventType, suicideRisk: SuicideRiskEntity, occurredAt: ZonedDateTime) {
    suicideRisk.reviewEvent = eventType.name
    suicideRisk.reviewRequiredDate = occurredAt.toLocalDateTime()
    suicideRiskRepository.save(suicideRisk)
  }

  fun deleteAllByCrn(crn: String) {
    suicideRiskRepository.deleteByCrn(crn)
  }
}
