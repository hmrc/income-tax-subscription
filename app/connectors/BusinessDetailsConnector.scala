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
import models.monitoring.getBusinessDetails.BusinessDetailsAuditModel
import parsers.GetBusinessDetailsParser.{GetBusinessDetailsResponse, getBusinessDetailsResponseHttpReads}
import play.api.Logging
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, NOT_FOUND, SERVICE_UNAVAILABLE}
import play.api.mvc.Request
import services.monitoring.AuditService
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, HeaderNames, HttpClient, HttpReads}
import utils.Logging._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessDetailsConnector @Inject()(appConfig: AppConfig,
                                         httpClient: HttpClient,
                                         auditService: AuditService
                                        )(implicit ec: ExecutionContext) extends RawResponseReads with Logging {

  def getBusinessDetails(nino: String)(implicit hc: HeaderCarrier, request: Request[_]): Future[GetBusinessDetailsResponse] = {

    val headerCarrier: HeaderCarrier = hc
      .copy(authorization = Some(Authorization(appConfig.getBusinessDetailsAuthorisationToken)))
      .withExtraHeaders("Environment" -> appConfig.getBusinessDetailsEnvironment)

    val headers: Seq[(String, String)] = Seq(
      HeaderNames.authorisation -> appConfig.getBusinessDetailsAuthorisationToken,
      "Environment" -> appConfig.getBusinessDetailsEnvironment
    )

    httpClient.GET[GetBusinessDetailsResponse](url = getBusinessDetailsUrl(nino), headers = headers)(
      implicitly[HttpReads[GetBusinessDetailsResponse]],
      headerCarrier,
      implicitly
    ) map {
      case Left(value) if value.status != NOT_FOUND || value.code.exists(_ != "NOT_FOUND_NINO") =>
        val suffix = value.status match {
          case BAD_REQUEST => eventTypeBadRequest
          case NOT_FOUND => eventTypeNotFound
          case SERVICE_UNAVAILABLE => eventTypeServerUnavailable
          case INTERNAL_SERVER_ERROR => eventTypeInternalServerError
          case _ => eventTypeUnexpectedError
        }
        auditService.audit(BusinessDetailsAuditModel(nino, suffix, value.reason))(hc, ec, request)
        logger.warn(s"BusinessDetailsConnector.getBusinessDetails - Get Business Details responded with an error," +
          s" status=${value.status} code=${value.code} message=${value.reason}")
        Left(value)
      case response => response
    }
  }

  private def getBusinessDetailsUrl(nino: String) =
    s"${appConfig.getBusinessDetailsURL}/registration/business-details/nino/$nino"

}