/*
 * Copyright 2022 HM Revenue & Customs
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

import common.CommonSpec
import connectors.MandationStatusConnector
import models.ErrorModel
import models.status.MandationStatus.Voluntary
import models.status.{MandationStatusRequest, MandationStatusResponse}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import parsers.MandationStatusParser.PostMandationStatusResponse
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsJson, contentAsString, defaultAwaitTimeout, status, stubControllerComponents}
import utils.MaterializerSupport

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MandationStatusControllerSpec extends CommonSpec with MaterializerSupport with GuiceOneAppPerSuite {
  private val request = FakeRequest().withBody(Json.toJson(MandationStatusRequest("test-nino", "test-utr")))
  private val invalidRequest = FakeRequest().withBody(Json.obj("invalid" -> "request"))

  "mandationStatus" should {
    "return 200 OK status" when {
      "the status-determination-service returns OK status and valid JSON" in withController(
        Future.successful(Right(MandationStatusResponse(currentYearStatus = Voluntary, nextYearStatus = Voluntary)))
      ) { controller =>
        val result = controller.mandationStatus(request)
        status(result) shouldBe OK
        contentAsJson(result).as[MandationStatusResponse] shouldBe MandationStatusResponse(currentYearStatus = Voluntary, nextYearStatus = Voluntary)
      }
    }

    "return 400 BAD_REQUEST status" when {
      "the status-determination-service returns OK status and valid JSON" in withController(
        Future.successful(Right(MandationStatusResponse(currentYearStatus = Voluntary, nextYearStatus = Voluntary)))
      ) { controller =>
        val result = controller.mandationStatus(invalidRequest)
        status(result) shouldBe BAD_REQUEST
        contentAsString(result) shouldBe "Invalid MandationStatusRequest payload: " +
          "List(" +
          "(/nino,List(JsonValidationError(List(error.path.missing),WrappedArray()))), " +
          "(/utr,List(JsonValidationError(List(error.path.missing),WrappedArray())))" +
          ")"
      }
    }

    "return an 500 INTERNAL_SERVER_ERROR status" when {
      "the status-determination-service returns a failure" in withController(
        Future.successful(Left(ErrorModel(INTERNAL_SERVER_ERROR, "Error body")))
      ) { controller =>
        val result = controller.mandationStatus(request)
        status(result) shouldBe INTERNAL_SERVER_ERROR
        contentAsString(result) shouldBe "Failed to retrieve the mandation status"
      }
    }
  }

  private def withController(expectedResponse: Future[PostMandationStatusResponse])(testCode: MandationStatusController => Any): Unit = {
    val mockConnector = mock[MandationStatusConnector]

    when(mockConnector.getMandationStatus(ArgumentMatchers.eq("test-nino"), ArgumentMatchers.eq("test-utr"))(ArgumentMatchers.any()))
      .thenReturn(expectedResponse)

    val controller = new MandationStatusController(mockConnector, stubControllerComponents())

    testCode(controller)
  }
}
