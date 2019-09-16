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

package connectors

import config.AppConfig
import connectors.RegistrationConnector._
import javax.inject.Inject
import play.api.libs.json._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.HttpReads.readRaw
import uk.gov.hmrc.http.logging.Authorization
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

class RegistrationConnector @Inject()(appConfig: AppConfig,
                                      httpClient: HttpClient)(implicit ec: ExecutionContext) {
  def register(nino: String, isAnAgent: Boolean)(implicit hc: HeaderCarrier): Future[RegistrationSuccess.type] = {
    val headerCarrier = hc
      .withExtraHeaders(appConfig.desEnvironmentHeader)
      .copy(authorization = Some(Authorization(appConfig.desAuthorisationToken)))

    httpClient.POST(appConfig.desURL + registrationUrl(nino), registerRequestBody(isAnAgent))(
      implicitly[Writes[JsObject]],
      readRaw,
      headerCarrier,
      implicitly[ExecutionContext]
    ) map (_ => RegistrationSuccess)
  }
}

case object RegistrationSuccess

object RegistrationConnector {
  val RegimeKey = "regime"
  val ItsaRegime = "ITSA"
  val RequiresNameMatchkey = "requiresNameMatch"
  val IsAnAgentKey = "isAnAgent"

  def registrationUrl(nino: String): String = s"/registration/individual/nino/$nino"

  def registerRequestBody(isAnAgent: Boolean): JsObject = {
    Json.obj(
      RegimeKey -> ItsaRegime,
      RequiresNameMatchkey -> false,
      IsAnAgentKey -> isAnAgent
    )
  }
}
