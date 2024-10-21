package nathangaar.imagedetectionscala.api

import cats.Applicative
import cats.data.Validated
import cats.effect.Concurrent
import cats.implicits.*
import io.circe.Json
import io.circe.syntax.EncoderOps
import nathangaar.imagedetectionscala.clients.ImaggaHttpClientLike
import nathangaar.imagedetectionscala.domain.Requests.AnalyzeImageRequest
import nathangaar.imagedetectionscala.repositories.images.Filters.*
import nathangaar.imagedetectionscala.repositories.images.ImageTypes.*
import nathangaar.imagedetectionscala.repositories.images.{Commands, ImageRepositoryLike, ImageTypes}
import org.http4s.circe.*
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.dsl.Http4sDsl
import org.http4s.dsl.impl.OptionalQueryParamDecoderMatcher
import org.http4s.{HttpRoutes, QueryParamDecoder, Request}
import nathangaar.imagedetectionscala.api.validations.AnalysisRequestValidator
import cats.data.NonEmptyList
import org.http4s.Response
import nathangaar.imagedetectionscala.domain.Errors

object ImageDetectionScalaRoutes:

  object ObjectsQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("objects")

  final val UndefinedLabel = "undefined"

  def imageRoutes[F[_]: Concurrent](
    Images: ImaggaHttpClientLike[F],
    ImagesRepository: ImageRepositoryLike[F]
  ): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl.*

    HttpRoutes.of[F] {
      case GET -> Root / "images" :? ObjectsQueryParamMatcher(objectsOpt) =>
        val queryTerms = objectsOpt.fold(Seq())(_.split(",").map(Term(_)).toSeq)
        for {
          images <- ImagesRepository.images(FilterImages(terms = queryTerms))
          result <- Ok(images)
        } yield result

      case GET -> Root / "images" / imageId =>
        for {
          maybeImage <- ImagesRepository.image(ImageUUID(imageId))
          result <-
            maybeImage.fold(
              NotFound(Json.obj("errors" -> Json.arr(Json.fromString(s"${Errors.ImageIdNotFound(imageId).message}"))))
            )(image => Ok(image))
        } yield result

      case req @ POST -> Root / "images" =>
        req.as[AnalyzeImageRequest].attempt.flatMap {
          case Right(analyzeImageReq) => {
            for {
              result <- AnalysisRequestValidator.fromRequest(analyzeImageReq) match {
                          case Validated.Valid(validReq) =>
                            for {
                              analysisResult <- if (validReq.imageDetectionEnabled) {
                                                  Images.analyze(validReq.imageUrl).map(Option(_))
                                                } else { Applicative[F].pure(None) }
                              finalLabel = validReq.label match {
                                             case Some(definedLabel) => definedLabel
                                             case _ =>
                                               analysisResult.fold(UndefinedLabel) { analysis =>
                                                 analysis.result.tags
                                                   .sortBy(tag => -tag.confidence.confidence)
                                                   .headOption
                                                   .map(_.tag.en.en)
                                                   .getOrElse(UndefinedLabel)
                                               }
                                           }
                              image <- ImagesRepository.insert(
                                         Commands.InsertImage(
                                           imageUrl = ImageUrl(validReq.imageUrl),
                                           metaData = analysisResult.fold(Json.obj())(_.asJson),
                                           label = Label(finalLabel)
                                         )
                                       )
                              result <- Ok(image)
                            } yield result
                          case Validated.Invalid(errors) =>
                            BadRequest(
                              Json.obj(
                                "errors" -> Json.arr(errors.toList.map(error => Json.fromString(error.message)): _*)
                              )
                            )
                        }
            } yield result
          }
          case Left(error) =>
            BadRequest(Json.obj("errors" -> Json.fromString(s"Invalid request: ${error.getMessage}")))
        }
    }
  }
