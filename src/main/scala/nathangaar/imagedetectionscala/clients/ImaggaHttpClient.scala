package nathangaar.imagedetectionscala.clients

import cats.effect.*
import cats.syntax.all.*
import nathangaar.imagedetectionscala.domain.Errors
import nathangaar.imagedetectionscala.domain.Errors.*
import nathangaar.imagedetectionscala.domain.Imagga.Analysis
import org.http4s.*
import org.http4s.Method.*
import org.http4s.circe.*
import org.http4s.client.*
import org.http4s.headers.Authorization

trait ImaggaHttpClientLike[F[_]]:
  def analyze(imageUrl: String): F[Analysis]

class ImaggaHttpClient[F[_]: Async](httpClient: Client[F], baseUrl: Uri, user: String, secret: String)
    extends ImaggaHttpClientLike[F] {
  implicit val tagDecoder: EntityDecoder[F, Analysis] = jsonOf[F, Analysis]

  override def analyze(imageUrl: String): F[Analysis] = {
    val request = Request[F](
      method = GET,
      uri = (baseUrl).withQueryParam("image_url", imageUrl)
    ).withHeaders(Authorization(BasicCredentials(user, secret)))

    httpClient.run(request).use { response =>
      response.status match {
        case Status.Ok =>
          response.as[Analysis]
        case _ =>
          Errors.ImaggaError(new RuntimeException("Error fetching tags")).raiseError[F, Analysis]
      }
    }
  }
}

// Companion object for the ImaggaHttpClient
object ImaggaHttpClient:
  def apply[F[_]: Async](HttpClient: Client[F], baseUrl: Uri, user: String, secret: String): ImaggaHttpClient[F] =
    new ImaggaHttpClient(HttpClient, baseUrl, user, secret)
