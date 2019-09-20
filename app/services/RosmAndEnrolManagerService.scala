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

package services

import javax.inject.{Inject, Singleton}
import config.AppConfig
import models.ErrorModel
import models.frontend._

import models.monitoring.rosmAndEnrol.rosmAndEnrolModel
import models.monitoring.rosmAndEnrolSuccess.rosmAndEnrolSuccessModel
import models.subscription.business.BusinessSubscriptionSuccessResponseModel
import models.subscription.property.PropertySubscriptionResponseModel
import play.api.http.Status._
import play.api.mvc.Request
import services.monitoring.AuditService
import utils.Implicits._

import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.http.HeaderCarrier

@Singleton
class RosmAndEnrolManagerService @Inject()
(
  appConfig: AppConfig,
  auditService: AuditService,
  registrationService: RegistrationService,
  subscriptionService: SubscriptionService
) {

  lazy val urlHeaderAuthorization: String = s"Bearer ${appConfig.desToken}"

  val pathKey = "path"

  def rosmAndEnrol(feRequest: FERequest, path: String)
                  (implicit hc: HeaderCarrier, ec: ExecutionContext, request: Request[_]): Future[Either[ErrorModel, FESuccessResponse]] = {
    auditService.audit(rosmAndEnrolModel(feRequest, urlHeaderAuthorization))

    orchestrateROSM(feRequest) flatMap {
      case Right(rosmSuccess) =>
        auditService.audit(rosmAndEnrolSuccessModel(feRequest, rosmSuccess, path))
        Future.successful(FESuccessResponse(rosmSuccess.mtditId))
      case Left(rosmFailure) =>
        Future.successful(rosmFailure)
    }
  }

  def orchestrateROSM(feRequest: FERequest)
                     (implicit hc: HeaderCarrier, ec: ExecutionContext,request: Request[_]): Future[Either[ErrorModel, FESuccessResponse]] = {
    registrationService.register(feRequest.isAgent, feRequest.nino) flatMap {
      case Right(success) =>
        for {
          businessResult <- businessSubscription(feRequest)
          propertyResult <- propertySubscription(feRequest)
        } yield (businessResult, propertyResult) match {
          case (Some(Left(err)), _) => err
          case (_, Some(Left(err))) => err
          case (Some(Right(x)), _) => FESuccessResponse(x.mtditId) // As long as there's no error reported then
          case (_, Some(Right(x))) => FESuccessResponse(x.mtditId) // We only need the response of one of the calls
          case (_, _) => ErrorModel(INTERNAL_SERVER_ERROR, "Unexpected Error") // this error is impossible but included for exhaustive match
        }
      case Left(failure) => Future.successful(failure)
    }
  }


  private def businessSubscription(feRequest: FERequest)
                                  (implicit hc: HeaderCarrier,
                                   ec: ExecutionContext,request: Request[_]): Future[Option[Either[ErrorModel, BusinessSubscriptionSuccessResponseModel]]] = {
    feRequest.incomeSource match {
      case Both | Business => subscriptionService.businessSubscribe(feRequest) map {
        case Right(success) => Some(success)
        case Left(failure) => Some(failure)
      }
      case _ => None
    }
  }

  private def propertySubscription(feRequest: FERequest)
                                  (implicit hc: HeaderCarrier,
                                   ec: ExecutionContext,
                                   request: Request[_]): Future[Option[Either[ErrorModel, PropertySubscriptionResponseModel]]] = {
    feRequest.incomeSource match {
      case Both | Property => subscriptionService.propertySubscribe(feRequest) map {
        case Right(success) => Some(success)
        case Left(failure) => Some(failure)
      }
      case _ => None
    }
  }
}
