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

import javax.inject.Inject

import audit.{Logging, LoggingConfig}
import config.AppConfig
import connectors.utils.ConnectorUtils
import models.ErrorModel
import models.registration.{GetBusinessDetailsFailureResponseModel, GetBusinessDetailsSuccessResponseModel}
import play.api.http.Status._
import uk.gov.hmrc.http.logging.Authorization
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.annotation.switch
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class BusinessDetailsConnector @Inject()(appConfig: AppConfig,
                                         logging: Logging,
                                         httpClient: HttpClient
                                        )(implicit ec: ExecutionContext) extends RawResponseReads {

  import Logging._

  lazy val urlHeaderAuthorization: String = s"Bearer ${appConfig.desToken}"

  // API 5
  def getBusinessDetailsUrl(nino: String): String = s"${appConfig.desURL}${BusinessDetailsConnector.getBusinessDetailsUri(nino)}"

  def createHeaderCarrierGet(headerCarrier: HeaderCarrier): HeaderCarrier =
    headerCarrier.withExtraHeaders("Environment" -> appConfig.desEnvironment)
      .copy(authorization = Some(Authorization(urlHeaderAuthorization)))

  def getBusinessDetails(nino: String)(implicit hc: HeaderCarrier): Future[GetBusinessDetailsUtil.Response] = {
    import BusinessDetailsConnector.auditGetBusinessDetails
    import GetBusinessDetailsUtil._
    implicit val loggingConfig = RegistrationConnector.getRegistrationLoggingConfig
    lazy val requestDetails: Map[String, String] = Map("nino" -> nino)
    val updatedHc = createHeaderCarrierGet(hc)
    logging.debug(s"Request:\n$requestDetails\n\nRequest Headers:\n$updatedHc")

    httpClient.GET[HttpResponse](getBusinessDetailsUrl(nino))(implicitly[HttpReads[HttpResponse]], updatedHc, ec)
      .map { response =>
        response.status match {
          case OK =>
            logging.info("Get Business Details responded with OK")
            parseSuccess(response.body)
          case status =>
            @switch
            val suffix = status match {
              case BAD_REQUEST => eventTypeBadRequest
              case NOT_FOUND => eventTypeNotFound
              case SERVICE_UNAVAILABLE => eventTypeServerUnavailable
              case INTERNAL_SERVER_ERROR => eventTypeInternalServerError
              case _ => eventTypeUnexpectedError
            }

            val parseResponse@Left(ErrorModel(_, optCode, message)) = parseFailure(status, response.body)
            val code: String = optCode.getOrElse("N/A")
            (status, code) match {
              case (NOT_FOUND, "NOT_FOUND_NINO") =>
                // expected case, do not audit
                logging.info(s"Get Business Details responded with nino not found")
              case _ =>
                logging.audit(
                  transactionName = auditGetBusinessDetails,
                  detail = requestDetails + ("response" -> response.body),
                  auditType = auditGetBusinessDetails + "-" + suffix
                )(updatedHc)
                logging.warn(s"Get Business Details responded with an error, status=$status code=$code message=$message")
            }
            parseResponse
        }
      }
  }
}

object BusinessDetailsConnector {

  val auditGetBusinessDetails = "getBusinessDetails api-5"

  import _root_.utils.Implicits.optionUtl

  val getRegistrationLoggingConfig: Option[LoggingConfig] = LoggingConfig(heading = "BusinessDetailsConnector.getBusinessDetails")

  def getBusinessDetailsUri(nino: String): String = s"/registration/business-details/nino/$nino"
}

object GetBusinessDetailsUtil extends ConnectorUtils[GetBusinessDetailsFailureResponseModel, GetBusinessDetailsSuccessResponseModel]
