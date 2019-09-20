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

package connectors.deprecated

import config.AppConfig
import connectors.RawResponseReads
import connectors.utilities.ConnectorUtils
import javax.inject.Inject
import models.ErrorModel
import models.monitoring.getRegistration.{getRegistrationModel, getRegistrationShortModel}
import models.monitoring.registerAudit.registerAuditModel
import models.registration._
import play.api.http.Status._
import play.api.libs.json.{JsValue, Writes}
import play.api.mvc.Request
import services.monitoring.AuditService
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.logging.Authorization
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import utils.{Logging, LoggingConfig}

import scala.annotation.switch
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class RegistrationConnector @Inject()(appConfig: AppConfig,
                                      logging: Logging,
                                      httpClient: HttpClient,
                                      auditService: AuditService
                                     )(implicit ec: ExecutionContext) extends RawResponseReads {

  import Logging._

  lazy val urlHeaderAuthorization: String = s"Bearer ${appConfig.desToken}"

  // DES API numbering [MTD API numbering]
  // API4 [API 9]
  def newRegistrationUrl(nino: String): String = s"${appConfig.desURL}${RegistrationConnector.newRegistrationUri(nino)}"

  // API 1(b) [API 1 (b)]
  def getRegistrationUrl(nino: String): String = s"${appConfig.desURL}${RegistrationConnector.getRegistrationUri(nino)}"

  def createHeaderCarrierPost(headerCarrier: HeaderCarrier): HeaderCarrier =
    headerCarrier.withExtraHeaders("Environment" -> appConfig.desEnvironment, "Content-Type" -> "application/json")
      .copy(authorization = Some(Authorization(urlHeaderAuthorization)))

  def createHeaderCarrierGet(headerCarrier: HeaderCarrier): HeaderCarrier =
    headerCarrier.withExtraHeaders("Environment" -> appConfig.desEnvironment)
      .copy(authorization = Some(Authorization(urlHeaderAuthorization)))

  def register(nino: String, registration: RegistrationRequestModel)(implicit hc: HeaderCarrier, request: Request[_]): Future[NewRegistrationUtil.Response] = {
    import NewRegistrationUtil._

    implicit val loggingConfig = RegistrationConnector.registerLoggingConfig
    lazy val requestDetails: Map[String, String] = Map("nino" -> nino, "requestJson" -> (registration: JsValue).toString)
    val updatedHc = createHeaderCarrierPost(hc)

    logging.debug(s"Request:\n$requestDetails\n\nRequest Headers:\n$updatedHc")
    httpClient.POST[RegistrationRequestModel, HttpResponse](newRegistrationUrl(nino), registration)(
      implicitly[Writes[RegistrationRequestModel]], implicitly[HttpReads[HttpResponse]], updatedHc, ec)
      .map { response =>
        response.status match {
          case OK =>
            logging.info("Registration responded with OK")
            parseSuccess(response.body)
          case status =>
            @switch
            val suffix = status match {
              case BAD_REQUEST => eventTypeBadRequest
              case NOT_FOUND => eventTypeNotFound
              case CONFLICT => eventTypeConflict
              case INTERNAL_SERVER_ERROR => eventTypeInternalServerError
              case SERVICE_UNAVAILABLE => eventTypeServerUnavailable
              case _ => eventTypeUnexpectedError
            }
            auditService.audit(registerAuditModel(nino, suffix, registration, response.body))(updatedHc, ec, request)

            val parseResponse@Left(ErrorModel(_, optCode, message)) = parseFailure(status, response.body)
            val code: String = optCode.getOrElse("N/A")
            logging.warn(s"Registration responded with an error, status=$status code=$code message=$message")

            parseResponse
        }
      }
  }

  def getRegistration(nino: String)(implicit hc: HeaderCarrier, request: Request[_]): Future[GetRegistrationUtil.Response] = {
    import GetRegistrationUtil._

    implicit val loggingConfig = RegistrationConnector.getRegistrationLoggingConfig
    lazy val requestDetails: Map[String, String] = Map("nino" -> nino)
    val updatedHc = createHeaderCarrierGet(hc)

    auditService.audit(getRegistrationShortModel(nino, eventTypeRequest))(updatedHc, ec, request)

    logging.debug(s"Request:\n$requestDetails\n\nRequest Headers:\n$updatedHc")
    httpClient.GET[HttpResponse](getRegistrationUrl(nino))(implicitly[HttpReads[HttpResponse]], updatedHc, ec)
      .map { response =>
        response.status match {
          case OK =>
            logging.info("Get Registration responded with an OK")
            parseSuccess(response.body)
          case status =>
            @switch
            val suffix = status match {
              case BAD_REQUEST => eventTypeBadRequest
              case NOT_FOUND => eventTypeNotFound
              case INTERNAL_SERVER_ERROR => eventTypeInternalServerError
              case SERVICE_UNAVAILABLE => eventTypeServerUnavailable
              case _ => eventTypeUnexpectedError
            }
            auditService.audit(getRegistrationModel(nino, suffix, response.body))(updatedHc, ec, request)

            val parseResponse@Left(ErrorModel(_, optCode, message)) = parseFailure(status, response.body)
            val code: String = optCode.getOrElse("N/A")
            logging.warn(s"Get Registration responded with an error, status=$status code=$code message=$message")

            parseResponse
        }
      }
  }

}

object RegistrationConnector {

  val auditGetRegistrationName = "getRegistration-api-1(b)"

  import _root_.utils.Implicits.optionUtl

  val registerLoggingConfig: Option[LoggingConfig] = LoggingConfig(heading = "RegistrationConnector.register")

  val getRegistrationLoggingConfig: Option[LoggingConfig] = LoggingConfig(heading = "RegistrationConnector.getRegistration")

  // DES API numbering [MTD API numbering]
  // API4 [API 9]
  def newRegistrationUri(nino: String): String = s"/registration/individual/nino/$nino"

  // API 1(b) [API 1 (b)]
  def getRegistrationUri(nino: String): String = s"/registration/details?nino=$nino"
}


object NewRegistrationUtil extends ConnectorUtils[NewRegistrationFailureResponseModel, RegistrationSuccessResponseModel]

object GetRegistrationUtil extends ConnectorUtils[GetRegistrationFailureResponseModel, RegistrationSuccessResponseModel]
