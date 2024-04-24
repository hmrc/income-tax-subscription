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

package services

import common.CommonSpec
import config.MicroserviceAppConfig
import config.featureswitch.FeatureSwitching
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.Configuration
import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import repositories.SessionDataRepository
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException, SessionId}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.Future

class SessionDataServiceSpec extends CommonSpec with MockitoSugar with FeatureSwitching with BeforeAndAfterEach {

  val mockServicesConfig: ServicesConfig = mock[ServicesConfig]
  val mockConfiguration: Configuration = mock[Configuration]
  val appConfig = new MicroserviceAppConfig(mockServicesConfig, mockConfiguration)

  trait Setup {
    val mockSessionDataRepository: SessionDataRepository = mock[SessionDataRepository]
    val service = new SessionDataService(mockSessionDataRepository, appConfig)
  }

  val testJson: JsObject = Json.obj(
    "testDataIdKey" -> "testDataIdValue"
  )
  val testSessionId: String = "sessionId"
  val testDataId: String = "dataId"

  val headerCarrier = HeaderCarrier(sessionId = Some(SessionId(testSessionId)))
  val headerCarrierWithOutSessionId = HeaderCarrier()
  val errorMessage = "[SessionDataService][sessionIdFromHC] - No session id in header carrier"

  "getAllSessionData" should {
    "retrieve all data using the sessionId" in new Setup {
      when(mockSessionDataRepository.getSessionData(ArgumentMatchers.eq(testSessionId)))
        .thenReturn(Future.successful(Some(testJson)))

      await(service.getAllSessionData(headerCarrier)) shouldBe Some(testJson)
    }
    "throw an internal server exception when no session id exists" in new Setup {
      intercept[InternalServerException](
        await(service.getAllSessionData(headerCarrierWithOutSessionId))
      ).message shouldBe errorMessage
    }
  }

  "retrieveSessionData" should {
    "retrieve the data using session and data id" in new Setup {
      when(mockSessionDataRepository.getDataFromSession(ArgumentMatchers.eq(testSessionId), ArgumentMatchers.eq(testDataId)))
        .thenReturn(Future.successful(Some(testJson)))

      await(service.getSessionData(testDataId)(headerCarrier)) shouldBe Some(testJson)
    }
    "throw an internal server exception when no session id exists" in new Setup {
      intercept[InternalServerException](
        await(service.getSessionData(testDataId)(headerCarrierWithOutSessionId))
      ).message shouldBe errorMessage
    }
  }

  "insertSessionData" should {
    "insert the data using the session id" in new Setup {
      when(mockSessionDataRepository.insertDataWithSession(
        ArgumentMatchers.eq(testSessionId), ArgumentMatchers.eq(testDataId), ArgumentMatchers.eq(testJson))
      ) thenReturn Future.successful(Some(testJson))

      await(service.insertSessionData(testDataId, testJson)(headerCarrier)) shouldBe Some(testJson)
    }
    "throw an internal server exception when no session id exists" in new Setup {
      intercept[InternalServerException](
        await(service.insertSessionData(testDataId, testJson)(headerCarrierWithOutSessionId))
      ).message shouldBe errorMessage
    }
  }

  "deleteSessionData" should {
    "delete dataId using sessionData" in new Setup {
      when(mockSessionDataRepository.deleteDataWithSession(ArgumentMatchers.eq(testSessionId), ArgumentMatchers.eq(testDataId)))
        .thenReturn(Future.successful(Some(testJson)))

      await(service.deleteSessionData(testDataId)(headerCarrier)) shouldBe Some(testJson)
    }
    "throw an internal server exception when no session id exists" in new Setup {
      intercept[InternalServerException](
        await(service.deleteSessionData(testDataId)(headerCarrierWithOutSessionId))
      ).message shouldBe errorMessage
    }
  }

}
