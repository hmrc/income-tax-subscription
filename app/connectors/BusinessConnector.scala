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
import javax.inject.{Inject, Singleton}
import models.monitoring.BusinessSubscribeFailureAudit
import models.subscription.incomesource.BusinessIncomeModel
import play.api.Logger
import play.api.http.Status._
import play.api.libs.json.{JsObject, JsSuccess}
import play.api.mvc.Request
import services.monitoring.AuditService
import uk.gov.hmrc.http.logging.Authorization
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BusinessConnector @Inject()(http: HttpClient,
                                  appConfig: MicroserviceAppConfig,
                                  auditService: AuditService)(implicit ec: ExecutionContext) extends RawResponseReads {

  def businessSubscribe(nino: String, businessIncomeModel: BusinessIncomeModel, arn: Option[String])
                       (implicit hc: HeaderCarrier, request: Request[_]): Future[String] = {

    val headerCarrier: HeaderCarrier = hc
      .copy(authorization = Some(Authorization(appConfig.desAuthorisationToken)))
      .withExtraHeaders(appConfig.desEnvironmentHeader)

    val requestBody: JsObject = BusinessIncomeModel.writeToDes(businessIncomeModel)

    http.POST(appConfig.businessSubscribeUrl(nino), requestBody)(implicitly, httpReads, headerCarrier, implicitly) map { response =>
      response.status match {
        case OK =>
          (response.json \ "mtditId").validate[String] match {
            case JsSuccess(mtditId, _) =>
              Logger.info(s"[BusinessConnector][businessSubscribe] - Successful business subscribed for $nino")
              mtditId
            case _ => throw new InternalServerException("[BusinessConnector][businessSubscribe] MTDITID missing from DES response")
          }
        case status =>
          auditService.audit(BusinessSubscribeFailureAudit(nino, arn, requestBody, response.body))(headerCarrier, implicitly, implicitly)
          throw new InternalServerException(s"[BusinessConnector][businessSubscribe] - Failed business subscription for $nino, status: $status")
      }
    }
  }

}