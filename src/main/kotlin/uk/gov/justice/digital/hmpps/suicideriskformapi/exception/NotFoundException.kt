package uk.gov.justice.digital.hmpps.suicideriskformapi.exception

open class NotFoundException(message: String) : RuntimeException(message) {
  constructor(
    entity: String,
    field: String,
    value: Any,
  ) : this("$entity with $field of $value not found")
}
