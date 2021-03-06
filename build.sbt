val AKKA_VERSION = "2.4.16"
val AKKA_HTTP_VERSION = "10.0.4"
val SCALATEST_VERSION = "3.0.0"
val SIGNPOST_VERSION = "1.2.1.2"
val JSON4S_VERSION = "3.5.0"

lazy val versionSettings = Seq(
  isSnapshot := {
    dynverGitDescribeOutput.value forall { gitVersion =>
      gitVersion.hasNoTags() || gitVersion.isDirty() || gitVersion.commitSuffix.distance > 0
    }
  },

  //  The 'version' setting is not set on purpose: its value is generated automatically by the sbt-dynver plugin
  //  based on the git tag/sha. Here we're just tacking on the maven-compatible snapshot suffix if needed
  version := {
    if (isSnapshot.value)
      version.value + "-SNAPSHOT"
    else
      version.value
  }
)

lazy val publicationSettings = Seq(
  publishTo := {
    if (isSnapshot.value)
      Some(s"Artifactory Realm" at "https://oss.jfrog.org/artifactory/oss-snapshot-local;build.timestamp=" + new java.util.Date().getTime)
    else
      publishTo.value //Here we are assuming that the bintray-sbt plugin does its magic and the publish settings are set to
                      //point to Bintray
  },
  credentials := {
    if (isSnapshot.value)
      List(Path.userHome / ".bintray" / ".artifactory").filter(_.exists).map(Credentials(_))
    else
      credentials.value
  },
  publishArtifact in Test := false,
  bintrayReleaseOnPublish := !isSnapshot.value
)

lazy val projectMetadataSettings = Seq(
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
  scmInfo := Some(
    ScmInfo(
      browseUrl = url("https://github.com/EmilDafinov/scala-ad-sdk"),
      connection = "scm:git:git@github.com:EmilDafinov/scala-ad-sdk.git"
    )
  ),
  developers := List(
    Developer(
      id = "edafinov",
      name = "Emil Dafinov",
      email = "emiliorodo@gmail.com",
      url = url("https://github.com/EmilDafinov")
    )
  )
)

lazy val scalaAdSdk = (project in file("."))
  .configs(IntegrationTest)
  .settings(Defaults.itSettings)
  .settings(projectMetadataSettings)
  .settings(versionSettings)
  .settings(publicationSettings)
  .settings(
    scalaVersion := "2.12.1",

    organization := "com.github.emildafinov",
    name := "scala-ad-sdk",

    libraryDependencies ++= Seq(
      //Application config
      "com.typesafe" % "config" % "1.3.0",

      //Logging
      "ch.qos.logback" % "logback-classic" % "1.1.7",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",

      //Authentication
      "oauth.signpost" % "signpost-core" % SIGNPOST_VERSION,
      "oauth.signpost" % "signpost-commonshttp4" % SIGNPOST_VERSION,

      //Http
      "com.typesafe.akka" %% "akka-actor" % AKKA_VERSION,
      "com.typesafe.akka" %% "akka-http-core" % AKKA_HTTP_VERSION,
      "com.typesafe.akka" %% "akka-http" % AKKA_HTTP_VERSION,
      "com.typesafe.akka" %% "akka-http-testkit" % AKKA_HTTP_VERSION,
      "com.typesafe.akka" %% "akka-http-xml" % AKKA_HTTP_VERSION,
      "io.github.lhotari" %% "akka-http-health" % "1.0.7",

      //Json
      "org.json4s" %% "json4s-jackson" % JSON4S_VERSION,
      "org.json4s" %% "json4s-ext" % JSON4S_VERSION,
      "de.heikoseeberger" %% "akka-http-json4s" % "1.13.0",

      //Test
      "org.scalactic" %% "scalactic" % SCALATEST_VERSION,
      "org.scalatest" %% "scalatest" % SCALATEST_VERSION % "it,test",
      "org.mockito" % "mockito-all" % "1.10.19" % "it,test",
      "com.github.tomakehurst" % "wiremock" % "2.5.1" % "it,test"
    )
  )
