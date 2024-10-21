package nathangaar.imagedetectionscala.api.validations

import cats.data.{Validated, ValidatedNel}
import java.net.URL
import cats.data.NonEmptyList
import nathangaar.imagedetectionscala.domain.Requests.AnalyzeImageRequest
import nathangaar.imagedetectionscala.domain.Errors.ImageDetectionFieldError

object AnalysisRequestValidator:
  type ValidationResult[A] = ValidatedNel[ImageDetectionFieldError, A]

  def validateUrl(url: String): ValidationResult[String] =
    Validated
      .catchNonFatal(new URL(url))
      .toValidatedNel
      .map(_ => url)
      .leftMap(_ => NonEmptyList.one(ImageDetectionFieldError("Invalid URL format")))

  def fromRequest(request: AnalyzeImageRequest): ValidationResult[AnalyzeImageRequest] =
    validateUrl(request.imageUrl).map { validUrl =>
      request.copy(imageUrl = validUrl)
    }
