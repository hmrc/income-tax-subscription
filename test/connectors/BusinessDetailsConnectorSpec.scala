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

package connectors

import _root_.utils.TestConstants._
import connectors.mocks.TestBusinessDetailsConnector
import models.ErrorModel
import models.monitoring.getBusinessDetails.BusinessDetailsAuditModel
import models.registration._
import org.scalatest.BeforeAndAfterEach
import play.api.http.Status._
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import services.mocks.monitoring.MockAuditService
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging.{eventTypeBadRequest, eventTypeInternalServerError, eventTypeServerUnavailable, eventTypeUnexpectedError}

import scala.concurrent.ExecutionContext.Implicits.global

class BusinessDetailsConnectorSpec extends TestBusinessDetailsConnector with MockAuditService with BeforeAndAfterEach {

  implicit val hc = HeaderCarrier()
  implicit val request: Request[_] = FakeRequest()

  "BusinessDetailsConnector.getBusinessDetailsUrl" should {
    "GET to the correct url" in {
      TestBusinessDetailsConnector.getBusinessDetailsUrl(testNino) should endWith(s"/registration/business-details/nino/$testNino")
    }
  }

  "BusinessDetailsConnector.getBusinessDetails" should {
    def result(f: GetBusinessDetailsUtil.Response => Any): Any = await(TestBusinessDetailsConnector.getBusinessDetails(testNino).map(v => f(v)))

    "parse and return the success response correctly" in {
      mockBusinessDetails(getBusinessDetailsSuccess)
      result { r =>
        r shouldBe Right(GetBusinessDetailsSuccessResponseModel(testMtditId))
        verifyNoAuditOfAnyKind()
      }
    }

    "parse and return the Bad request response correctly" in {
      mockBusinessDetails(INVALID_NINO)
      result { r =>
        r shouldBe Left(INVALID_NINO_MODEL)
        verifyAudit(eventTypeBadRequest, INVALID_NINO)
      }
    }

    "parse and return the Resource not found response correctly" in {
      mockBusinessDetails(NOT_FOUND_NINO)
      result { r =>
        r shouldBe Left(NOT_FOUND_NINO_MODEL)
        verifyNoAuditOfAnyKind()
      }
    }

    "parse and return the Conflict error response correctly" in {
      mockBusinessDetails(CONFLICT_ERROR)
      result { r =>
        r shouldBe Left(CONFLICT_ERROR_MODEL)
        verifyAudit(eventTypeUnexpectedError, CONFLICT_ERROR)
      }
    }

    "parse and return the Server error response correctly" in {
      mockBusinessDetails(SERVER_ERROR)
      verifyNoAuditOfAnyKind()
      result { r =>
        r shouldBe Left(SERVER_ERROR_MODEL)
        verifyAudit(eventTypeInternalServerError, SERVER_ERROR)
      }
    }

    "parse and return the Service unavailable response correctly" in {
      mockBusinessDetails(UNAVAILABLE)
      result { r =>
        r shouldBe Left(UNAVAILABLE_MODEL)
        verifyAudit(eventTypeServerUnavailable, UNAVAILABLE)
      }
    }

    "return parse error for corrupt response" in {
      mockBusinessDetails(CORRUPT)
      result { r =>
        r shouldBe Left(ErrorModel(INTERNAL_SERVER_ERROR, ErrorModel.parseFailure(corruptResponse)))
        verifyAudit(eventTypeBadRequest, CORRUPT)
      }
    }

  }

  private def verifyAudit(suffix: String, testHttpResponse: TestHttpResponse): Unit =
    verifyAudit(BusinessDetailsAuditModel(testNino, suffix, testHttpResponse._2.toString()))

}
