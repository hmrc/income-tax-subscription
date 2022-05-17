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

package services

import common.CommonSpec
import config.MicroserviceAppConfig
import config.featureswitch.{FeatureSwitching, SaveAndRetrieve}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.Configuration
import play.api.http.Status.OK
import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import reactivemongo.api.commands.UpdateWriteResult
import repositories.SubscriptionDataRepository
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException, SessionId}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SubscriptionDataServiceSpec extends CommonSpec with MockitoSugar with FeatureSwitching with BeforeAndAfterEach {

  val mockServicesConfig: ServicesConfig = mock[ServicesConfig]
  val mockConfiguration: Configuration = mock[Configuration]
  val appConfig = new MicroserviceAppConfig(mockServicesConfig, mockConfiguration)

  override def beforeEach(): Unit = {
    super.beforeEach()
    disable(SaveAndRetrieve)
  }

  trait Setup {
    val mockSubscriptionDataRepository: SubscriptionDataRepository = mock[SubscriptionDataRepository]
    val service = new SubscriptionDataService(mockSubscriptionDataRepository, appConfig)
  }

  val testJson: JsObject = Json.obj(
    "testDataIdKey" -> "testDataIdValue"
  )
  val testSessionId: String = "sessionId"
  val testDataId: String = "dataId"
  val reference: String = "test-reference"
  val utr: String = "1234567890"
  val credId: String = "test-cred-id"

  "sessionIdFromHC" should {
    "return the sessionId" when {
      "there is one sessionId value" in new Setup {
        implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId(testSessionId)))
        val result: String = service.sessionIdFromHC(hc)
        result shouldBe testSessionId
      }
    }

    "throw internal server exception" when {
      "the sessionId is not in the headerCarrier" in new Setup {
        implicit val hc: HeaderCarrier = HeaderCarrier()

        val result: InternalServerException = intercept[InternalServerException](service.sessionIdFromHC(hc))
        result.message shouldBe "[SubscriptionDataService][retrieveSelfEmployments] - No session id in header carrier"
      }
    }

  }

  "retrieveReference" should {
    "return the reference from the database" when {
      "it exists in the database" in new Setup {
        implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId(testSessionId)))

        when(mockSubscriptionDataRepository.retrieveReference(ArgumentMatchers.eq(utr), ArgumentMatchers.eq(credId)))
          .thenReturn(Future.successful(Some(reference)))

        val result: String = await(service.retrieveReference(utr, credId))

        result shouldBe reference
      }
    }
    "create a reference in the database" when {
      "it doesn't exist in the database" in new Setup {
        implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId(testSessionId)))

        when(mockSubscriptionDataRepository.retrieveReference(ArgumentMatchers.eq(utr), ArgumentMatchers.eq(credId)))
          .thenReturn(Future.successful(None))
        when(mockSubscriptionDataRepository.createReference(ArgumentMatchers.eq(utr), ArgumentMatchers.eq(credId), ArgumentMatchers.eq(testSessionId)))
          .thenReturn(Future.successful(reference))

        val result: String = await(service.retrieveReference(utr, credId))

        result shouldBe reference
      }
    }
  }

  "getAllSubscriptionData" when {
    "the save & retrieve feature switch is disabled" should {
      "retrieve all data using the session id" when {
        "available in the headerCarrier" in new Setup {
          implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId(testSessionId)))

          when(mockSubscriptionDataRepository.getSessionIdData(reference, testSessionId))
            .thenReturn(Future.successful(Some(testJson)))

          await(service.getAllSubscriptionData(reference)) shouldBe Some(testJson)
        }
      }
    }
    "the save and retrieve feature switch is enabled" should {
      "retrieve all data using the reference" in new Setup {
        enable(SaveAndRetrieve)

        implicit val hc: HeaderCarrier = HeaderCarrier()

        when(mockSubscriptionDataRepository.getReferenceData(ArgumentMatchers.eq(reference)))
          .thenReturn(Future.successful(Some(testJson)))

        await(service.getAllSubscriptionData(reference)) shouldBe Some(testJson)
      }
    }
  }

  "retrieveSubscriptionData" when {
    "the save and retrieve feature switch is disabled" should {
      "retrieve the data using session id and the data id" when {
        "sessionId is available in the headerCarrier" in new Setup {
          implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId(testSessionId)))

          when(mockSubscriptionDataRepository.getDataFromSession(reference, testSessionId, testDataId))
            .thenReturn(Future.successful(Some(testJson)))

          await(service.retrieveSubscriptionData(reference, testDataId)) shouldBe Some(testJson)
        }
      }
    }
    "the save and retrieve feature switch is enabled" should {
      "retrieve the data using reference and data id" in new Setup {
        enable(SaveAndRetrieve)

        implicit val hc: HeaderCarrier = HeaderCarrier()

        when(mockSubscriptionDataRepository.getDataFromReference(ArgumentMatchers.eq(reference), ArgumentMatchers.eq(testDataId)))
          .thenReturn(Future.successful(Some(testJson)))

        await(service.retrieveSubscriptionData(reference, testDataId)) shouldBe Some(testJson)
      }
    }
  }

  "insertSubscriptionData" when {
    "the save and retrieve feature switch is disabled" should {
      "insert the data using the session id" when {
        "sessionId is available in the headerCarrier" in new Setup {
          implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId(testSessionId)))

          when(mockSubscriptionDataRepository.insertDataWithSession(reference, testSessionId, testDataId, testJson))
            .thenReturn(Future.successful(Some(testJson)))

          await(service.insertSubscriptionData(reference, testDataId, testJson)) shouldBe Some(testJson)
        }
      }
    }
    "the save and retrieve feature switch is enabled" should {
      "insert the data using the reference" in new Setup {
        enable(SaveAndRetrieve)

        implicit val hc: HeaderCarrier = HeaderCarrier()

        when(mockSubscriptionDataRepository.insertDataWithReference(
          ArgumentMatchers.eq(reference), ArgumentMatchers.eq(testDataId), ArgumentMatchers.eq(testJson))
        ) thenReturn Future.successful(Some(testJson))

        await(service.insertSubscriptionData(reference, testDataId, testJson)) shouldBe Some(testJson)
      }
    }
  }

  "deleteAllSubscriptionData" when {
    "the save & retrieve feature switch is disabled" should {
      "delete the document using session id" when {
        "sessionId is available in the headerCarrier" in new Setup {
          implicit val hc: HeaderCarrier = HeaderCarrier(sessionId = Some(SessionId(testSessionId)))

          when(mockSubscriptionDataRepository.deleteDataFromSessionId(ArgumentMatchers.eq(reference), ArgumentMatchers.eq(testSessionId)))
            .thenReturn(Future.successful(UpdateWriteResult(ok = true, 1, 1, Seq(), Seq(), None, Some(OK), None)))

          await(service.deleteAllSubscriptionData(reference)).ok shouldBe true
        }
      }
    }
    "the save & retrieve feature switch is enabled" should {
      "delete the document using reference" in new Setup {
        enable(SaveAndRetrieve)

        implicit val hc: HeaderCarrier = HeaderCarrier()

        when(mockSubscriptionDataRepository.deleteDataFromReference(ArgumentMatchers.eq(reference)))
          .thenReturn(Future.successful(UpdateWriteResult(ok = true, 1, 1, Seq(), Seq(), None, Some(OK), None)))

        await(service.deleteAllSubscriptionData(reference)).ok shouldBe true
      }
    }
  }

}
