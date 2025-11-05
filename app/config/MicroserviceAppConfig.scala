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

  def statusDeterminationServiceURL: String

  val statusDeterminationServiceAuthorisationToken: String
  val statusDeterminationServiceEnvironment: String

  val getHipBaseURL: String
  def getHipAuthToken: String
}

@Singleton
class MicroserviceAppConfig @Inject()(servicesConfig: ServicesConfig, val configuration: Configuration) extends AppConfig {

  private def loadConfig(key: String) = servicesConfig.getString(key)

  override lazy val statusDeterminationServiceURL: String = servicesConfig.baseUrl("status-determination-service")

  private val statusDeterminationServiceBase = "microservice.services.status-determination-service"
  override lazy val statusDeterminationServiceAuthorisationToken: String = s"Bearer ${loadConfig(s"$statusDeterminationServiceBase.authorization-token")}"
  override lazy val statusDeterminationServiceEnvironment: String = loadConfig(s"$statusDeterminationServiceBase.environment")

  lazy val mongoUri: String = loadConfig("mongodb.uri")

  lazy val timeToLiveSeconds: Int = loadConfig("mongodb.timeToLiveSeconds").toInt
  lazy val sessionTimeToLiveSeconds: Int = loadConfig("mongodb.sessionTimeToLiveSeconds").toInt
  lazy val throttleTimeToLiveSeconds: Int = loadConfig("mongodb.throttleTimeToLiveSeconds").toInt

  override val getHipBaseURL: String =
    servicesConfig.baseUrl("hip")

  private val appClientIdForHip: String =
    loadConfig("microservice.services.hip.creds.clientId")

  private val appClientSecretForHip: String =
    loadConfig("microservice.services.hip.creds.clientSecret")

  override def getHipAuthToken: String = {
    val parts = Seq(
      appClientIdForHip,
      appClientSecretForHip
    )

    "Basic " + Base64.getEncoder.encodeToString(parts.mkString(":").getBytes())
  }
}
