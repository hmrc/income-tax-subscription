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

import helpers.IntegrationTestConstants.testArn
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.mongo.MongoUtils

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

class SessionDataRepositorySpec extends AnyWordSpecLike with Matchers with OptionValues with GuiceOneAppPerSuite with BeforeAndAfterEach {

  val testSessionDataRepository: SessionDataRepository = app.injector.instanceOf[SessionDataRepository]

  override def beforeEach(): Unit = {
    await(testSessionDataRepository.drop())
    await(MongoUtils.ensureIndexes(testSessionDataRepository.collection, testSessionDataRepository.indexes, replaceIndexes = true))
  }

  val testSessionId: String = "testSessionIdOne"
  val testDataId: String = "testDataId"
  val testData: JsObject = Json.obj("testDataIdKey" -> "testDataIdValue")


  def testDocument(sessionId : String = testSessionId): JsObject = Json.obj(
    "session-id" -> sessionId,
    "testDataIdOne" -> Json.obj(
      "testDataIdOneKey" -> "testDataIdOneValue"
    ),
    "testDataIdTwo" -> Json.obj(
      "testDataIdTwoKey" -> "testDataIdTwoValue"
    )
  )

  class Setup(documents: JsObject*) {
    await(Future.sequence(documents.map(testSessionDataRepository.insert)))
  }

  "getDataFromSession" should {
    "return the data relating to the dataId" when {
      "a document with the session and dataId is found" in new Setup(testDocument()) {
        testSessionDataRepository.getDataFromSession(testSessionId, "testDataIdOne").futureValue shouldBe Some(Json.obj(
          "testDataIdOneKey" -> "testDataIdOneValue"
        ))
      }
    }
    "return none" when {
      "no document with the session was found" in new Setup(testDocument("testSessionIdTwo")) {
        testSessionDataRepository.getDataFromSession(testSessionId, "testDataIdOne").futureValue shouldBe None
      }
      "a document with the session was found but did not contain dataId" in new Setup(testDocument()) {
        testSessionDataRepository.getDataFromSession(testSessionId, "testDataIdThree").futureValue shouldBe None
      }
    }
  }

  "getSessionData" should {
    "return the data relating to the session" when {
      "a document with the session is found" in new Setup(testDocument()) {
        testSessionDataRepository.getSessionData(testSessionId).futureValue shouldBe Some(testDocument())
      }
    }
    "return None" when {
      "no document with the session was found" in new Setup(testDocument("testSessionIdTwo")) {
        testSessionDataRepository.getSessionData(testSessionId).futureValue shouldBe None
      }
    }
  }

  "insertDataWithSession" should {
    "upsert the data relating to the session" when {
      "there is no document with the session" in new Setup(testDocument("testSessionIdTwo")) {
        await(testSessionDataRepository.insertDataWithSession(testSessionId, testDataId, testData))

        val optionalData: Option[JsValue] = testSessionDataRepository.getSessionData(testSessionId).futureValue
        optionalData.isDefined shouldBe true
        val data: JsValue = optionalData.get
        (data \ "session-id").asOpt[String] shouldBe Some(testSessionId)
        (data \ testDataId).asOpt[JsObject] shouldBe Some(testData)
        (data \ "lastUpdatedTimestamp").isDefined shouldBe true
      }
      "there is a document with the session but does not contain the dataId" in new Setup(testDocument()) {
        await(testSessionDataRepository.insertDataWithSession(testSessionId, testDataId, testData))

        val optionalData: Option[JsValue] = testSessionDataRepository.getSessionData(testSessionId).futureValue
        optionalData.isDefined shouldBe true
        val data: JsValue = optionalData.get
        (data \ "session-id").asOpt[String] shouldBe Some(testSessionId)
        (data \ testDataId).asOpt[JsObject] shouldBe Some(testData)
        (data \ "lastUpdatedTimestamp").isDefined shouldBe true
        (data \ "testDataIdOne" \ "testDataIdOneKey").asOpt[String] shouldBe Some("testDataIdOneValue")
        (data \ "testDataIdTwo" \ "testDataIdTwoKey").asOpt[String] shouldBe Some("testDataIdTwoValue")
      }

      "there is a document with the session and the dataId" in new Setup(testDocument()) {
        await(testSessionDataRepository.insertDataWithSession(testSessionId, "testDataIdOne", testData))

        val optionalData: Option[JsValue] = testSessionDataRepository.getSessionData(testSessionId).futureValue
        optionalData.isDefined shouldBe true
        val data: JsValue = optionalData.get
        (data \ "session-id").asOpt[String] shouldBe Some(testSessionId)
        (data \ "lastUpdatedTimestamp").isDefined shouldBe true
        (data \ "testDataIdOne").asOpt[JsObject] shouldBe Some(testData)
        (data \ "testDataIdTwo" \ "testDataIdTwoKey").asOpt[String] shouldBe Some("testDataIdTwoValue")
      }
    }
  }

  "deleteDataWithSession" when {
    "the document was found" should {
      "update the document so that it no longer contains the specified key" when {
        "The document contains the requested key" in new Setup(testDocument()) {
          testSessionDataRepository.deleteDataWithSession(testSessionId, "testDataIdOne").futureValue.isDefined shouldBe true

          val optionalData: Option[JsValue] = testSessionDataRepository.getSessionData(testSessionId).futureValue
          optionalData.isDefined shouldBe true
          val data: JsValue = optionalData.get
          (data \ "session-id").asOpt[String] shouldBe Some(testSessionId)
          (data \ "testDataIdOne").isDefined shouldBe false
          (data \ "testDataIdTwo").isDefined shouldBe true
        }
        "The document does not contain the requested key" in new Setup(testDocument()) {
          testSessionDataRepository.deleteDataWithSession(testSessionId, "testDataIdThree").futureValue.isDefined shouldBe true

          val optionalData: Option[JsValue] = testSessionDataRepository.getSessionData(testSessionId).futureValue
          optionalData.isDefined shouldBe true
          val data: JsValue = optionalData.get
          (data \ "session-id").asOpt[String] shouldBe Some(testSessionId)
          (data \ "testDataIdOne").isDefined shouldBe true
          (data \ "testDataIdTwo").isDefined shouldBe true
          (data \ "testDataIdThree").isDefined shouldBe false
        }
      }
    }

    "deleteDataBySessionId" when {
      "the document was found" should {
        "delete the entire document" in new Setup(testDocument()) {
          testSessionDataRepository.deleteDataBySessionId(testSessionId).futureValue.map(_ \ "session-id").flatMap(_.asOpt[String]) shouldBe Some(testSessionId)

          val optionalData: Option[JsValue] = testSessionDataRepository.getSessionData(testSessionId).futureValue
          optionalData shouldBe None
        }
      }

      "the document is not found" should {
        "return no document" in new Setup(testDocument("testSessionIdTwo")) {
          testSessionDataRepository.deleteDataBySessionId(testSessionId).futureValue shouldBe None

          val optionalData: Option[JsValue] = testSessionDataRepository.getSessionData("testSessionIdTwo").futureValue
          optionalData.isDefined shouldBe true
        }
      }
    }

    "the document is not found" should {
      "return no document" in new Setup(testDocument()) {
        testSessionDataRepository.deleteDataWithSession(testSessionId + "-2", "testDataIdOne").futureValue shouldBe None

        val optionalData: Option[JsValue] = testSessionDataRepository.getSessionData(testSessionId).futureValue
        optionalData.isDefined shouldBe true
        val data: JsValue = optionalData.get
        (data \ "session-id").asOpt[String] shouldBe Some(testSessionId)
        (data \ "testDataIdOne").isDefined shouldBe true
        (data \ "testDataIdTwo").isDefined shouldBe true
      }
    }
  }
}
