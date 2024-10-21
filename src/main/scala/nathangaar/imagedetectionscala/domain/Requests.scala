package nathangaar.imagedetectionscala.domain

import cats.effect.Concurrent
import io.circe.*
import io.circe.generic.auto.*
import org.http4s.EntityDecoder
import org.http4s.circe.*

object Requests:
  case class AnalyzeImageRequest(
    imageUrl: String,
    label: Option[String],
    imageDetectionEnabled: Boolean
  )

  implicit val analyzeImageRequestDecoder: Decoder[AnalyzeImageRequest] = new Decoder[AnalyzeImageRequest] {
    final def apply(c: HCursor): Decoder.Result[AnalyzeImageRequest] = for {
      imageUrl              <- c.downField("imageUrl").as[String]
      label                 <- c.downField("label").as[Option[String]]
      imageDetectionEnabled <- c.downField("imageDetectionEnabled").as[Option[Boolean]].map(_.getOrElse(false))
    } yield {
      AnalyzeImageRequest(
        imageUrl = imageUrl,
        label = label,
        imageDetectionEnabled = imageDetectionEnabled
      )
    }
  }

  implicit def analyzeImageRequestDecoder[F[_]: Concurrent]: EntityDecoder[F, AnalyzeImageRequest] =
    jsonOf[F, AnalyzeImageRequest]
