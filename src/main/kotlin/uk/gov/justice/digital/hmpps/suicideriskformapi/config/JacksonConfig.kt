package uk.gov.justice.digital.hmpps.suicideriskformapi.config

import com.fasterxml.jackson.databind.DeserializationFeature
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class JacksonConfig {

  @Bean
  fun jacksonCustomizer(): Jackson2ObjectMapperBuilderCustomizer = Jackson2ObjectMapperBuilderCustomizer { builder ->
    builder.featuresToDisable(
      DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE,
    )
  }
}
