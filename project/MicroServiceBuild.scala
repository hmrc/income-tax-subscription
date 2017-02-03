import sbt._

object MicroServiceBuild extends Build with MicroService {

  val appName = "income-tax-subscription"

  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()
}

private object AppDependencies {
  import play.PlayImport._
  import play.core.PlayVersion

  private val hmrcPlayJsonLoggerVersion = "2.1.1"
  private val microserviceBootstrapVersion = "4.4.0"
  private val playAuthVersion = "3.3.0"
  private val playHealthVersion = "1.1.0"
  private val playUrlBindersVersion = "1.0.0"
  private val playConfigVersion = "2.0.1"
  private val domainVersion = "3.1.0"
  private val hmrcTestVersion = "1.4.0"
  private val pegdownVersion = "1.6.0"
  private val referenceCheckerVersion = "2.0.0"
  private val scalaTestVersion = "2.2.2"
  private val scalaTestPlusVersion = "1.2.0"
  private val scalaJVersion = "1.1.6"
  private val cucumberVersion = "1.2.4"
  private val wireMockVersion = "1.57"
  private val junitVersion = "4.12"
  private val seleniumVersion = "2.50.0"

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

  object Test {
    def apply() = new TestDependencies {
      override lazy val test = Seq(
        "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope,
        "org.scalatest" %% "scalatest" % scalaTestVersion % scope,
        "org.scalatestplus" %% "play" % scalaTestPlusVersion % scope,
        "org.pegdown" % "pegdown" % pegdownVersion % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "info.cukes" %% "cucumber-scala" % cucumberVersion % scope,
        "info.cukes" % "cucumber-junit" % cucumberVersion % scope,
        "org.scalaj" %% "scalaj-http" % scalaJVersion,
        "junit" % "junit" % junitVersion % scope,
        "com.github.tomakehurst" % "wiremock" % wireMockVersion % scope,
        "org.seleniumhq.selenium" % "selenium-java" % seleniumVersion % scope
      )
    }.test
  }

  object IntegrationTest {
    def apply() = new TestDependencies {

      override lazy val scope: String = "it"

      override lazy val test = Seq(
        "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope,
        "org.scalatest" %% "scalatest" % scalaTestVersion % scope,
        "org.scalatestplus" %% "play" % scalaTestPlusVersion % scope,
        "org.pegdown" % "pegdown" % pegdownVersion % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "com.github.tomakehurst" % "wiremock" % wireMockVersion % scope,
        "info.cukes" %% "cucumber-scala" % cucumberVersion % scope,
        "info.cukes" % "cucumber-junit" % cucumberVersion % scope,
        "junit" % "junit" % junitVersion % scope,
        "org.scalaj" %% "scalaj-http" % scalaJVersion,
        "org.seleniumhq.selenium" % "selenium-java" % seleniumVersion % scope
      )
    }.test
  }

  def apply() = compile ++ Test() ++ IntegrationTest()
}

