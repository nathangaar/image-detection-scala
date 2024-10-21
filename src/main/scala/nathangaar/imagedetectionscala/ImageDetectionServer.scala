package nathangaar.imagedetectionscala

import cats.effect.Async
import com.comcast.ip4s.*
import doobie.util.transactor.Transactor
import fs2.io.net.Network
import nathangaar.imagedetectionscala.api.ImageDetectionScalaRoutes
import nathangaar.imagedetectionscala.clients.ImaggaHttpClient
import nathangaar.imagedetectionscala.config.ServiceConfig
import nathangaar.imagedetectionscala.repositories.images.{ImageRepository, ImageRepositoryLike}
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits.*
import org.http4s.server.middleware.Logger

object ImageDetectionServer:

  def run[F[_]: Async: Network](
    serviceConfig: ServiceConfig,
    xa: Transactor[F]
  ): F[Nothing] = {

    val imagesRepository: ImageRepositoryLike[F] = new ImageRepository[F](xa)

    for {
      client <- EmberClientBuilder.default[F].build
      imaggaHttpClient =
        ImaggaHttpClient[F](client, serviceConfig.imagga.url, serviceConfig.imagga.user, serviceConfig.imagga.secret)
      httpApp = (
                  ImageDetectionScalaRoutes.imageRoutes[F](imaggaHttpClient, imagesRepository)
                ).orNotFound
      finalHttpApp = Logger.httpApp(true, true)(httpApp)
      _ <-
        EmberServerBuilder
          .default[F]
          .withHost(ipv4"0.0.0.0")
          .withPort(port"8080")
          .withHttpApp(finalHttpApp)
          .build
    } yield ()
  }.useForever
