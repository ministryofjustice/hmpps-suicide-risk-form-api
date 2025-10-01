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
}

data class NDeliusCrn(
  val crn: String,
)
