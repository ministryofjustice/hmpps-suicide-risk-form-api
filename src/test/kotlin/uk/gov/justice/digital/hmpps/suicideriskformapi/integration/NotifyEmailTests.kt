package uk.gov.justice.digital.hmpps.suicideriskformapi.integration

import org.apache.commons.io.FileUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.suicideriskformapi.repository.SuicideRiskRepository
import uk.gov.justice.digital.hmpps.suicideriskformapi.service.NotificationService
import uk.gov.service.notify.SendEmailResponse
import java.io.File


class NotifyEmailTests : IntegrationTestBase() {

  @Autowired
  lateinit var notificationService: NotificationService

  @Autowired
  private lateinit var suicideRiskRepository: SuicideRiskRepository


  @Test
  fun `should send an email`() {
    val emailAddress = "pwilson@unilinik.com"
    val templatePersonalisation: MutableMap<String?, Any?> = HashMap<String?, Any?>()
    templatePersonalisation.put("expiry-date", "20/12/2025")
    templatePersonalisation.put("link-to-suicide-risk-form", "<a href=\"www.bbc.co.uk\">BBC Homepage</a>")
    val sendEmailResponse: SendEmailResponse? = notificationService.sendEmailNotification(emailAddress, templatePersonalisation)
    assertThat(sendEmailResponse?.templateId).isNotNull
  }

  @Test
  open fun `should send an email with an attachment as magic link`() {
    val classLoader = javaClass.getClassLoader()
    val file: File = File(classLoader.getResource("test_suicide_risk_pdf.pdf").getFile())
    //val fileContents: ByteArray? = FileUtils.readFileToByteArray(file)
    val fileContents: ByteArray = FileUtils.readFileToByteArray(file)
    val emailAddress = "marcus.aspin@justice.gov.uk"
    val templatePersonalisation: MutableMap<String?, Any?> = HashMap<String?, Any?>()
    templatePersonalisation.put("expiry-date", "20/12/2025")
    templatePersonalisation.put("link-to-suicide-risk-form", "<a href=\"www.bbc.co.uk\">BBC Homepage</a>")
    val sendEmailResponse: SendEmailResponse? = notificationService.sendEmailNotificationWithAttachment(emailAddress, templatePersonalisation, fileContents)
    assertThat(sendEmailResponse?.templateId).isNotNull
  }

}
