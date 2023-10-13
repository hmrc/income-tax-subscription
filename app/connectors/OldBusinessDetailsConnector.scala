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

package connectors

import config.AppConfig
import connectors.utilities.ConnectorUtils
import models.ErrorModel
import models.monitoring.getBusinessDetails.BusinessDetailsAuditModel
import models.registration.{GetBusinessDetailsFailureResponseModel, OldGetBusinessDetailsSuccessResponseModel}
import play.api.Logger
import play.api.http.Status._
import play.api.mvc.Request
import services.monitoring.AuditService
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, HttpClient, HttpReads, HttpResponse}
import utils.Logging._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class OldBusinessDetailsConnector @Inject()(appConfig: AppConfig,
                                            httpClient: HttpClient,
                                            auditService: AuditService
                                           )(implicit ec: ExecutionContext) extends RawResponseReads {

  val logger: Logger = Logger(this.getClass)

  lazy val urlHeaderAuthorization: String = s"Bearer ${appConfig.desToken}"

  // API 5
  def getBusinessDetailsUrl(nino: String): String = s"${appConfig.desURL}${OldBusinessDetailsConnector.getBusinessDetailsUri(nino)}"

  val desHeaders: Seq[(String, String)] = Seq(
    HeaderNames.authorisation -> appConfig.desAuthorisationToken,
    appConfig.desEnvironmentHeader
  )

  def getBusinessDetails(nino: String)(implicit hc: HeaderCarrier, request: Request[_]): Future[OldGetBusinessDetailsUtil.Response] = {
    import OldGetBusinessDetailsUtil._
    lazy val requestDetails: Map[String, String] = Map("nino" -> nino)
    logger.debug(s"BusinessDetailsConnector.getBusinessDetails - Request:\n$requestDetails\n\nRequest Headers:\n$hc")

    httpClient.GET[HttpResponse](getBusinessDetailsUrl(nino), headers = desHeaders)(implicitly[HttpReads[HttpResponse]], hc, ec)
      .map { response =>
        response.status match {
          case OK =>
            logger.info("BusinessDetailsConnector.getBusinessDetails - Get Business Details responded with OK")
            parseSuccess(response.body)
          case status =>
            val parseResponse@Left(ErrorModel(_, optCode, message)) = parseFailure(status, response.body)
            val code: String = optCode.getOrElse("N/A")
            (status, code) match {
              case (NOT_FOUND, "NOT_FOUND_NINO") =>
                // expected case, do not audit
                logger.info(s"BusinessDetailsConnector.getBusinessDetails - Get Business Details responded with nino not found")
              case _ =>
                val suffix = status match {
                  case BAD_REQUEST => eventTypeBadRequest
                  case NOT_FOUND => eventTypeNotFound
                  case SERVICE_UNAVAILABLE => eventTypeServerUnavailable
                  case INTERNAL_SERVER_ERROR => eventTypeInternalServerError
                  case _ => eventTypeUnexpectedError
                }
                auditService.audit(BusinessDetailsAuditModel(nino, suffix, response.body))(hc, ec, request)
                logger.warn(s"OldBusinessDetailsConnector.getBusinessDetails - Get Business Details responded with an error," +
                  s" status=$status code=$code message=$message")
            }
            parseResponse
        }
      }
  }
}

object OldBusinessDetailsConnector {
  def getBusinessDetailsUri(nino: String): String = s"/registration/business-details/nino/$nino"
}

object OldGetBusinessDetailsUtil extends ConnectorUtils[GetBusinessDetailsFailureResponseModel, OldGetBusinessDetailsSuccessResponseModel]
