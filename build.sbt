name := """hmrc-todo-app"""
organization := "madetech"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.14"

libraryDependencies += guice
libraryDependencies += ws
libraryDependencies += "uk.gov.hmrc" %% "simple-reactivemongo" % "8.0.0-play-28"
libraryDependencies += "uk.gov.hmrc" %% "bootstrap-frontend-play-28" % "5.3.0"
libraryDependencies += "uk.gov.hmrc" %% "play-frontend-hmrc" % "0.69.0-play-28"
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test
libraryDependencies += "org.mockito" %% "mockito-scala" % "1.16.37" % Test

resolvers += MavenRepository("HMRC-open-artefacts-maven2", "https://open.artefacts.tax.service.gov.uk/maven2")

TwirlKeys.templateImports ++= Seq(
  "uk.gov.hmrc.govukfrontend.views.html.components._",
  "uk.gov.hmrc.govukfrontend.views.html.helpers._",
  "uk.gov.hmrc.hmrcfrontend.views.html.components._",
  "uk.gov.hmrc.hmrcfrontend.views.html.helpers._"
)


// Adds additional packages into Twirl
//TwirlKeys.templateImports += "madetech.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "madetech.binders._"
