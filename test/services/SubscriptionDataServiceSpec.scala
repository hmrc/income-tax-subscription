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

import com.mongodb.client.result.DeleteResult
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
import repositories.SubscriptionDataRepository
import services.SubscriptionDataService.{Created, Existence, Existing}
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SubscriptionDataServiceSpec extends CommonSpec with MockitoSugar with FeatureSwitching with BeforeAndAfterEach {

  val mockServicesConfig: ServicesConfig = mock[ServicesConfig]
  val mockConfiguration: Configuration = mock[Configuration]
  val appConfig = new MicroserviceAppConfig(mockServicesConfig, mockConfiguration)

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
  val arn: String = "1"

  "retrieveReference" when {
    "arn is not provided" should {
      "create a reference in the database" when {
        "it doesn't exist in the database" in new Setup {
          when(mockSubscriptionDataRepository.retrieveReference(ArgumentMatchers.eq(utr), ArgumentMatchers.eq(None)))
            .thenReturn(Future.successful(None))
          when(mockSubscriptionDataRepository.createReference(ArgumentMatchers.eq(utr), ArgumentMatchers.eq(None)))
            .thenReturn(Future.successful(reference))

          val result: Existence = await(service.retrieveReference(utr, None))

          result shouldBe Created(reference)
        }
      }

      "return the reference from the database" when {
        "it exists in the database" in new Setup {
          when(mockSubscriptionDataRepository.retrieveReference(ArgumentMatchers.eq(utr), ArgumentMatchers.eq(None)))
            .thenReturn(Future.successful(Some(reference)))

          val result: Existence = await(service.retrieveReference(utr, None))

          result shouldBe Existing(reference)
        }
      }
    }
    "arn is provided" should {
      "create a reference in the database" when {
        "it doesn't exist in the database" in new Setup {
          when(mockSubscriptionDataRepository.retrieveReference(ArgumentMatchers.eq(utr), ArgumentMatchers.eq(Some(arn))))
            .thenReturn(Future.successful(None))
          when(mockSubscriptionDataRepository.createReference(ArgumentMatchers.eq(utr), ArgumentMatchers.eq(Some(arn))))
            .thenReturn(Future.successful(reference))

          val result: Existence = await(service.retrieveReference(utr, Some(arn)))

          result shouldBe Created(reference)
        }
      }

      "return the reference from the database" when {
        "it exists in the database" in new Setup {
          when(mockSubscriptionDataRepository.retrieveReference(ArgumentMatchers.eq(utr), ArgumentMatchers.eq(Some(arn))))
            .thenReturn(Future.successful(Some(reference)))

          val result: Existence = await(service.retrieveReference(utr, Some(arn)))

          result shouldBe Existing(reference)
        }
      }
    }
  }

  "getAllSubscriptionData" should {
    "retrieve all data using the reference" in new Setup {
      when(mockSubscriptionDataRepository.getReferenceData(ArgumentMatchers.eq(reference)))
        .thenReturn(Future.successful(Some(testJson)))

      await(service.getAllSubscriptionData(reference)) shouldBe Some(testJson)
    }
  }

  "retrieveSubscriptionData" should {
    "retrieve the data using reference and data id" in new Setup {
      when(mockSubscriptionDataRepository.getDataFromReference(ArgumentMatchers.eq(reference), ArgumentMatchers.eq(testDataId)))
        .thenReturn(Future.successful(Some(testJson)))

      await(service.retrieveSubscriptionData(reference, testDataId)) shouldBe Some(testJson)
    }
  }

  "insertSubscriptionData" should {
    "insert the data using the reference" in new Setup {
      when(mockSubscriptionDataRepository.insertDataWithReference(
        ArgumentMatchers.eq(reference), ArgumentMatchers.eq(testDataId), ArgumentMatchers.eq(testJson))
      ) thenReturn Future.successful(Some(testJson))

      await(service.insertSubscriptionData(reference, testDataId, testJson)) shouldBe Some(testJson)
    }
  }

  "deleteAllSubscriptionData" should {
    "delete the document using reference" in new Setup {
      when(mockSubscriptionDataRepository.deleteDataFromReference(ArgumentMatchers.eq(reference)))
        .thenReturn(Future.successful(DeleteResult.acknowledged(1)))

      await(service.deleteAllSubscriptionData(reference)).wasAcknowledged() shouldBe true
    }
  }

}
