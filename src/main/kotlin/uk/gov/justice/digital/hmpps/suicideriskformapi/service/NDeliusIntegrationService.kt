package uk.gov.justice.digital.hmpps.suicideriskformapi.service

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono

@Service
class NDeliusIntegrationService(
  @Qualifier("integrationApiClient") private val webClient: WebClient,
) {
  fun getCrnForSuicideRiskUuid(suicideRiskId: String): NDeliusCrn? = webClient.get()
    .uri("/case/{suicideRiskId}", suicideRiskId)
    .retrieve()
    .bodyToMono(NDeliusCrn::class.java)
    .onErrorResume(WebClientResponseException.NotFound::class.java) { Mono.empty() }
    .block()

  fun getBreachEventDocuments(crn: String, eventNumber: String): List<String> = webClient.get()
    .uri("/srf-event-documents/{crn}/{eventNumber}", crn, eventNumber)
    .retrieve()
    .bodyToMono(SuicideRiskIdList::class.java)
    .onErrorResume(WebClientResponseException.NotFound::class.java) { Mono.empty() }
    .block()?.srfIdList ?: emptyList()
}

data class NDeliusCrn(
  val crn: String,
)

data class SuicideRiskIdList(
  val srfIdList: List<String>,
)
