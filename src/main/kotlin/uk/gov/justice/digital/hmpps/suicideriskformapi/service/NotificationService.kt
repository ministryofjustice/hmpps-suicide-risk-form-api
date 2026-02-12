package uk.gov.justice.digital.hmpps.suicideriskformapi.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uk.gov.service.notify.NotificationClient
import uk.gov.service.notify.SendEmailResponse
import java.util.UUID

@Service
class NotificationService(
  @Value("\${application.notify.suicide-risk-form.template}") private val srfTemplateId: String,
  @Value("\${application.notify.suicide-risk-form.key}") private val srfApiKey: String,
) {
  private val notificationServiceLogger = LoggerFactory.getLogger(this::class.java)

  fun sendEmailNotificationWithAttachment(emailAddress: String, fileByteArray: ByteArray?): SendEmailResponse? {
    notificationServiceLogger.info("Sending an email to " + emailAddress)
    val notificationClient = NotificationClient(srfApiKey)
    val emailReferenceId = UUID.randomUUID().toString()
    val personalisation: MutableMap<String?, Any?> = HashMap()
    personalisation["link_to_file"] = NotificationClient.prepareUpload(fileByteArray, "SuicideRiskForm.pdf")

    val response: SendEmailResponse? = notificationClient.sendEmail(
      srfTemplateId,
      emailAddress,
      personalisation,
      emailReferenceId,
    )
    return response
  }
}
