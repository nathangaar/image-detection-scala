package nathangaar.imagedetectionscala.clients

import cats.effect.{IO, Resource}
import cats.syntax.all.*
import munit.CatsEffectSuite
import nathangaar.imagedetectionscala.domain.Errors
import nathangaar.imagedetectionscala.domain.Imagga.*
import org.http4s.*
import org.http4s.Method.*
import org.http4s.client.*

object MockData {
  val analysisJson = """{
    "result": {
      "tags": [
        {
          "confidence": 0.95,
          "tag": {
            "en": "dog"
          }
        }
      ]
    }
  }"""

  val analysis: Analysis = Analysis(result = TagCollection(Seq(ImageTag(Confidence(0.95), Tag(En("dog"))))))
}

class ImaggaHttpClientSpec extends CatsEffectSuite:

  test("analyze should return Analysis when the response is Ok") {
    val mockClient: Client[IO] = Client[IO] { request =>
      assert(request.method == GET)
      assert(request.uri == Uri.unsafeFromString("http://example.com/analyze?image_url=http://example.com/image.jpg"))

      Resource.eval(
        Response[IO](Status.Ok)
          .withEntity(MockData.analysisJson)
          .pure[IO]
      )
    }

    val imaggaClient =
      ImaggaHttpClient[IO](mockClient, Uri.unsafeFromString("http://example.com/analyze"), "user", "secret")

    val result = imaggaClient.analyze("http://example.com/image.jpg")
    assertIO(result, MockData.analysis)
  }

  test("analyze should raise ImaggaError when the response is not Ok") {
    val mockClient: Client[IO] = Client[IO] { request =>
      assert(request.method == GET)
      assert(request.uri == Uri.unsafeFromString("http://example.com/analyze?image_url=http://example.com/image.jpg"))

      Resource.eval(Response[IO](Status.BadRequest).pure[IO])
    }

    val imaggaClient =
      ImaggaHttpClient[IO](mockClient, Uri.unsafeFromString("http://example.com/analyze"), "user", "secret")

    val result = imaggaClient.analyze("http://example.com/image.jpg")

    result.attempt.map {
      case Left(error: Errors.ImaggaError) =>
        assert(true)
      case Left(_) =>
        fail("Expected an ImaggaError to be raised")
      case Right(_) =>
        fail("Expected an error but received a valid response")
    }
  }
