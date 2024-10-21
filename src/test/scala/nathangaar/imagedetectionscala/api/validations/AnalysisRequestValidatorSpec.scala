package nathangaar.imagedetectionscala.api.validations

import cats.data.NonEmptyList
import munit.FunSuite
import nathangaar.imagedetectionscala.domain.Errors.ImageDetectionFieldError
import nathangaar.imagedetectionscala.domain.Requests.AnalyzeImageRequest

class AnalysisRequestValidatorSpec extends FunSuite {

  test("validateUrl should return valid URL for valid input") {
    val validUrl = "http://example.com"
    val result   = AnalysisRequestValidator.validateUrl(validUrl)

    assert(result.isValid)
    assert(result.getOrElse("") == validUrl)
  }

  test("validateUrl should return error for invalid URL") {
    val invalidUrl = "invalid-url"
    val result     = AnalysisRequestValidator.validateUrl(invalidUrl)

    assert(result.isInvalid)
    assert(result.swap.getOrElse(NonEmptyList.one("")).head == ImageDetectionFieldError("Invalid URL format"))
  }

  test("fromRequest should create a valid AnalyzeImageRequest") {
    val request = AnalyzeImageRequest("http://example.com", None, imageDetectionEnabled = true)
    val result  = AnalysisRequestValidator.fromRequest(request)

    assert(result.isValid)
    assert(result.getOrElse(AnalyzeImageRequest("", None, false)) == request)
  }

  test("fromRequest should return error for invalid URL") {
    val request = AnalyzeImageRequest("invalid-url", None, imageDetectionEnabled = true)
    val result  = AnalysisRequestValidator.fromRequest(request)

    assert(result.isInvalid)
    assert(result.swap.getOrElse(NonEmptyList.one("")).head == ImageDetectionFieldError("Invalid URL format"))
  }
}
