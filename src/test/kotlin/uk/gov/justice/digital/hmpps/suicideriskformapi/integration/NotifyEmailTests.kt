package uk.gov.justice.digital.hmpps.suicideriskformapi.integration

import org.apache.commons.io.FileUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.suicideriskformapi.service.NotificationService
import uk.gov.service.notify.SendEmailResponse
import java.io.File

@ActiveProfiles("local")
class NotifyEmailTests : IntegrationTestBase() {

  @Autowired
  lateinit var notificationService: NotificationService

  @Value("\${test.email}")
  lateinit var email: String

  // The following test will not be run by CI but is here to allow a developer to manually run the test to send an email
  // If you wish to run this test, create an application-local.yml under the resources folder. Make sure
  // you dont add the file to git and add  it to the git ignore file
  // inside application-local.yml add the following block:

  //  application:
  //    notify:
  //      suicide-risk-form:
  //        template: 6d2e680d-ff01-4fc4-b082-1c3a4c28a1af
  //         key: replace-this-with-an-api-key-from-notify
  //
  //  test:
  //    email: replace-this-with-the-test-email-address

//  @Test
  @Disabled("For manual execution only. Not to be run by CI")
  fun `should send an email with an attachment as magic link`() {
    val classLoader = javaClass.getClassLoader()
    val file = File(classLoader.getResource("test_suicide_risk_pdf.pdf").getFile())
    val fileContents: ByteArray = FileUtils.readFileToByteArray(file)
    val sendEmailResponse: SendEmailResponse? = notificationService.sendEmailNotificationWithAttachment(email, generatePersonalisation(), fileContents)
    assertThat(sendEmailResponse?.templateId).isNotNull
  }

  private fun generatePersonalisation(): MutableMap<String?, Any?> {
    var personalisations: MutableMap<String?, Any?> = HashMap()
    personalisations.put("crn", "X123456")
    personalisations.put("offender_full_name", "Mr Joe Bloggs")
    personalisations.put("officer_name", "Peter the Pirate")
    personalisations.put("officer_phone", "07707123456")
    personalisations.put("officer_email", "test@test.com")
    return personalisations
  }
}
