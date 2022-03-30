/*
 * Copyright 2019 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import sbt._

private object MicroServiceBuild {
  val appName = "income-tax-subscription"
  lazy val appDependencies: Seq[ModuleID] = AppDependencies()
}

private object AppDependencies {

  import play.core.PlayVersion
  import play.sbt.PlayImport._

  private val domainVersion = "8.0.0-play-28"
  private val scalaTestVersion = "3.2.11"
  private val scalaTestPlusVersion = "5.1.0"
  private val mockitoVersion = "4.4.0"

  private val scalaJVersion = "2.4.2"

  private val reactiveMongoVersion = "8.0.0-play-28"

  private val wiremockVersion = "2.32.0"

  private val bootstrapBackendVersion = "5.21.0"

  private val playVersion = "2.8.14"

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-backend-play-28" % bootstrapBackendVersion,
    "uk.gov.hmrc" %% "domain" % domainVersion,
    "uk.gov.hmrc" %% "simple-reactivemongo" % reactiveMongoVersion
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test: Seq[ModuleID] = Seq()
  }

  object Test {
    def apply(): Seq[ModuleID] = new TestDependencies {
      override lazy val test = Seq(
        "uk.gov.hmrc" %% "bootstrap-backend-play-28" % bootstrapBackendVersion % scope,
        "org.scalatest" %% "scalatest" % scalaTestVersion % scope,
        "org.scalatestplus" %% "mockito-3-12" % "3.2.10.0" % scope,
        "com.typesafe.play" %% "play-test" % playVersion % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusVersion % scope,
        "com.vladsch.flexmark" % "flexmark-all" % "0.62.2" % scope,
        "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.13.2" % scope,
        "org.scalaj" %% "scalaj-http" % scalaJVersion % scope,
        "org.mockito" % "mockito-core" % mockitoVersion % scope,
        "com.github.fge" % "json-schema-validator" % "2.2.14" % scope
      )
    }.test
  }

  object IntegrationTest {
    def apply(): Seq[ModuleID] = new TestDependencies {

      override lazy val scope: String = "it"

      override lazy val test = Seq(
        "org.scalatest" %% "scalatest" % scalaTestVersion % scope,
        "com.typesafe.play" %% "play-test" % playVersion % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusVersion % scope,
        "com.vladsch.flexmark" % "flexmark-all" % "0.62.2" % scope,
        "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.13.2" % scope,
        "org.scalaj" %% "scalaj-http" % scalaJVersion % scope,
        "org.mockito" % "mockito-core" % mockitoVersion % scope,
        "com.github.fge" % "json-schema-validator" % "2.2.14" % scope,
        "com.github.tomakehurst" % "wiremock" % wiremockVersion % scope
      )
    }.test
  }

  def tmpMacWorkaround(): Seq[ModuleID] =
    if (sys.props.get("os.name").exists(_.toLowerCase.contains("mac")))
      Seq("org.reactivemongo" % "reactivemongo-shaded-native" % "1.0.1-osx-x86-64" % "runtime,test,it")
    else Seq()

  def apply(): Seq[ModuleID] = compile ++ Test() ++ IntegrationTest() ++ tmpMacWorkaround()

}
