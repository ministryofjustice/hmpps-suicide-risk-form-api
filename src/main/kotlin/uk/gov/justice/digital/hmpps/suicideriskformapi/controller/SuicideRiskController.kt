package uk.gov.justice.digital.hmpps.suicideriskformapi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
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
import uk.gov.justice.digital.hmpps.suicideriskformapi.model.InitialiseSuicideRisk
import uk.gov.justice.digital.hmpps.suicideriskformapi.model.SuicideRisk
import uk.gov.justice.digital.hmpps.suicideriskformapi.service.SuicideRiskService
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse
import java.util.*

@Validated
@RestController
//@PreAuthorize("hasRole('ROLE_SUICIDE_RISK')") TODO: create PR for hmpps auth
@RequestMapping(value = ["/suicide-risk"], produces = ["application/json"])
class SuicideRiskController(
  private val suicideRiskService: SuicideRiskService,
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
    description = "Calls through the suicide risk service to initialise a breach notice",
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
    suicideRiskService.updateSuicideRisk(id, suicideRisk)
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
    suicideRiskService.deleteSuicideRisk(id)
  }
}
