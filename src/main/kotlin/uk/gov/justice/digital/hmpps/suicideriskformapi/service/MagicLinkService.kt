package uk.gov.justice.digital.hmpps.suicideriskformapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.suicideriskformapi.util.MagicLinkSignatureUtil
import kotlin.time.Clock
import kotlin.time.ExperimentalTime


@Service
class MagicLinkService {

  var secretKey: String = "ReallySecretKey"
  var magicLinkSignatureUtil: MagicLinkSignatureUtil? = MagicLinkSignatureUtil(secretKey)

  @OptIn(ExperimentalTime::class)
  @Throws(Exception::class)
  fun generateLink(filePath: String, lifetimeSeconds: Long): String {
    val expiresAt: Long = Clock.System.now().epochSeconds + lifetimeSeconds
    val signature: String = magicLinkSignatureUtil?.signPath(filePath, expiresAt) ?: ""

    return String.format(
      "%s?expires=%d&signature=%s",
      filePath, expiresAt, signature,
    )
  }
}