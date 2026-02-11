package uk.gov.justice.digital.hmpps.suicideriskformapi.util

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.io.encoding.Base64


class MagicLinkSignatureUtil(secretKey: String) {
  val hmac: String = "HmacSHA256"
  var secret: ByteArray = secretKey.toByteArray(charset = Charsets.UTF_8)


  @Throws(Exception::class)
  fun signPath(path: String, expires: Long): String {
    val data = path + "|" + expires
    val mac = Mac.getInstance(hmac)
    mac.init(SecretKeySpec(secret, hmac))
    val raw = mac.doFinal(data.toByteArray(charset = Charsets.UTF_8))
    return Base64.UrlSafe.encode(raw)
  }
}