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

import connectors.{BusinessConnector, PropertyConnector, RegistrationConnector}
import javax.inject.{Inject, Singleton}
import models.subscription.incomesource.SignUpRequest
import services.SubmissionOrchestrationService._
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubmissionOrchestrationService @Inject()(registrationConnector: RegistrationConnector,
                                               businessConnector: BusinessConnector,
                                               propertyConnector: PropertyConnector
                                              )(implicit ec: ExecutionContext) {

  def submit(signUpRequest: SignUpRequest)(implicit hc: HeaderCarrier): Future[SuccessfulSubmission.type] =
    for {
      _ <- registrationConnector.register(signUpRequest.nino, signUpRequest.isAgent)
      _ <- signUpRequest.businessIncome match {
        case Some(business) => businessConnector.businessSubscribe(signUpRequest.nino, business)
        case None => Future.successful(NoSubmissionNeeded)
      }
      _ <- signUpRequest.propertyIncome match {
        case Some(_) => propertyConnector.propertySubscribe(signUpRequest.nino)
        case None => Future.successful(NoSubmissionNeeded)
      }
    } yield SuccessfulSubmission

}

object SubmissionOrchestrationService {

  case object NoSubmissionNeeded

  case object SuccessfulSubmission

}