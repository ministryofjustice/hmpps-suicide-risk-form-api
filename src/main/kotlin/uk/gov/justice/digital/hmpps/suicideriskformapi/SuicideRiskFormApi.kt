package uk.gov.justice.digital.hmpps.suicideriskformapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SuicideRiskFormApi

fun main(args: Array<String>) {
  runApplication<SuicideRiskFormApi>(*args)
}
