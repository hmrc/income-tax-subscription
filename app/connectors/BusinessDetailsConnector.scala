/*
 * Copyright 2017 HM Revenue & Customs
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
import models.registration.{GetBusinessDetailsFailureResponseModel, GetBusinessDetailsSuccessResponseModel}
import play.api.http.Status._
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.http.logging.Authorization

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class BusinessDetailsConnector @Inject()(appConfig: AppConfig,
                                         logging: Logging,
                                         httpGet: HttpGet
                                        ) extends ServicesConfig with RawResponseReads {

  import Logging._

  lazy val urlHeaderAuthorization: String = s"Bearer ${appConfig.desToken}"

  // API 5
  val getBusinessDetailsUrl: String => String = (nino: String) => s"${appConfig.desURL}/registration/business-details/nino/$nino"

  def createHeaderCarrierGet(headerCarrier: HeaderCarrier): HeaderCarrier =
    headerCarrier.withExtraHeaders("Environment" -> appConfig.desEnvironment)
      .copy(authorization = Some(Authorization(urlHeaderAuthorization)))

  def getBusinessDetails(nino: String)(implicit hc: HeaderCarrier): Future[GetBusinessDetailsUtil.Response] = {
    import BusinessDetailsConnector.auditGetBusinessDetails
    import GetBusinessDetailsUtil._

    implicit val loggingConfig = RegistrationConnector.getRegistrationLoggingConfig
    lazy val requestDetails: Map[String, String] = Map("nino" -> nino)
    val updatedHc = createHeaderCarrierGet(hc)

    lazy val auditRequest = logging.auditFor(auditGetBusinessDetails, requestDetails)(updatedHc)
    auditRequest(eventTypeRequest)

    logging.debug(s"Request:\n$requestDetails\n\nRequest Headers:\n$updatedHc")
    httpGet.GET[HttpResponse](getBusinessDetailsUrl(nino))(implicitly[HttpReads[HttpResponse]], updatedHc)
      .map { response =>

        val status = response.status
        lazy val audit = logging.auditFor(auditGetBusinessDetails, requestDetails + ("response" -> response.body))(updatedHc)

        status match {
          case OK =>
            audit(auditGetBusinessDetails + "-" + eventTypeSuccess)
            parseSuccess(response.body)
          case BAD_REQUEST =>
            audit(auditGetBusinessDetails + "-" + eventTypeBadRequest)
            parseFailure(BAD_REQUEST, response.body)
          case NOT_FOUND =>
            audit(auditGetBusinessDetails + "-" + eventTypeNotFound)
            parseFailure(NOT_FOUND, response.body)
          case INTERNAL_SERVER_ERROR =>
            audit(auditGetBusinessDetails + "-" + eventTypeInternalServerError)
            parseFailure(INTERNAL_SERVER_ERROR, response.body)
          case SERVICE_UNAVAILABLE =>
            audit(auditGetBusinessDetails + "-" + eventTypeServerUnavailable)
            parseFailure(SERVICE_UNAVAILABLE, response.body)
          case x =>
            audit(auditGetBusinessDetails + "-" + eventTypeUnexpectedError)
            parseFailure(x, response.body)
        }
      }
  }
}

object BusinessDetailsConnector {

  val auditGetBusinessDetails = "getBusinessDetails api-5"

  import _root_.utils.Implicits.OptionUtl

  val getRegistrationLoggingConfig: Option[LoggingConfig] = LoggingConfig(heading = "BusinessDetailsConnector.getBusinessDetails")

}

object GetBusinessDetailsUtil extends ConnectorUtils[GetBusinessDetailsFailureResponseModel, GetBusinessDetailsSuccessResponseModel]
