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

import connectors.{BusinessConnector, PropertyConnector, RegistrationConnector}
import javax.inject.{Inject, Singleton}
import models.subscription.incomesource.SignUpRequest
import play.api.libs.json.Json
import services.SubmissionOrchestrationService.{NoSubmissionNeeded, _}
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubmissionOrchestrationService @Inject()(registrationConnector: RegistrationConnector,
                                               businessConnector: BusinessConnector,
                                               propertyConnector: PropertyConnector
                                              )(implicit ec: ExecutionContext) {


  def submit(signUpRequest: SignUpRequest)(implicit hc: HeaderCarrier): Future[SuccessfulSubmission] =
    for {
      _ <- registrationConnector.register(signUpRequest.nino, signUpRequest.isAgent)
      businessResponse <- signUpRequest.businessIncome match {
        case Some(business) => businessConnector.businessSubscribe(signUpRequest.nino, business)
        case None => Future.successful(NoSubmissionNeeded)
      }
      propertyResponse <- signUpRequest.propertyIncome match {
        case Some(property) => propertyConnector.propertySubscribe(signUpRequest.nino, property)
        case None => Future.successful(NoSubmissionNeeded)
      }
    } yield
      (businessResponse, propertyResponse) match {
        case (businessSuccess: String, _) =>
          SuccessfulSubmission(businessSuccess)
        case (_, propertySuccess: String) =>
          SuccessfulSubmission(propertySuccess)
        case _ =>
          throw new InternalServerException("Unexpected error - income type missing")
      }
}

object SubmissionOrchestrationService {

  case object NoSubmissionNeeded

  case class SuccessfulSubmission(mtditId: String)

  object SuccessfulSubmission {
    implicit val format = Json.format[SuccessfulSubmission]
  }

}