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

import audit.Logging
import config.AppConfig
import models.ErrorModel
import models.frontend._
import models.subscription.business.BusinessSubscriptionSuccessResponseModel
import models.subscription.property.PropertySubscriptionResponseModel
import play.api.http.Status._
import utils.Implicits._

import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.http.HeaderCarrier

@Singleton
class RosmAndEnrolManagerService @Inject()
(
  appConfig: AppConfig,
  logging: Logging,
  registrationService: RegistrationService,
  subscriptionService: SubscriptionService
) {

  lazy val urlHeaderAuthorization: String = s"Bearer ${appConfig.desToken}"

  val pathKey = "path"

  val feRequestToAuditMap: FERequest => Map[String, String] = feRequest =>
    Map(
      "nino" -> feRequest.nino,
      "isAgent" -> feRequest.isAgent.toString,
      "arn" -> feRequest.arn.fold("-")(identity),
      "sourceOfIncome" -> feRequest.incomeSource.toString,
      "acccountingPeriodStartDate" -> feRequest.accountingPeriodStart.fold("-")(x => x.toDesDateFormat),
      "acccountingPeriodEndDate" -> feRequest.accountingPeriodEnd.fold("-")(x => x.toDesDateFormat),
      "tradingName" -> feRequest.tradingName.fold("-")(identity),
      "cashOrAccruals" -> feRequest.cashOrAccruals.fold("-")(x => x.toLowerCase),
      "Authorization" -> urlHeaderAuthorization
    )

  val auditResponseMap: (FERequest, FESuccessResponse) => Map[String, String] = (feRequest, response) =>
    Map(
      "nino" -> feRequest.nino,
      "arn" -> feRequest.arn.fold("-")(identity),
      "mtdItsaReferenceNumber" -> response.mtditId.get
    )

  def rosmAndEnrol(request: FERequest, path: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[ErrorModel, FESuccessResponse]] = {
    logging.audit(
      Logging.AuditSubscribeRequest.transactionName,
      feRequestToAuditMap(request) + (pathKey -> path),
      Logging.AuditSubscribeRequest.auditType
    )(hc)

    val result: Future[Either[ErrorModel, FESuccessResponse]] = orchestrateROSM(request).flatMap {
      case Right(rosmSuccess) => Future.successful(FESuccessResponse(rosmSuccess.mtditId))
      case Left(rosmFailure) => Future.successful(rosmFailure)
    }
    result.map {
      case Right(rosmSuccess@FESuccessResponse(_)) =>
        logging.audit(
          Logging.AuditReferenceNumber.transactionName,
          auditResponseMap(request, rosmSuccess) + (pathKey -> path),
          Logging.AuditReferenceNumber.auditType
        )(hc)

        rosmSuccess
      case x => x
    }
  }

  def orchestrateROSM(request: FERequest)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[ErrorModel, FESuccessResponse]] = {
    registrationService.register(request.isAgent, request.nino) flatMap {
      case Right(success) =>
        for {
          businessResult <- businessSubscription(request)
          propertyResult <- propertySubscription(request)
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


  private def businessSubscription(request: FERequest)(implicit hc: HeaderCarrier, ec: ExecutionContext)
  : Future[Option[Either[ErrorModel, BusinessSubscriptionSuccessResponseModel]]] = {
    request.incomeSource match {
      case Both | Business => subscriptionService.businessSubscribe(request) map {
        case Right(success) => Some(success)
        case Left(failure) => Some(failure)
      }
      case _ => None
    }
  }

  private def propertySubscription(request: FERequest)(implicit hc: HeaderCarrier, ec: ExecutionContext)
  : Future[Option[Either[ErrorModel, PropertySubscriptionResponseModel]]] = {
    request.incomeSource match {
      case Both | Property => subscriptionService.propertySubscribe(request) map {
        case Right(success) => Some(success)
        case Left(failure) => Some(failure)
      }
      case _ => None
    }
  }
}
