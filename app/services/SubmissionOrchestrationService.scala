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

import config.AppConfig
import connectors.{BusinessConnector, PropertyConnector, RegistrationConnector}
import controllers.ITSASessionKeys
import javax.inject.{Inject, Singleton}
import models.ErrorModel
import models.monitoring.{RegistrationRequestAudit, RegistrationSuccessAudit}
import models.subscription.incomesource.SignUpRequest
import play.api.libs.json.{Json, OFormat}
import play.api.mvc.Request
import services.SubmissionOrchestrationService._
import services.monitoring.AuditService
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubmissionOrchestrationService @Inject()(registrationConnector: RegistrationConnector,
                                               businessConnector: BusinessConnector,
                                               propertyConnector: PropertyConnector,
                                               auditService: AuditService,
                                               appConfig: AppConfig)(implicit ec: ExecutionContext) {

  def submit(signUpRequest: SignUpRequest)
            (implicit hc: HeaderCarrier, request: Request[_]): Future[Either[ErrorModel, SuccessfulSubmission]] = {

    auditService.audit(RegistrationRequestAudit(signUpRequest, appConfig.desAuthorisationToken))

    registrationConnector.register(signUpRequest.nino, signUpRequest.isAgent) flatMap {
      case Left(left) => Future.successful(Left(left))
      case _ => signUpIncomeSources(signUpRequest).map(id => Right(SuccessfulSubmission(id)))
    } map {
      case Right(success) =>
        val path: String = request.headers.get(ITSASessionKeys.RequestURI).getOrElse("-")
        auditService.audit(RegistrationSuccessAudit(signUpRequest, success.mtditId, path))
        Right(success)
      case left => left
    }

  }

  private def signUpIncomeSources(signUpRequest: SignUpRequest)(implicit hc: HeaderCarrier, request: Request[_]): Future[String] = {
    for {
      businessResponse <- signUpBusiness(signUpRequest)
      propertyResponse <- signUpProperty(signUpRequest)
    } yield {
      businessResponse orElse propertyResponse match {
        case Some(id) => id
        case None => throw new InternalServerException("[SubmissionOrchestrationService][submit] - No mtditid response")
      }
    }
  }

  private def signUpBusiness(signUpRequest: SignUpRequest)(implicit hc: HeaderCarrier, request: Request[_]): Future[Option[String]] = {
    signUpRequest.businessIncome match {
      case Some(business) => businessConnector.businessSubscribe(signUpRequest.nino, business, signUpRequest.arn).map(Some.apply)
      case None => Future.successful(None)
    }
  }

  private def signUpProperty(signUpRequest: SignUpRequest)(implicit hc: HeaderCarrier, request: Request[_]): Future[Option[String]] = {
    signUpRequest.propertyIncome match {
      case Some(property) => propertyConnector.propertySubscribe(signUpRequest.nino, property, signUpRequest.arn).map(Some.apply)
      case None => Future.successful(None)
    }
  }

}

object SubmissionOrchestrationService {

  case object NoSubmissionNeeded

  case class SuccessfulSubmission(mtditId: String)

  object SuccessfulSubmission {
    implicit val format: OFormat[SuccessfulSubmission] = Json.format[SuccessfulSubmission]
  }

}