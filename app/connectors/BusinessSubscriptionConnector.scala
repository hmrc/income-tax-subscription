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

import audit.Logging
import connectors.utils.ConnectorUtils
import models.subscription.business._
import play.api.Configuration
import play.api.http.Status._
import play.api.libs.json.Writes
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.http.logging.Authorization

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class BusinessSubscriptionConnector @Inject()
(
  config: Configuration,
  logging: Logging,
  httpPost: HttpPost,
  httpGet: HttpGet
) extends ServicesConfig with RawResponseReads with ConnectorUtils[BusinessSubscriptionErrorResponseModel, BusinessSubscriptionSuccessResponseModel] {

  lazy val urlHeaderEnvironment: String = config.getString("microservice.services.des.environment").fold("")(x => x)
  lazy val urlHeaderAuthorization: String = s"Bearer ${config.getString("microservice.services.des.authorization-token").fold("")(x => x)}"
  lazy val desServiceUrl: String = baseUrl("des")

  val businessSubscribeUrl: String => String = nino => s"$desServiceUrl/income-tax-self-assessment/nino/$nino/business"

  def createHeaderCarrierPost(headerCarrier: HeaderCarrier): HeaderCarrier =
    headerCarrier.withExtraHeaders("Environment" -> urlHeaderEnvironment, "Content-Type" -> "application/json")
      .copy(authorization = Some(Authorization(urlHeaderAuthorization)))


  def businessSubscribe(nino: String, businessSubscriptionPayload: BusinessSubscriptionRequestModel)
                       (implicit hc: HeaderCarrier): Future[Response] = {
    httpPost.POST[BusinessSubscriptionRequestModel, HttpResponse](businessSubscribeUrl(nino), businessSubscriptionPayload)(
      implicitly[Writes[BusinessSubscriptionRequestModel]], implicitly[HttpReads[HttpResponse]], createHeaderCarrierPost(hc)
    ).map { response =>
      response.status match {
        case OK => parseSuccess(response.body)
        case x => parseFailure(x, response.body)
      }
    }
  }
}