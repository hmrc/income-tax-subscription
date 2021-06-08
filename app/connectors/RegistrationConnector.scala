/*
 * Copyright 2021 HM Revenue & Customs
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
import models.ErrorModel
import models.monitoring.RegistrationFailureAudit
import play.api.Logger
import play.api.http.Status._
import play.api.libs.json._
import play.api.mvc.Request
import services.monitoring.AuditService
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, HeaderNames, HttpClient}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RegistrationConnector @Inject()(appConfig: AppConfig,
                                      httpClient: HttpClient,
                                      auditService: AuditService)(implicit ec: ExecutionContext) extends RawResponseReads {

  val logger: Logger = Logger(this.getClass)

  def registerUrl(nino: String): String = s"${appConfig.desURL}/registration/individual/nino/$nino"

  def register(nino: String, isAnAgent: Boolean)
              (implicit hc: HeaderCarrier, request: Request[_]): Future[Either[ErrorModel, RegistrationSuccess.type]] = {
    val headerCarrier = hc
      .withExtraHeaders(appConfig.desEnvironmentHeader)
      .copy(authorization = Some(Authorization(appConfig.desAuthorisationToken)))

    val desHeaders: Seq[(String, String)] = Seq(
      HeaderNames.authorisation -> appConfig.desAuthorisationToken,
      appConfig.desEnvironmentHeader
    )

    httpClient.POST(registerUrl(nino), registerRequestBody(isAnAgent), headers = desHeaders)(
      implicitly,
      httpReads,
      headerCarrier,
      implicitly
    ) map { response =>
      response.status match {
        case OK =>
          logger.info(s"[RegistrationConnector][register] - Successfully registered $nino")
          Right(RegistrationSuccess)
        case status =>
          logger.warn(s"[RegistrationConnector][register] - Failed to register $nino, status: $status")
          auditService.audit(RegistrationFailureAudit(nino, status, registerRequestBody(isAnAgent), response.body))(
            headerCarrier, implicitly, implicitly
          )
          Left(ErrorModel(status, s"Failed to register $nino"))
      }
    }
  }

}

case object RegistrationSuccess

object RegistrationConnector {

  val RegimeKey = "regime"
  val ItsaRegime = "ITSA"
  val RequiresNameMatchkey = "requiresNameMatch"
  val IsAnAgentKey = "isAnAgent"

  def registerRequestBody(isAnAgent: Boolean): JsObject = {
    Json.obj(
      RegimeKey -> ItsaRegime,
      RequiresNameMatchkey -> false,
      IsAnAgentKey -> isAnAgent
    )
  }
}
