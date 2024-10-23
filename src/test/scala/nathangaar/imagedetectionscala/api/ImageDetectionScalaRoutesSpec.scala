import cats.effect.IO
import io.circe.Json
import io.circe.generic.auto.*
import io.circe.syntax.*
import munit.CatsEffectSuite
import nathangaar.imagedetectionscala.api.ImageDetectionScalaRoutes
import nathangaar.imagedetectionscala.clients.ImaggaHttpClientLike
import nathangaar.imagedetectionscala.domain.Imagga
import nathangaar.imagedetectionscala.domain.Imagga.*
import nathangaar.imagedetectionscala.domain.Requests.AnalyzeImageRequest
import nathangaar.imagedetectionscala.repositories.images.Filters.{FilterImages, ImageUUID}
import nathangaar.imagedetectionscala.repositories.images.ImageTypes.*
import nathangaar.imagedetectionscala.repositories.images.{Commands, ImageRepositoryLike}
import org.http4s.*
import org.http4s.circe.*
import org.http4s.implicits.*

class ImageDetectionScalaRoutesSpec extends CatsEffectSuite:

  val mockImage: Image = Image(
    id = ImageId("test-uuid"),
    metaData = Json.obj("key" -> Json.fromString("value")),
    imageUrl = ImageUrl("http://example.com/image.jpg"),
    label = Label("test-label")
  )

  val mockAnalysis: Analysis = Analysis(
    result = TagCollection(
      tags = Seq(
        ImageTag(
          confidence = Confidence(0.98),
          tag = Tag(En("dog"))
        ),
        ImageTag(
          confidence = Confidence(0.88),
          tag = Tag(En("cat"))
        )
      )
    )
  )

  def mockImageRepo = new ImageRepositoryLike[IO] {
    def images(queries: FilterImages): IO[Seq[Image]]          = IO.pure(Seq(mockImage))
    def image(uuid: ImageUUID): IO[Option[Image]]              = IO.pure(Some(mockImage))
    def insert(image: Commands.InsertImage): IO[Option[Image]] = IO.pure(Some(mockImage))
  }

  def mockImageRepoNotFound = new ImageRepositoryLike[IO] {
    def images(queries: FilterImages): IO[Seq[Image]]          = IO.pure(Seq.empty)
    def image(uuid: ImageUUID): IO[Option[Image]]              = IO.pure(None)
    def insert(image: Commands.InsertImage): IO[Option[Image]] = IO.pure(None)
  }

  def mockImaggaClient = new ImaggaHttpClientLike[IO] {
    def analyze(imageUrl: String): IO[Analysis] = IO.pure(mockAnalysis)
  }

  test("GET /images returns a list of images") {
    val request = Request[IO](Method.GET, uri"/images")
    val routes  = ImageDetectionScalaRoutes.imageRoutes(mockImaggaClient, mockImageRepo).orNotFound

    for {
      response <- routes.run(request)
      body     <- response.as[Json]
    } yield {
      assertEquals(response.status, Status.Ok)
      assertEquals(
        body.noSpaces,
        """[{"id":"test-uuid","metaData":{"key":"value"},"imageUrl":"http://example.com/image.jpg","label":"test-label"}]"""
      )
    }
  }

  test("POST /images inserts and analyzes an image") {
    val analyzeImageRequest = AnalyzeImageRequest(
      imageUrl = "http://example.com/image.jpg",
      label = None,
      imageDetectionEnabled = true
    )

    val request = Request[IO](Method.POST, uri"/images").withEntity(analyzeImageRequest.asJson)
    val routes  = ImageDetectionScalaRoutes.imageRoutes(mockImaggaClient, mockImageRepo).orNotFound

    for {
      response <- routes.run(request)
      body     <- response.as[Json]
    } yield {
      assertEquals(response.status, Status.Ok)
      assertEquals(
        body.noSpaces,
        """{"id":"test-uuid","metaData":{"key":"value"},"imageUrl":"http://example.com/image.jpg","label":"test-label"}"""
      )
    }
  }

  test("GET /images/:id returns an image") {
    val request = Request[IO](Method.GET, uri"/images/test-uuid")
    val routes  = ImageDetectionScalaRoutes.imageRoutes(mockImaggaClient, mockImageRepo).orNotFound

    for {
      response <- routes.run(request)
      body     <- response.as[Json]
    } yield {
      assertEquals(response.status, Status.Ok)
      assertEquals(
        body.noSpaces,
        """{"id":"test-uuid","metaData":{"key":"value"},"imageUrl":"http://example.com/image.jpg","label":"test-label"}"""
      )
    }
  }

  test("GET /images/:id returns NotFound if image does not exist") {
    val request = Request[IO](Method.GET, uri"/images/non-existent-id")
    val routes  = ImageDetectionScalaRoutes.imageRoutes(mockImaggaClient, mockImageRepoNotFound).orNotFound

    for {
      response <- routes.run(request)
      body     <- response.as[Json]
    } yield {
      assertEquals(response.status, Status.NotFound)
      assertEquals(
        body.noSpaces,
        """{"errors":["Image with ID non-existent-id not found"]}"""
      )
    }
  }

  test("POST /images returns BadRequest if URL is invalid") {
    val analyzeImageRequest = AnalyzeImageRequest(
      imageUrl = "invalid-url",
      label = None,
      imageDetectionEnabled = true
    )

    val request = Request[IO](Method.POST, uri"/images").withEntity(analyzeImageRequest.asJson)
    val routes  = ImageDetectionScalaRoutes.imageRoutes(mockImaggaClient, mockImageRepo).orNotFound

    for {
      response <- routes.run(request)
      body     <- response.as[Json]
    } yield {
      assertEquals(response.status, Status.UnprocessableEntity)
      assertEquals(
        body.noSpaces,
        """{"errors":["Invalid URL format"]}"""
      )
    }
  }
