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

package controllers

import models.subscription.business.{CreateIncomeSourceErrorModel, CreateIncomeSourceSuccessModel}
import play.api.http.Status._
import play.api.test.FakeRequest
import play.api.test.Helpers.stubControllerComponents
import services.mocks.{MockAuthService, MockIncomeSourcesConnector}
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier, Enrolments}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import utils.MaterializerSupport
import utils.TestConstants.{testCreateIncomeSubmissionJson, testCreateIncomeSubmissionModel}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class BusinessIncomeSourcesControllerSpec extends UnitSpec with MockAuthService with MockIncomeSourcesConnector with MaterializerSupport {
  lazy val mockCC = stubControllerComponents()


  object TestController extends BusinessIncomeSourcesController(mockAuthService, mockIncomeSourcesConnector, mockCC)

  "Income Sources Controller" when {
    "Income Sources are submitted" should {
      "return a 204 response" in {
        val fakeRequest = FakeRequest().withBody(testCreateIncomeSubmissionJson)

        implicit val hc: HeaderCarrier = HeaderCarrier()

        mockRetrievalSuccess(Enrolments(Set(Enrolment("HMRC-AS-AGENT", Seq(EnrolmentIdentifier("AgentReferenceNumber", "1223456")), "Activated"))))
        createBusinessIncome(Some("1223456"),"XAIT0000006", testCreateIncomeSubmissionModel)(Future.successful(Right(CreateIncomeSourceSuccessModel())))

        val result = await(TestController.createIncomeSource("XAIT0000006")(fakeRequest))
        status(result) shouldBe NO_CONTENT
      }
    }
  }

  "return InternalServerError" when {
    "Income Sources are submitted" should {
      "return an error" in {
        val fakeRequest = FakeRequest().withBody(testCreateIncomeSubmissionJson)

        implicit val hc: HeaderCarrier = HeaderCarrier()

        mockRetrievalSuccess(Enrolments(Set(Enrolment("HMRC-AS-AGENT", Seq(EnrolmentIdentifier("AgentReferenceNumber", "1223456")), "Activated"))))
        createBusinessIncome(Some("1223456"),"XAIT0000006", testCreateIncomeSubmissionModel)(Future.successful(Left(CreateIncomeSourceErrorModel(INTERNAL_SERVER_ERROR, "error body"))))

        val result = await(TestController.createIncomeSource("XAIT0000006")(fakeRequest))
        status(result) shouldBe INTERNAL_SERVER_ERROR
        bodyOf(result) shouldBe "Business Income Source Failure"
      }
    }
  }

}
