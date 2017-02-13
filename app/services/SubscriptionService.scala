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

package services

import javax.inject.Inject

import connectors.SubscriptionConnector
import models.subscription.business.{BusinessDetailsModel, BusinessSubscriptionRequestModel, BusinessSubscriptionSuccessResponseModel}
import models.ErrorModel
import models.frontend.FERequest
import models.subscription.property.PropertySubscriptionResponseModel
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SubscriptionService @Inject() (subscriptionConnector: SubscriptionConnector){

  def propertySubscribe(request: FERequest)(implicit hc: HeaderCarrier): Future[Either[ErrorModel, PropertySubscriptionResponseModel]] =
    subscriptionConnector.propertySubscribe(request.nino)

  def businessSubscribe(request: FERequest)(implicit hc: HeaderCarrier): Future[Either[ErrorModel, BusinessSubscriptionSuccessResponseModel]] = {

    val businessDetails = BusinessDetailsModel(
      accountingPeriodStartDate = request.accountingPeriodStart.get.toDesDateFormat,
      accountingPeriodEndDate = request.accountingPeriodEnd.get.toDesDateFormat,
      cashOrAccruals = request.cashOrAccruals.get,
      tradingName = request.tradingName.get
    )

    subscriptionConnector.businessSubscribe(request.nino, BusinessSubscriptionRequestModel(List(businessDetails)))
  }
}
