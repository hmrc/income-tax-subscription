/*
 * Copyright 2022 HM Revenue & Customs
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

package config

import config.featureswitch.FeatureSwitching
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import javax.inject.{Inject, Singleton}


trait AppConfig {
  val configuration: Configuration
  val authURL: String
  val ggURL: String
  val ggAdminURL: String
  val ggAuthenticationURL: String

  val timeToLiveSeconds: Int
  val throttleTimeToLiveSeconds: Int

  val mongoUri: String

  def desURL: String

  val desEnvironment: String
  val desToken: String
  val paperlessPreferencesExpirySeconds: Int
  val desAuthorisationToken: String
  val desEnvironmentHeader: (String, String)

  def statusDeterminationServiceURL: String
  val statusDeterminationServiceAuthorisationToken: String
  val statusDeterminationServiceEnvironment: String
}

@Singleton
class MicroserviceAppConfig @Inject()(servicesConfig: ServicesConfig, val configuration: Configuration) extends AppConfig {

  private def loadConfig(key: String) = servicesConfig.getString(key)

  override lazy val authURL: String = servicesConfig.baseUrl("auth")
  override lazy val ggAuthenticationURL: String = servicesConfig.baseUrl("gg-authentication")
  override lazy val ggURL: String = servicesConfig.baseUrl("government-gateway")
  override lazy val ggAdminURL: String = servicesConfig.baseUrl("gg-admin")

  override lazy val statusDeterminationServiceURL: String = servicesConfig.baseUrl("status-determination-service")

  private val statusDeterminationServiceBase = "microservice.services.status-determination-service"
  override lazy val statusDeterminationServiceAuthorisationToken: String = s"Bearer ${loadConfig(s"$statusDeterminationServiceBase.authorization-token")}"
  override lazy val statusDeterminationServiceEnvironment: String= loadConfig(s"$statusDeterminationServiceBase.environment")

  private def desBase =
    if (FeatureSwitching.isEnabled(featureswitch.StubDESFeature, configuration)) "microservice.services.stub-des"
    else "microservice.services.des"

  override def desURL: String = loadConfig(s"$desBase.url")

  lazy val desAuthorisationToken: String = s"Bearer ${loadConfig(s"$desBase.authorization-token")}"

  lazy val desEnvironmentHeader: (String, String) =
    "Environment" -> loadConfig(s"$desBase.environment")

  override lazy val desEnvironment: String = loadConfig(s"$desBase.environment")
  override lazy val desToken: String = loadConfig(s"$desBase.authorization-token")
  override val paperlessPreferencesExpirySeconds: Int = {
    servicesConfig.getInt(s"paperless-preference.expiry-seconds")
  }

  lazy val mongoUri: String = loadConfig("mongodb.uri")

  lazy val timeToLiveSeconds: Int = loadConfig("mongodb.timeToLiveSeconds").toInt
  lazy val throttleTimeToLiveSeconds: Int = loadConfig("mongodb.throttleTimeToLiveSeconds").toInt

  def businessSubscribeUrl(nino: String): String = s"$desURL/income-tax-self-assessment/nino/$nino/business"

  def propertySubscribeUrl(nino: String): String = s"$desURL/income-tax-self-assessment/nino/$nino/properties"
}
