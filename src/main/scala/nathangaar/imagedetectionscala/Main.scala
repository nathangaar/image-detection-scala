package nathangaar.imagedetectionscala

import cats.effect.{IO, IOApp, Resource}
import doobie.util.transactor.Transactor
import nathangaar.imagedetectionscala.config.ServiceConfig
import org.flywaydb.core.Flyway
import pureconfig.*

import java.util.concurrent.{ExecutorService, Executors}
import scala.concurrent.ExecutionContext

object Main extends IOApp.Simple:
  // This is effectful, doing this intentionally so that the program will blow up if configuration is wrong
  lazy val serviceConfig: ServiceConfig = ConfigSource.default.loadOrThrow[ServiceConfig]

  lazy val xa: Transactor[IO] = Transactor.fromDriverManager[IO](
    driver = "org.postgresql.Driver",
    url = s"jdbc:postgresql:${serviceConfig.database.name}",
    user = serviceConfig.database.user,
    password = serviceConfig.database.password,
    logHandler = None
  )

  def runMigrations(): IO[Unit] = {
    val flyway = Flyway
      .configure()
      .dataSource(
        serviceConfig.database.databaseUrl,
        serviceConfig.database.user,
        serviceConfig.database.password
      )
      .locations("filesystem:src/main/resources/db/migration")
      .load()

    // Start the migration process
    val migrationsApplied = flyway.migrate()
    IO(println(s"Migrations applied: $migrationsApplied"))
  }

  lazy val fixedThreadPoolResource: Resource[IO, ExecutionContext] =
    Resource.make {
      // We'll use a fix sized thread pool for this exercise. On the JVM, this will give us 1 shared queue for tasks
      // that x numbrer of threads dequeue work from. For IO bound tasks like reading from a file or making network requets
      // it offers predictable performance.
      IO(Executors.newFixedThreadPool(serviceConfig.threadPoolSize))
    } { pool =>
      IO(pool.shutdown())
    }.map(ExecutionContext.fromExecutorService)

  def run: IO[Unit] = fixedThreadPoolResource.use { ec =>
    val customEc: ExecutionContext = ec
    for {
      _ <- if (serviceConfig.database.migrationsEnabled) runMigrations() else IO.unit
      _ <- ImageDetectionServer.run[IO](serviceConfig, xa).evalOn(customEc)
    } yield ()
  }
