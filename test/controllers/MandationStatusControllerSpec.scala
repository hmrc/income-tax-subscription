/*
 * Copyright 2023 HM Revenue & Customs
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

import common.{CommonSpec, Extractors}
import connectors.ItsaStatusConnector
import models.ErrorModel
import models.monitoring.MandationStatusAuditModel
import models.status.MtdMandationStatus.Voluntary
import models.status._
import models.subscription.AccountingPeriodUtil
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import parsers.ItsaStatusParser.GetItsaStatusResponse
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsJson, contentAsString, defaultAwaitTimeout, status, stubControllerComponents}
import services.mocks.MockAuthService
import services.mocks.monitoring.MockAuditService
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier, Enrolments}
import utils.MaterializerSupport
import utils.TestConstants.hmrcAsAgent

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MandationStatusControllerSpec extends CommonSpec
  with MockAuthService with MockAuditService with Extractors with MaterializerSupport with GuiceOneAppPerSuite {

  private val testNino = "test-nino"
  private val testUtr = "test-utr"
  private val agentReferenceNumber = "123456789"
  private val enrolments = Enrolments(Set(Enrolment(hmrcAsAgent, Seq(EnrolmentIdentifier("AgentReferenceNumber", agentReferenceNumber)), "Activated")))

  private val request = FakeRequest().withBody(Json.toJson(MandationStatusRequest(testNino, testUtr)))
  private val invalidRequest = FakeRequest().withBody(Json.obj("invalid" -> "request"))

  private val expectedResponse = ItsaStatusResponse(
    List(
      TaxYearStatus(AccountingPeriodUtil.getCurrentTaxYear.toItsaStatusShortTaxYear, Voluntary),
      TaxYearStatus(AccountingPeriodUtil.getNextTaxYear.toItsaStatusShortTaxYear, Voluntary)
    )
  )

  "mandationStatus" should {
    "return 200 OK status" when {
      "the status-determination-service returns OK status and valid JSON" in withController(
        Future.successful(Right(expectedResponse))
      ) { controller =>
        mockRetrievalSuccess(enrolments)

        val result = controller.mandationStatus(request)
        status(result) shouldBe OK
        contentAsJson(result).as[MandationStatusResponse] shouldBe MandationStatusResponse(currentYearStatus = Voluntary, nextYearStatus = Voluntary)
        verifyAudit(MandationStatusAuditModel(
          Some(agentReferenceNumber),
          testUtr,
          testNino,
          AccountingPeriodUtil.getCurrentTaxYear.toShortTaxYear,
          MtdMandationStatus.Voluntary.value,
          AccountingPeriodUtil.getNextTaxYear.toShortTaxYear,
          MtdMandationStatus.Voluntary.value
        ))
      }
    }

    "return 400 BAD_REQUEST status" when {
      "the status-determination-service returns OK status and invalid JSON" in withController(
        Future.successful(Right(expectedResponse))
      ) { controller =>
        mockRetrievalSuccess(enrolments)

        val result = controller.mandationStatus(invalidRequest)
        status(result) shouldBe BAD_REQUEST
        contentAsString(result) shouldBe "Invalid MandationStatusRequest payload: " +
          "List(" +
          "(/utr,List(JsonValidationError(List(error.path.missing),ArraySeq()))), " +
          "(/nino,List(JsonValidationError(List(error.path.missing),ArraySeq())))" +
          ")"
        verifyNoAuditOfAnyKind()
      }
    }

    "return an 500 INTERNAL_SERVER_ERROR status" when {
      "the status-determination-service returns a failure" in withController(
        Future.successful(Left(ErrorModel(INTERNAL_SERVER_ERROR, "Error body")))
      ) { controller =>
        mockRetrievalSuccess(enrolments)

        val result = controller.mandationStatus(request)
        status(result) shouldBe INTERNAL_SERVER_ERROR
        contentAsString(result) shouldBe "Failed to retrieve the mandation status"
        verifyNoAuditOfAnyKind()
      }
    }
  }

  private def withController(expectedResponse: Future[GetItsaStatusResponse])(testCode: MandationStatusController => Any): Unit = {
    val mockConnector = mock[ItsaStatusConnector]

    when(mockConnector.getItsaStatus(ArgumentMatchers.eq(testNino), ArgumentMatchers.eq(testUtr))(ArgumentMatchers.any()))
      .thenReturn(expectedResponse)

    val controller = new MandationStatusController(mockAuthService, mockAuditService, mockConnector, stubControllerComponents())

    testCode(controller)
  }
}
