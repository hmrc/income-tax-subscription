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

import com.google.inject.{Inject, Singleton}
import config.AppConfig
import models.{PropertySubscriptionFailureModel, PropertySubscriptionRequestModel, PropertySubscriptionResponseModel}
import play.api.libs.json.{JsValue, Json, Writes}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http._
import uk.gov.hmrc.play.http.logging.Authorization
import uk.gov.hmrc.play.http.ws.WSHttp

import scala.concurrent.{ExecutionContext, Future}
import play.api.http.Status._

@Singleton
class SubscriptionETMPConnectorImpl @Inject()(http: WSHttp, applicationConfig: AppConfig) extends SubscriptionETMPConnector
  with ServicesConfig with RawResponseReads {

  lazy val serviceUrl = applicationConfig.desURL
  lazy val environment = applicationConfig.desEnvironment
  lazy val token = applicationConfig.desToken

  def subscribePropertyEtmp(nino: String, subscribeRequest: PropertySubscriptionRequestModel)
                           (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse] = {
    val requestUrl = s"$serviceUrl/income-tax-self-assessment/nino/$nino/properties"
    val desHeaders = hc.copy(authorization = Some(Authorization(s"Bearer $token"))).withExtraHeaders("Environment" -> environment)
    val request = http.POST[JsValue, HttpResponse](requestUrl, Json.toJson(subscribeRequest))(implicitly[Writes[JsValue]], HttpReads.readRaw, desHeaders)
    request.map {

      case response => response.status match {
        case OK => {Json.fromJson[PropertySubscriptionResponseModel](response.json)}
        case BAD_REQUEST => {Json.fromJson[PropertySubscriptionFailureModel](response.json)}
        case NOT_FOUND => {Json.fromJson[PropertySubscriptionFailureModel](response.json)}
        case INTERNAL_SERVER_ERROR => {Json.fromJson[PropertySubscriptionFailureModel](response.json)}
        case SERVICE_UNAVAILABLE => {Json.fromJson[PropertySubscriptionFailureModel](response.json)}
      }
    }
  }
}

trait SubscriptionETMPConnector {
  val serviceUrl: String
  val environment: String
  val token: String

  def subscribePropertyEtmp(nino: String, subscribeRequest: PropertySubscriptionRequestModel)
                           (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[HttpResponse]
}
