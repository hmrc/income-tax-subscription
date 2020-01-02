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

package services.mocks

import models.ErrorModel
import models.frontend.FESuccessResponse
import models.subscription.incomesource.SignUpRequest
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import services.SubmissionOrchestrationService
import services.SubmissionOrchestrationService.SuccessfulSubmission
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

trait MockSubmissionOrchestrationService extends MockitoSugar {
  val mockSubmissionOrchestrationService: SubmissionOrchestrationService = mock[SubmissionOrchestrationService]

  def mockSubmit(request: SignUpRequest)(response: Future[SuccessfulSubmission]): Unit = {
    when(mockSubmissionOrchestrationService.submit(
      ArgumentMatchers.eq(request)
    )(
      ArgumentMatchers.any[HeaderCarrier]
    ))
      .thenReturn(response)
  }
}
