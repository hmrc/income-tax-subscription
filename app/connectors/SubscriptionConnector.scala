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

import audit.Logging._
import audit.{Logging, LoggingConfig}
import config.AppConfig
import connectors.SubscriptionConnector._
import connectors.utils.ConnectorUtils
import models.ErrorModel
import models.subscription.business._
import models.subscription.property.{PropertySubscriptionFailureModel, PropertySubscriptionResponseModel}
import play.api.http.Status._
import play.api.libs.json.{JsValue, Writes}
import uk.gov.hmrc.http.logging.Authorization
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

class SubscriptionConnector @Inject()(applicationConfig: AppConfig,
                                      httpClient: HttpClient,
                                      logging: Logging
                                     ) extends RawResponseReads {

  val businessSubscribeUrl: String => String = nino => applicationConfig.desURL + businessSubscribeUri(nino)
  val propertySubscribeUrl: String => String = nino => applicationConfig.desURL + propertySubscribeUri(nino)

  lazy val urlHeaderAuthorization: String = s"Bearer ${applicationConfig.desToken}"

  def createHeaderCarrierPost(headerCarrier: HeaderCarrier): HeaderCarrier =
    headerCarrier.copy(authorization = Some(Authorization(urlHeaderAuthorization)))
      .withExtraHeaders("Environment" -> applicationConfig.desEnvironment, "Content-Type" -> "application/json")

  def businessSubscribe(nino: String, businessSubscriptionPayload: BusinessSubscriptionRequestModel, arn: Option[String])
                       (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[BusinessConnectorUtil.Response] = {
    import BusinessConnectorUtil._
    import SubscriptionConnector._
    implicit val loggingConfig = SubscriptionConnector.businessSubscribeLoggingConfig
    lazy val requestDetails: Map[String, String] = Map("nino" -> nino, "arn" -> arn.fold("-")(identity),
      "subscribe" -> (businessSubscriptionPayload: JsValue).toString)
    val updatedHc = createHeaderCarrierPost(hc)

    logging.debug(s"Request:\n$requestDetails\n\nHeader Carrier:\n$updatedHc")
    httpClient.POST[BusinessSubscriptionRequestModel, HttpResponse](businessSubscribeUrl(nino), businessSubscriptionPayload)(
      implicitly[Writes[BusinessSubscriptionRequestModel]], implicitly[HttpReads[HttpResponse]], createHeaderCarrierPost(hc), ec
    ).map { response =>
      response.status match {
        case OK =>
          logging.info(s"Business subscription responded with an OK")
          parseSuccess(response.body)
        case status =>
          logging.audit(
            transactionName = auditBusinessSubscribeName,
            detail = requestDetails + ("response" -> response.body),
            auditType = auditBusinessSubscribeName + "-" + eventTypeUnexpectedError
          )(updatedHc)

          val parseResponse@Left(ErrorModel(_, optCode, message)) = parseFailure(status, response.body)
          val code: String = optCode.getOrElse("N/A")
          logging.warn(s"Business subscription responded with an error, status=$status code=$code message=$message")

          parseResponse
      }
    }
  }

  def propertySubscribe(nino: String, arn: Option[String])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[PropertyConnectorUtil.Response] = {
    import PropertyConnectorUtil._
    import SubscriptionConnector._
    implicit val loggingConfig = SubscriptionConnector.propertySubscribeLoggingConfig
    lazy val requestDetails: Map[String, String] = Map("nino" -> nino, "arn" -> arn.fold("-")(identity))
    val updatedHc = createHeaderCarrierPost(hc)
    logging.debug(s"Request:\n$requestDetails\n\nHeader Carrier:\n$updatedHc")
    httpClient.POST[JsValue, HttpResponse](propertySubscribeUrl(nino), "{}": JsValue)(implicitly[Writes[JsValue]],
      implicitly[HttpReads[HttpResponse]], updatedHc, ec).map { response =>

      response.status match {
        case OK =>
          logging.info(s"Property subscription responded with an OK")
          parseSuccess(response.body)
        case status =>

          logging.audit(
            transactionName = auditPropertySubscribeName,
            detail = requestDetails + ("response" -> response.body),
            auditType = auditPropertySubscribeName + "-" + eventTypeUnexpectedError
          )(updatedHc)

          val parseResponse@Left(ErrorModel(_, optCode, message)) = parseFailure(status, response.body)
          val code: String = optCode.getOrElse("N/A")
          logging.warn(s"Property subscription responded with an error, status=$status code=$code message=$message")

          parseResponse
      }
    }
  }
}

object SubscriptionConnector {

  import _root_.utils.Implicits.optionUtl

  val auditBusinessSubscribeName = "business-subscribe-api-10"
  val businessSubscribeLoggingConfig: Option[LoggingConfig] = LoggingConfig(heading = "SubscriptionConnector.businessSubscribe")

  val auditPropertySubscribeName = "property-subscribe-api-35"
  val propertySubscribeLoggingConfig: Option[LoggingConfig] = LoggingConfig(heading = "SubscriptionConnector.propertySubscribe")

  def businessSubscribeUri(nino: String): String = s"/income-tax-self-assessment/nino/$nino/business"

  def propertySubscribeUri(nino: String): String = s"/income-tax-self-assessment/nino/$nino/properties"
}

object PropertyConnectorUtil extends ConnectorUtils[PropertySubscriptionFailureModel, PropertySubscriptionResponseModel]

object BusinessConnectorUtil extends ConnectorUtils[BusinessSubscriptionErrorResponseModel, BusinessSubscriptionSuccessResponseModel]
