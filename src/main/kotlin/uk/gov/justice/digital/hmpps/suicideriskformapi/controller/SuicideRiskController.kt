package uk.gov.justice.digital.hmpps.suicideriskformapi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.suicideriskformapi.model.Contact
import uk.gov.justice.digital.hmpps.suicideriskformapi.model.InitialiseSuicideRisk
import uk.gov.justice.digital.hmpps.suicideriskformapi.model.SuicideRisk
import uk.gov.justice.digital.hmpps.suicideriskformapi.service.SnsService
import uk.gov.justice.digital.hmpps.suicideriskformapi.service.SuicideRiskService
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse
import java.util.UUID

@Validated
@RestController
@PreAuthorize("hasRole('ROLE_SUICIDE_RISK')")
@RequestMapping(value = ["/suicide-risk"], produces = ["application/json"])
class SuicideRiskController(
  private val suicideRiskService: SuicideRiskService,
  private val sqsService: SnsService,
) {
  @GetMapping("/{uuid}")
  @Operation(
    summary = "Retrieve a draft suicide risk form by uuid",
    description = "Calls through the suicide risk service to retrieve suicide risk form",
    security = [SecurityRequirement(name = "suicide-risk-api-ui-role")],
    responses = [
      ApiResponse(responseCode = "200", description = "suicide risk returned"),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun getSuicideRiskById(@PathVariable uuid: UUID): SuicideRisk? = suicideRiskService.findSuicideRiskById(uuid)

  @PostMapping
  @Operation(
    summary = "Initialises a Suicide Risk",
    description = "Calls the API to initialise a suicide risk form",
    security = [SecurityRequirement(name = "suicide-risk-api-ui-role")],
    responses = [
      ApiResponse(responseCode = "201", description = "suicide risk created"),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @ResponseStatus(HttpStatus.CREATED)
  fun initialiseSuicideRisk(@Valid @RequestBody initialiseSuicideRisk: InitialiseSuicideRisk) = suicideRiskService.initialiseSuicideRisk(initialiseSuicideRisk)

  @PutMapping("/{id}")
  @Operation(
    summary = "Update a Suicide Risk",
    description = "Calls through the suicide service to update a suicide risk",
    security = [SecurityRequirement(name = "suicide-risk-api-ui-role")],
    responses = [
      ApiResponse(responseCode = "200", description = "Suicide Risk updated"),
      ApiResponse(
        responseCode = "400",
        description = "cant change the CRN on an update",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "The Suicide Rick id was not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun updateSuicideRisk(@PathVariable id: UUID, @RequestBody suicideRisk: SuicideRisk) {
    val original = suicideRiskService.findSuicideRiskById(id)
    suicideRiskService.updateSuicideRisk(id, suicideRisk)

    if (original != null && original.completedDate == null && suicideRisk.completedDate != null) {
      performCompletionSteps(suicideRisk, id)
    }
  }

  fun performCompletionSteps(suicideRisk: SuicideRisk, id: UUID) {
    // send publish message
    sqsService.sendPublishDomainEvent(suicideRisk, id);

    //create a magic link

    //after poc can loop through all the emails

    // email each person on the list. Initial POC will just email 1 person
//    emailService
  }

  @DeleteMapping("/{id}")
  @Operation(
    summary = "Delete a Suicide Risk",
    description = "Calls through the suicide risk service to delete a suicide risk",
    security = [SecurityRequirement(name = "suicide-risk-api-ui-role")],
    responses = [
      ApiResponse(responseCode = "200", description = "Suicide Risk deleted"),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "The Suicide Risk id was not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun deleteSuicideRisk(@PathVariable id: UUID) {
    val crn = suicideRiskService.deleteSuicideRisk(id)
    sqsService.sendDeleteDomainEvent(crn, id)
  }

  @GetMapping("/{uuid}/pdf")
  @Operation(
    summary = "Retrieve a suicide risk pdf by uuid - suicide risk id",
    description = "Calls through the suicide risk form service to retrieve a generate ",
    security = [SecurityRequirement(name = "suicide-risk-form-api-ui-role")],
    responses = [
      ApiResponse(responseCode = "200", description = "breach notice pdf returned"),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun getSuicideRiskAsPdf(@PathVariable uuid: UUID): ResponseEntity<ByteArray> {
    var suicideRisk = suicideRiskService.findSuicideRiskById(uuid)
    var pdfBytes = suicideRiskService.getSuicideRiskAsPdf(uuid, suicideRisk, suicideRisk.completedDate == null)
    var headers = HttpHeaders()
    headers.contentType = MediaType.APPLICATION_PDF
    headers.contentDisposition = ContentDisposition.attachment().filename("Suicide_Risk_Form_" + suicideRisk?.crn + ".pdf").build()
    return ResponseEntity.ok().headers(headers).body(pdfBytes)
  }

  @DeleteMapping("/{id}/recipient/{contactId}")
  @Operation(
    summary = "Delete a Suicide Risk Recipient",
    description = "Calls through the suicide risk service to delete a recipient",
    security = [SecurityRequirement(name = "suicide-risk-form-api-ui-role")],
    responses = [
      ApiResponse(responseCode = "200", description = "Suicide Risk Recipient Deleted"),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "The Suicide Risk id was not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun deleteRecipient(@PathVariable id: UUID, @PathVariable contactId: UUID) = suicideRiskService.deleteRecipient(id, contactId)

  @PostMapping("{id}/recipient")
  @Operation(
    summary = "Create a Suicide Risk Recipient",
    description = "Adds a new recipient to the suicide risk",
    security = [SecurityRequirement(name = "suicide-risk-form-api-ui-role")],
    responses = [
      ApiResponse(responseCode = "201", description = "Suicide Risk Recipient Created"),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "The Suicide Risk id was not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun createRecipient(
    @PathVariable("id") suicideRiskId: UUID,
    @RequestBody recipient: Contact,
  ): Contact = suicideRiskService.createRecipient(suicideRiskId, recipient)

  @PutMapping("{id}/recipient/{contactId}")
  @Operation(
    summary = "Update a Suicide Risk Recipient",
    description = "Updates an existing recipient for the suicide risk",
    security = [SecurityRequirement(name = "suicide-risk-form-api-ui-role")],
    responses = [
      ApiResponse(responseCode = "200", description = "Suicide Risk Recipient Updated"),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "The Suicide Risk or Recipient id was not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun updateRecipient(
    @PathVariable id: UUID,
    @PathVariable contactId: UUID,
    @RequestBody request: Contact,
  ) = suicideRiskService.updateRecipient(id, contactId, request)

  @GetMapping("{id}/recipient/{contactId}")
  @Operation(
    summary = "Get a Suicide Risk Recipient",
    description = "Calls through the suicide risk service to get a recipient",
    security = [SecurityRequirement(name = "suicide-risk-form-api-ui-role")],
    responses = [
      ApiResponse(responseCode = "200", description = "Suicide Risk recipient returned"),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorized to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden to access this endpoint",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "404",
        description = "The Suicide Risk id was not found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  fun getRecipient(@PathVariable id: UUID, @PathVariable contactId: UUID) = suicideRiskService.getRecipient(id, contactId)
}
