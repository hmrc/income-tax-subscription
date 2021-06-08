/*
 * Copyright 2021 HM Revenue & Customs
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

import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{JsObject, Json}
import reactivemongo.api.commands.UpdateWriteResult
import repositories.SubscriptionDataRepository
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException, SessionId}
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SubscriptionDataServiceSpec extends UnitSpec with MockitoSugar {

  trait Setup {
    val mockSelfEmploymentsRepository: SubscriptionDataRepository = mock[SubscriptionDataRepository]
    val service = new SubscriptionDataService(mockSelfEmploymentsRepository)
  }

  val testJson: JsObject = Json.obj(
    "testDataIdKey" -> "testDataIdValue"
  )
  val testSessionId: String = "sessionId"
  val testDataId: String = "dataId"

  "sessionIdFromHC" should {
    "return the sessionId" when {
      "there is one sessionId value" in new Setup {
        implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId(testSessionId)))
        val result = service.sessionIdFromHC(hc)
        result shouldBe testSessionId
      }
    }

    "throw internal server exception" when {
      "the sessionId is not in the headerCarrier" in new Setup {
        implicit val hc: HeaderCarrier = HeaderCarrier()

        val result: InternalServerException = intercept[InternalServerException](await(service.sessionIdFromHC(hc)))
        result.message shouldBe "[SubscriptionDataService][retrieveSelfEmployments] - No session id in header carrier"
      }
    }

  }

  "getAllSelfEmployments" should {
    "call the repo with the sessionId" when {
      "available in the headerCarrier" in new Setup {
        implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId(testSessionId)))

        when(mockSelfEmploymentsRepository.getSessionIdData(testSessionId))
          .thenReturn(Future.successful(Some(testJson)))

        await(service.getAllSelfEmployments) shouldBe Some(testJson)
      }
    }
  }

  "retrieveSelfEmployments" should {
    "call the repo with the sessionId and dataId" when {
      "sessionId is available in the headerCarrier" in new Setup {
        implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId(testSessionId)))

        when(mockSelfEmploymentsRepository.getDataFromSession(testSessionId, testDataId))
          .thenReturn(Future.successful(Some(testJson)))

        await(service.retrieveSelfEmployments(testDataId)) shouldBe Some(testJson)
      }
    }
  }

  "insertSelfEmployments" should {
    "call the repo with the sessionId, dataId and data" when {
      "sessionId is available in the headerCarrier" in new Setup {
        implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId(testSessionId)))

        when(mockSelfEmploymentsRepository.insertDataWithSession(testSessionId, testDataId, testJson))
          .thenReturn(Future.successful(Some(testJson)))

        await(service.insertSelfEmployments(testDataId, testJson)) shouldBe Some(testJson)
      }
    }
  }

  "deleteSessionData" should {
    "call the repo with the sessionId" when {
      "sessionId is available in the headerCarrier" in new Setup {
        implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId(testSessionId)))

        when(mockSelfEmploymentsRepository.deleteDataFromSessionId(testSessionId))
          .thenReturn(Future.successful(UpdateWriteResult(true, 1, 1, Seq(), Seq(), None, Some(200), None)))

        await(service.deleteSessionData).ok shouldBe true
      }
    }
  }

}
