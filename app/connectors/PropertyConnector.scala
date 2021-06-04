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

import config.MicroserviceAppConfig
import models.monitoring.PropertySubscribeFailureAudit
import models.subscription.incomesource.PropertyIncomeModel
import play.api.Logger
import play.api.http.Status._
import play.api.libs.json.{JsObject, JsSuccess}
import play.api.mvc.Request
import services.monitoring.AuditService
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, HeaderNames, HttpClient, InternalServerException}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PropertyConnector @Inject()(http: HttpClient,
                                  appConfig: MicroserviceAppConfig,
                                  auditService: AuditService)(implicit ec: ExecutionContext) extends RawResponseReads {

  val logger: Logger = Logger(this.getClass)

  def propertySubscribe(nino: String, propertyIncomeModel: PropertyIncomeModel, arn: Option[String])
                       (implicit hc: HeaderCarrier, request: Request[_]): Future[String] = {

    val headerCarrier = hc
      .copy(authorization = Some(Authorization(appConfig.desAuthorisationToken)))
      .withExtraHeaders(appConfig.desEnvironmentHeader)

    val desHeaders: Seq[(String, String)] = Seq(
      HeaderNames.authorisation -> appConfig.desAuthorisationToken,
      appConfig.desEnvironmentHeader
    )

    val requestBody: JsObject = PropertyIncomeModel.writeToDes(propertyIncomeModel)

    http.POST(appConfig.propertySubscribeUrl(nino), requestBody, headers = desHeaders)(implicitly, httpReads, headerCarrier, implicitly) map { response =>
      response.status match {
        case OK =>
          (response.json \ "mtditId").validate[String] match {
            case JsSuccess(mtditId, _) =>
              logger.info(s"[PropertyConnector][propertySubscribe] - Successful property subscribed for $nino")
              mtditId
            case _ => throw new InternalServerException("[PropertyConnector][propertySubscribe] MTDITID missing from DES response")
          }
        case status =>
          auditService.audit(PropertySubscribeFailureAudit(nino, arn, requestBody, response.body))(headerCarrier, implicitly, implicitly)
          throw new InternalServerException(s"[PropertyConnector][propertySubscribe] - Failed property subscription for $nino, status: $status")
      }
    }
  }
}
