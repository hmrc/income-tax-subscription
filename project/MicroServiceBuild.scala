import sbt._

object MicroServiceBuild extends Build with MicroService {
  import scala.util.Properties.envOrElse

  val appName = "income-tax-subscription"
  val appVersion = envOrElse("API_EXAMPLE_MICROSERVICE_VERSION", "999-SNAPSHOT")

  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()
}

private object AppDependencies {
  import play.PlayImport._
  import play.core.PlayVersion

  private val microserviceBootstrapVersion = "4.4.0"
  private val playHealthVersion = "1.1.0"
  private val playConfigVersion = "2.0.1"
  private val hmrcTestVersion = "1.4.0"
  private val wireMockVersion = "1.57"
  private val scalaJVersion = "1.1.5"
  private val scalaTestVersion = "2.2.5"
  private val hmrcPlayJsonLoggerVersion = "2.1.1"
  private val pegdownVersion = "1.6.0"
  private val cucumberVersion = "1.2.4"

  val compile = Seq(

    ws,
    "uk.gov.hmrc" %% "microservice-bootstrap" % microserviceBootstrapVersion,
    "uk.gov.hmrc" %% "play-health" % playHealthVersion,
    "uk.gov.hmrc" %% "play-config" % playConfigVersion,
    "uk.gov.hmrc" %% "play-json-logger" % hmrcPlayJsonLoggerVersion
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test : Seq[ModuleID] = ???
  }

  val test = Seq(
        "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % "test,it",
        "org.scalaj" %% "scalaj-http" % scalaJVersion % "test,it",
        "org.scalatest" %% "scalatest" % scalaTestVersion % "test,it",
        "org.pegdown" % "pegdown" % pegdownVersion % "test,it",
        "com.typesafe.play" %% "play-test" % PlayVersion.current % "test,it",
        "com.github.tomakehurst" % "wiremock" % wireMockVersion % "test,it",
        "info.cukes" %% "cucumber-scala" % cucumberVersion % "test,it",
        "info.cukes" % "cucumber-junit" % cucumberVersion % "test,it"
  )

  def apply() = compile ++ test
}

