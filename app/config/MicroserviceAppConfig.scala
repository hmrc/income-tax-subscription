/*
 * Copyright 2023 HM Revenue & Customs
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

  val timeToLiveSeconds: Int
  val sessionTimeToLiveSeconds: Int
  val throttleTimeToLiveSeconds: Int

  val mongoUri: String

  def desURL: String

  val desEnvironment: String
  val desToken: String
  val desAuthorisationToken: String
  val desEnvironmentHeader: (String, String)

  def getBusinessDetailsURL: String

  val getBusinessDetailsAuthorisationToken: String
  val getBusinessDetailsEnvironment: String

  def statusDeterminationServiceURL: String

  val statusDeterminationServiceAuthorisationToken: String
  val statusDeterminationServiceEnvironment: String

  def signUpServiceURL: String

  val signUpServiceAuthorisationToken: String
  val signUpServiceEnvironment: String

  val prePopURL: String
  val prePopAuthorisationToken: String
  val prePopEnvironment: String
}


@Singleton
class MicroserviceAppConfig @Inject()(servicesConfig: ServicesConfig, val configuration: Configuration) extends AppConfig {

  private def loadConfig(key: String) = servicesConfig.getString(key)

  override lazy val getBusinessDetailsURL: String = servicesConfig.baseUrl("get-business-details")

  private val getBusinessDetailsBase = "microservice.services.get-business-details"
  override lazy val getBusinessDetailsAuthorisationToken: String = s"Bearer ${loadConfig(s"$getBusinessDetailsBase.authorization-token")}"
  override lazy val getBusinessDetailsEnvironment: String = loadConfig(s"$getBusinessDetailsBase.environment")

  override lazy val statusDeterminationServiceURL: String = servicesConfig.baseUrl("status-determination-service")

  private val statusDeterminationServiceBase = "microservice.services.status-determination-service"
  override lazy val statusDeterminationServiceAuthorisationToken: String = s"Bearer ${loadConfig(s"$statusDeterminationServiceBase.authorization-token")}"
  override lazy val statusDeterminationServiceEnvironment: String = loadConfig(s"$statusDeterminationServiceBase.environment")


  override lazy val signUpServiceURL: String = servicesConfig.baseUrl("signup-tax-year-service")

  private val signUpServiceBase = "microservice.services.signup-tax-year-service"
  override lazy val signUpServiceAuthorisationToken: String = s"Bearer ${loadConfig(s"$signUpServiceBase.authorization-token")}"
  override lazy val signUpServiceEnvironment: String = loadConfig(s"$signUpServiceBase.environment")

  override lazy val prePopURL: String = servicesConfig.baseUrl("pre-pop")

  private val prePopBase = "microservice.services.pre-pop"
  override lazy val prePopAuthorisationToken: String = s"Bearer ${loadConfig(s"$prePopBase.authorization-token")}"
  override lazy val prePopEnvironment: String = loadConfig(s"$prePopBase.environment")

  private def desBase =
    if (FeatureSwitching.isEnabled(featureswitch.StubDESFeature, configuration)) "microservice.services.stub-des"
    else "microservice.services.des"

  override def desURL: String = loadConfig(s"$desBase.url")

  lazy val desAuthorisationToken: String = s"Bearer ${loadConfig(s"$desBase.authorization-token")}"

  lazy val desEnvironmentHeader: (String, String) =
    "Environment" -> loadConfig(s"$desBase.environment")

  override lazy val desEnvironment: String = loadConfig(s"$desBase.environment")
  override lazy val desToken: String = loadConfig(s"$desBase.authorization-token")

  lazy val mongoUri: String = loadConfig("mongodb.uri")

  lazy val timeToLiveSeconds: Int = loadConfig("mongodb.timeToLiveSeconds").toInt
  lazy val sessionTimeToLiveSeconds: Int = loadConfig("mongodb.sessionTimeToLiveSeconds").toInt
  lazy val throttleTimeToLiveSeconds: Int = loadConfig("mongodb.throttleTimeToLiveSeconds").toInt
}
