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
import models.monitoring.{CompletedSignUpAudit, SignUpCompleteAudit}
import models.subscription.{BusinessSubscriptionDetailsModel, CreateIncomeSourcesModel}
import parsers.CreateIncomeSourceParser._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Request
import services.monitoring.AuditService
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, HeaderNames, HttpClient, HttpReads}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateIncomeSourcesConnector @Inject()(http: HttpClient,
                                             appConfig: AppConfig,
                                             auditService: AuditService)(implicit ec: ExecutionContext) {

  private def businessIncomeUrl(mtdbsaRef: String): String = {
    s"${appConfig.desURL}/income-tax/income-sources/mtdbsa/$mtdbsaRef/ITSA/business"
  }

  def createBusinessIncomeSources(agentReferenceNumber: Option[String],
                                  mtdbsaRef: String,
                                  createIncomeSources: CreateIncomeSourcesModel)
                                 (implicit hc: HeaderCarrier, request: Request[_]): Future[PostIncomeSourceResponse] = {

    val headerCarrier: HeaderCarrier = hc
      .copy(authorization = Some(Authorization(appConfig.desAuthorisationToken)))
      .withExtraHeaders(appConfig.desEnvironmentHeader)

    val desHeaders: Seq[(String, String)] = Seq(
      HeaderNames.authorisation -> appConfig.desAuthorisationToken,
      appConfig.desEnvironmentHeader
    )

    http.POST[JsValue, PostIncomeSourceResponse](businessIncomeUrl(mtdbsaRef), Json.toJson(createIncomeSources), headers = desHeaders)(
      implicitly,
      implicitly[HttpReads[PostIncomeSourceResponse]],
      headerCarrier,
      implicitly
    ) map {
      case Left(error) =>
        Left(error)
      case Right(value) =>
        auditService.extendedAudit(CompletedSignUpAudit(agentReferenceNumber, createIncomeSources, appConfig.desAuthorisationToken))
        Right(value)
    }

  }

  def createBusinessIncome(agentReferenceNumber: Option[String],
                           mtdbsaRef: String,
                           incomeSourceRequest: BusinessSubscriptionDetailsModel)
                          (implicit hc: HeaderCarrier,
                           request: Request[_]): Future[PostIncomeSourceResponse] = {

    auditService.extendedAudit(SignUpCompleteAudit(agentReferenceNumber, incomeSourceRequest, appConfig.desAuthorisationToken))

    val headerCarrier: HeaderCarrier = hc
      .copy(authorization = Some(Authorization(appConfig.desAuthorisationToken)))
      .withExtraHeaders(appConfig.desEnvironmentHeader)

    val desHeaders: Seq[(String, String)] = Seq(
      HeaderNames.authorisation -> appConfig.desAuthorisationToken,
      appConfig.desEnvironmentHeader
    )

    http.POST[JsValue, PostIncomeSourceResponse](businessIncomeUrl(mtdbsaRef), Json.toJson(incomeSourceRequest), headers = desHeaders)(implicitly,
      implicitly[HttpReads[PostIncomeSourceResponse]], headerCarrier, implicitly)
  }
}
