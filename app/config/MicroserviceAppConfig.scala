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

import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.util.Base64
import javax.inject.{Inject, Singleton}

trait AppConfig {
  val configuration: Configuration

  val timeToLiveSeconds: Int
  val sessionTimeToLiveSeconds: Int
  val throttleTimeToLiveSeconds: Int

  val mongoUri: String

  val itsaIncomeSourceURL: String
  val itsaIncomeSourceAuthorisationToken: String

  val hipBusinessDetailsURL: String
  val getItsaBusinessDetailsEnvironmentToken: String

  def statusDeterminationServiceURL: String

  val statusDeterminationServiceAuthorisationToken: String
  val statusDeterminationServiceEnvironment: String

  def taxableEntityAPI: String

  def getITSAStatusAuthorisationToken: String

  val hipSignUpServiceURL: String

  val hipPrePopURL: String

  val hipItsaStatusURL: String

  def getHipAuthToken(): String
}

@Singleton
class MicroserviceAppConfig @Inject()(servicesConfig: ServicesConfig, val configuration: Configuration) extends AppConfig {

  private def loadConfig(key: String) = servicesConfig.getString(key)

  override lazy val hipBusinessDetailsURL: String = servicesConfig.baseUrl("get-itsa-business-details")
  override lazy val getItsaBusinessDetailsEnvironmentToken = s"Basic ${loadConfig("microservice.services.get-itsa-business-details.authorization-token")}"

  override lazy val statusDeterminationServiceURL: String = servicesConfig.baseUrl("status-determination-service")

  private val statusDeterminationServiceBase = "microservice.services.status-determination-service"
  override lazy val statusDeterminationServiceAuthorisationToken: String = s"Bearer ${loadConfig(s"$statusDeterminationServiceBase.authorization-token")}"
  override lazy val statusDeterminationServiceEnvironment: String = loadConfig(s"$statusDeterminationServiceBase.environment")

  override lazy val taxableEntityAPI: String = servicesConfig.baseUrl("taxable-entity-api")
  override lazy val getITSAStatusAuthorisationToken = s"Basic ${loadConfig("microservice.services.taxable-entity-api.get-itsa-status.authorization-token")}"

  override lazy val hipSignUpServiceURL: String = servicesConfig.baseUrl("hip-signup-tax-year-service")

  lazy val itsaIncomeSourceURL: String = servicesConfig.baseUrl("itsa-income-source")
  lazy val itsaIncomeSourceAuthorisationToken: String = s"Basic ${loadConfig("microservice.services.itsa-income-source.authorization-token")}"

  lazy val mongoUri: String = loadConfig("mongodb.uri")

  lazy val timeToLiveSeconds: Int = loadConfig("mongodb.timeToLiveSeconds").toInt
  lazy val sessionTimeToLiveSeconds: Int = loadConfig("mongodb.sessionTimeToLiveSeconds").toInt
  lazy val throttleTimeToLiveSeconds: Int = loadConfig("mongodb.throttleTimeToLiveSeconds").toInt

  override lazy val hipPrePopURL: String =
    servicesConfig.baseUrl("hip-pre-pop")

  override val hipItsaStatusURL: String =
    servicesConfig.baseUrl("hip-itsa-status")

  private val appClientIdForHip: String =
    loadConfig("hip.clientId")

  private val appClientSecretForHip: String =
    loadConfig("hip.clientSecret")

  override def getHipAuthToken(): String = {
    val parts = Seq(
      appClientIdForHip,
      appClientSecretForHip
    )

    "Basic " + Base64.getEncoder.encodeToString(parts.mkString(":").getBytes())
  }
}
