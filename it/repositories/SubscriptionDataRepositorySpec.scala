/*
 * Copyright 2018 HM Revenue & Customs
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

package repositories

import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

class SubscriptionDataRepositorySpec extends UnitSpec with GuiceOneAppPerSuite with BeforeAndAfterEach {

  val testSelfEmploymentsRepository: SubscriptionDataRepository = app.injector.instanceOf[SubscriptionDataRepository]

  override def beforeEach(): Unit = {
    await(testSelfEmploymentsRepository.drop)
  }

  val testSessionId: String = "testSessionIdOne"
  val testDataId: String = "testDataId"
  val testData: JsObject = Json.obj("testDataIdKey" -> "testDataIdValue")

  def testDocument(sessionId: String): JsObject = Json.obj(
    "sessionId" -> sessionId,
    "testDataIdOne" -> Json.obj(
      "testDataIdOneKey" -> "testDataIdOneValue"
    ),
    "testDataIdTwo" -> Json.obj(
      "testDataIdTwoKey" -> "testDataIdTwoValue"
    )
  )

  class Setup(documents: JsObject*) {
    await(Future.sequence(documents.map(testSelfEmploymentsRepository.insert)))
  }

  "getDataFromSession" should {
    "return the data relating to the dataId" when {
      "a document with the sessionId and the dataId is found" in new Setup(testDocument(testSessionId), testDocument("testSessionIdTwo")) {
        await(testSelfEmploymentsRepository.getDataFromSession(testSessionId, "testDataIdOne")).get shouldBe Json.obj(
          "testDataIdOneKey" -> "testDataIdOneValue"
        )
      }
    }
    "return none" when {
      "no document with the sessionId was found" in new Setup(testDocument("testSessionIdTwo")) {
        await(testSelfEmploymentsRepository.getDataFromSession(testSessionId, "testDataIdOne")) shouldBe None
      }
      "a document with the sessionId was found but did not contain dataId" in new Setup(testDocument(testSessionId)) {
        await(testSelfEmploymentsRepository.getDataFromSession(testSessionId, "testDataIdThree")) shouldBe None
      }
    }
  }

  "getSessionIdData" should {
    "return the data relating to the sessionId" when {
      "a document with the sessionId is found" in new Setup(testDocument(testSessionId), testDocument("testSessionIdTwo")) {
        await(testSelfEmploymentsRepository.getSessionIdData(testSessionId)).get shouldBe testDocument(testSessionId)
      }
    }
    "return none" when {
      "no document with the sessionId was found" in new Setup(testDocument("testSessionIdTwo")) {
        await(testSelfEmploymentsRepository.getSessionIdData(testSessionId)) shouldBe None
      }
    }
  }

  "insertDataWithSession" should {
    "upsert the data relating to the sessionId" when {
      "there is no document with the sessionId" in new Setup(testDocument("testSessionIdTwo")) {
        await(testSelfEmploymentsRepository.insertDataWithSession(testSessionId, testDataId, testData))

        await(testSelfEmploymentsRepository.getSessionIdData(testSessionId)) shouldBe Some(Json.obj(
          "sessionId" -> testSessionId,
          testDataId -> testData
        ))
      }

      "there is a document with the sessionId but does not contain with the dataId" in new Setup(testDocument(testSessionId)) {

        await(testSelfEmploymentsRepository.insertDataWithSession(testSessionId, testDataId, testData))

        await(testSelfEmploymentsRepository.getSessionIdData(testSessionId)) shouldBe Some(
          testDocument(testSessionId) ++ Json.obj(testDataId -> testData)
        )
      }

      "there is a document with the sessionId and the dataId" in new Setup(testDocument(testSessionId)) {
        await(testSelfEmploymentsRepository.insertDataWithSession(testSessionId, "testDataIdOne", testData))

        await(testSelfEmploymentsRepository.getSessionIdData(testSessionId)) shouldBe Some(
          testDocument(testSessionId) ++ Json.obj("testDataIdOne" -> testData)
        )
      }

    }
  }

}
