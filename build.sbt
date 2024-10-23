val Http4sVersion          = "0.23.28"
val CirceVersion           = "0.14.10"
val MunitVersion           = "1.0.2"
val LogbackVersion         = "1.5.11"
val MunitCatsEffectVersion = "2.0.0"

lazy val root = (project in file("."))
  .settings(
    organization := "nathangaar",
    name         := "image-detection-scala",
    version      := "0.0.1-SNAPSHOT",
    scalaVersion := "3.3.3",
    // scalacOptions ++= Seq(
    //   "-deprecation",
    //   "-explaintypes",
    //   "-feature",
    //   "-unchecked",
    //   "-Wvalue-discard",
    //   "-Wunused:imports",
    //   "-Wunused:locals",
    //   "-Wunused:privates",
    //   "-Wunused:patvars",
    //   "-Wunused:params",
    //   "-Wunused:params-false",
    //   "-Xlint",
    //   "-Xfatal-warnings",
    //   "-Ysafe-init",
    //   "-source:future",
    //   "-Xcheckinit",
    //   "-Vimplicits",
    //   "-Vtype-diffs"
    // ),
    libraryDependencies ++= Seq(
      "org.http4s"            %% "http4s-ember-server" % Http4sVersion,
      "org.http4s"            %% "http4s-ember-client" % Http4sVersion,
      "org.http4s"            %% "http4s-circe"        % Http4sVersion,
      "org.http4s"            %% "http4s-dsl"          % Http4sVersion,
      "io.circe"              %% "circe-generic"       % CirceVersion,
      "io.circe"              %% "circe-parser"        % CirceVersion,
      "org.scalameta"         %% "munit"               % MunitVersion           % Test,
      "org.typelevel"         %% "munit-cats-effect"   % MunitCatsEffectVersion % Test,
      "ch.qos.logback"         % "logback-classic"     % LogbackVersion         % Runtime,
      "org.tpolecat"          %% "doobie-h2"           % "1.0.0-RC4", // H2 driver 1.4.200 + type mappings.
      "org.tpolecat"          %% "doobie-hikari"       % "1.0.0-RC4", // HikariCP transactor.
      "org.tpolecat"          %% "doobie-postgres"     % "1.0.0-RC4", // Postgres driver 42.6.0 + type mappings.
      "org.tpolecat"          %% "doobie-postgres"     % "1.0.0-RC4",
      "org.tpolecat"          %% "doobie-scalatest"    % "1.0.0-RC4"            % "test",
      "com.github.pureconfig" %% "pureconfig-core"     % "0.17.7",
      "com.github.pureconfig" %% "pureconfig-http4s"   % "0.17.7",
      "org.flywaydb"           % "flyway-core"         % "9.22.0",
      "org.fusesource.jansi"   % "jansi"               % "2.4.0"
    ),
    assembly / assemblyMergeStrategy := {
      case "module-info.class" => MergeStrategy.discard
      case x                   => (assembly / assemblyMergeStrategy).value.apply(x)
    }
  )
