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

import connectors.BusinessDetailsConnector
import javax.inject.{Inject, Singleton}
import models.ErrorModel
import models.frontend.FESuccessResponse
import play.api.Logger
import play.api.http.Status._
import play.api.mvc.Request
import uk.gov.hmrc.http.HeaderCarrier
import utils.Implicits._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubscriptionStatusService @Inject()(businessDetailsConnector: BusinessDetailsConnector) {
  /*
  * This method will check to see if a user with the supplied nino has an MTD IT SA subscription
  * if will return OK with the reference if it is found, or OK with {} if it is not found
  * it will return all other errors as they were
  **/
  def checkMtditsaSubscription(nino: String)
                              (implicit hc: HeaderCarrier, ec: ExecutionContext, request: Request[_]): Future[Either[ErrorModel, Option[FESuccessResponse]]] = {
    Logger.debug(s"Request: NINO=$nino")
    businessDetailsConnector.getBusinessDetails(nino).flatMap {
      // if the subscription is not found, convert it to OK with {}
      case Left(error: ErrorModel) if error.status == NOT_FOUND =>
        Logger.debug(s"SubscriptionStatusService.checkMtditsaEnrolment - No mtditsa enrolment for nino=$nino")
        Right(None)
      case Right(x) =>
        Logger.debug(s"SubscriptionStatusService.checkMtditsaEnrolment - Client is already enrolled with mtditsa, ref=${x.mtdbsa}")
        Right(Some(FESuccessResponse(x.mtdbsa)))
      case Left(x) => Left(x)
    }
  }
}

