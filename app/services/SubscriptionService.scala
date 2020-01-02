/*
 * Copyright 2020 HM Revenue & Customs
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

package services

import connectors.deprecated.SubscriptionConnector
import javax.inject.{Inject, Singleton}
import models.ErrorModel
import models.frontend.FERequest
import models.subscription.business.{BusinessDetailsModel, BusinessSubscriptionRequestModel, BusinessSubscriptionSuccessResponseModel}
import models.subscription.property.PropertySubscriptionResponseModel
import play.api.mvc.Request
import uk.gov.hmrc.http.HeaderCarrier
import utils.Implicits._
import utils.{Logging, LoggingConfig}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class SubscriptionService @Inject()(subscriptionConnector: SubscriptionConnector,
                                    logging: Logging) {

  def propertySubscribe(feRequest: FERequest)(
    implicit hc: HeaderCarrier, request: Request[_]): Future[Either[ErrorModel, PropertySubscriptionResponseModel]] = {
    implicit val subscribeLoggingConfig: Option[LoggingConfig] = SubscriptionService.propertySubscribeLoggingConfig
    logging.debug(s"Request: $feRequest")
    subscriptionConnector.propertySubscribe(feRequest.nino, feRequest.arn)
  }

  def businessSubscribe(feRequest: FERequest)(implicit hc: HeaderCarrier, request: Request[_]):
  Future[Either[ErrorModel, BusinessSubscriptionSuccessResponseModel]] = {
    implicit val businessSubscribeLoggingConfig: Option[LoggingConfig] = SubscriptionService.businessSubscribeLoggingConfig
    logging.debug(s"Request: $feRequest")
    val businessDetails = BusinessDetailsModel(
      accountingPeriodStartDate = feRequest.accountingPeriodStart.get.toDesDateFormat,
      accountingPeriodEndDate = feRequest.accountingPeriodEnd.get.toDesDateFormat,
      cashOrAccruals = feRequest.cashOrAccruals.get,
      tradingName = feRequest.tradingName.get
    )
    subscriptionConnector.businessSubscribe(feRequest.nino, BusinessSubscriptionRequestModel(List(businessDetails)), feRequest.arn)
  }

}

object SubscriptionService {
  val propertySubscribeLoggingConfig: Option[LoggingConfig] = LoggingConfig(heading = "SubscriptionService.propertySubscribe")
  val businessSubscribeLoggingConfig: Option[LoggingConfig] = LoggingConfig(heading = "SubscriptionService.businessSubscribe")
}
