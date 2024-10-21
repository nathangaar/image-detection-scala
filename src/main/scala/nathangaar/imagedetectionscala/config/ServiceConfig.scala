package nathangaar.imagedetectionscala.config

import org.http4s.Uri
import pureconfig.*
import pureconfig.generic.derivation.default.*
import pureconfig.module.http4s.*

final case class ServiceConfig(
  threadPoolSize: Int,
  imagga: ImaggaConfig,
  database: DatabaseConfig
) derives ConfigReader

final case class ImaggaConfig(user: String, secret: String, url: Uri)

final case class DatabaseConfig(
  user: String,
  password: String,
  databaseUrl: String,
  name: String,
  migrationsEnabled: Boolean
)
