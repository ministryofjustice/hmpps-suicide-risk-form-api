package uk.gov.justice.digital.hmpps.suicideriskformapi.controller

import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.suicideriskformapi.service.SuicideRiskService
import uk.gov.justice.digital.hmpps.suicideriskformapi.util.MagicLinkSignatureUtil
import java.security.MessageDigest
import java.time.Instant
import java.util.*
import kotlin.uuid.Uuid


@RestController
@RequestMapping("/files")

class FileAccessController(private val suicideRiskService: SuicideRiskService) {
  var secretKey: String = "ReallySecretKey"
  val signatureUtil = MagicLinkSignatureUtil(secretKey)

  // Spring Framework 6 path pattern captures the rest of the path
  @GetMapping("/{*path}")
  @Throws(Exception::class)
  fun fetchFile(
    @PathVariable("path") path: String,
    @RequestParam expires: Long,
    @RequestParam signature: String?
//    response: HttpServletResponse,
  ) : ResponseEntity<ByteArray>{
    val now = Instant.now().getEpochSecond()
    if (now >= expires) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null)
//      response.setStatus(HttpServletResponse.SC_FORBIDDEN)
//      return
    }

    val fullpath = "/files$path"
    val uuid: UUID =  UUID.fromString(path.replace("/",""))
    val expected: String = signatureUtil.signPath(fullpath, expires)

    try {
      val expectedBytes = Base64.getUrlDecoder().decode(expected)
      val providedBytes = Base64.getUrlDecoder().decode(signature)

      if (!MessageDigest.isEqual(expectedBytes, providedBytes)) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null)
//        response.status = HttpServletResponse.SC_FORBIDDEN
//        return
      }
    } catch (badBase64: IllegalArgumentException) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null)
//      response.status = HttpServletResponse.SC_FORBIDDEN
//      return
    }

    // generate the document using the PDF generator end point
    var suicideRisk = suicideRiskService.findSuicideRiskById(uuid)
    var pdfBytes = suicideRiskService.getSuicideRiskAsPdf(uuid, suicideRisk, suicideRisk.completedDate == null)
    var headers = HttpHeaders()
    headers.contentType = MediaType.APPLICATION_PDF
    headers.contentDisposition = ContentDisposition.attachment().filename("Suicide_Risk_Form_" + suicideRisk.crn + ".pdf").build()
    //return ResponseEntity.ok().headers(headers).body(pdfBytes)
    return ResponseEntity.ok().headers(headers).body(pdfBytes)
//    response.status = HttpServletResponse.SC_OK
//    response.getWriter().write("Serving file " + path)
  }
}