package uk.gov.justice.digital.hmpps.suicideriskformapi.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uk.gov.service.notify.NotificationClient
import uk.gov.service.notify.SendEmailResponse
import java.util.UUID


const val SCORE_UNAVAILABLE = "Score Unavailable"
private const val NOT_APPLICABLE = "N/A"
private const val REFERENCE_ID = "referenceId"
private const val CRN = "crn"
private const val FAILED_ALLOCATION_COUNTER = "failed_allocation_notification"

@Service
class NotificationService(
  @Value("\${application.notify.suicide-risk-form.template}") private val srfTemplateId: String,
  @Value("\${application.notify.suicide-risk-form.key}") private val srfApiKey: String,
) {
  private val log = LoggerFactory.getLogger(this::class.java)
  
  fun sendEmailNotification(emailAddress: String, personalisation: MutableMap<String?, Any?>): SendEmailResponse? {
    val notificationClient: NotificationClient = NotificationClient(srfApiKey)
    val emailReferenceId = UUID.randomUUID().toString()


// pass in a list and it will appear as bullet points in the message:
//    personalisation.put("list", listOfItems)

    val response: SendEmailResponse? = notificationClient.sendEmail(
      srfTemplateId,
      emailAddress,
      personalisation,
      emailReferenceId,
    )
    return response
  }

  fun sendEmailNotificationWithAttachment(emailAddress: String, personalisation: MutableMap<String?, Any?>, fileByteArray: ByteArray): SendEmailResponse? {
    val notificationClient: NotificationClient = NotificationClient(srfApiKey)
    val emailReferenceId = UUID.randomUUID().toString()
    personalisation.put("link_to_file", NotificationClient.prepareUpload(fileByteArray, "SuicideRiskForm.pdf"))

    val response: SendEmailResponse? = notificationClient.sendEmail(
      srfTemplateId,
      emailAddress,
      personalisation,
      emailReferenceId,
    )
    return response
  }

}

//data class NotificationMessageResponse(
//  val templateId: String,
//  val referenceId: String,
//  val email: Set<String>,
//)
//
////data class NotifyData(
////  val riskSummary: RiskSummary?,
////  val riskPredictors: List<RiskPredictor>,
////)
//
//data class NotificationEmail(
//  val emailTo: Set<String>,
//  val emailTemplate: String,
//  val emailReferenceId: String,
//  val emailParameters: Map<String, Any>,
//)