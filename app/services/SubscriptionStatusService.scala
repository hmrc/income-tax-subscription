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

import javax.inject.{Inject, Singleton}

import audit.{Logging, LoggingConfig}
import connectors.BusinessDetailsConnector
import models.ErrorModel
import models.frontend.FESuccessResponse
import play.api.http.Status._
import uk.gov.hmrc.play.http.HeaderCarrier
import utils.Implicits._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubscriptionStatusService @Inject()(businessDetailsConnector: BusinessDetailsConnector,
                                          logging: Logging) {

  def checkMtditsaEnrolment(nino: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[ErrorModel, Option[FESuccessResponse]]] = {
    logging.debug(s"Request: NINO=$nino")
    implicit val checkAlreadyEnrolledLoggingConfig = SubscriptionStatusService.checkMtditsaEnrolmentLoggingConfig
    businessDetailsConnector.getBusinessDetails(nino).flatMap {
      case Left(error: ErrorModel) if error.status == NOT_FOUND =>
        logging.debug(s"No mtditsa enrolment for nino=$nino")
        Right(None)
      case Right(x) =>
        logging.debug(s"Client is already enrolled with mtditsa, ref=${x.mtdbsa}")
        Right(Some(FESuccessResponse(x.mtdbsa)))
      case Left(x) => Left(x)
    }
  }

}

object SubscriptionStatusService {
  val checkMtditsaEnrolmentLoggingConfig: Option[LoggingConfig] = LoggingConfig(heading = "SubscriptionStatusService.checkMtditsaEnrolment")
}

