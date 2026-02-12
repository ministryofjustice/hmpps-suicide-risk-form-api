package uk.gov.justice.digital.hmpps.suicideriskformapi.integration

import org.apache.commons.io.FileUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.suicideriskformapi.service.NotificationService
import uk.gov.service.notify.SendEmailResponse
import java.io.File

class NotifyEmailTests : IntegrationTestBase() {

  @Autowired
  lateinit var notificationService: NotificationService

  @Test
  fun `should send an email with an attachment as magic link`() {
    val classLoader = javaClass.getClassLoader()
    val file = File(classLoader.getResource("test_suicide_risk_pdf.pdf").getFile())
    val fileContents: ByteArray = FileUtils.readFileToByteArray(file)
    val emailAddress = "pwilson@unilink.com"
    val sendEmailResponse: SendEmailResponse? = notificationService.sendEmailNotificationWithAttachment(emailAddress, fileContents)
    assertThat(sendEmailResponse?.templateId).isNotNull
  }
}
