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

import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.{BeforeAndAfterEach, Matchers, OptionValues, WordSpecLike}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.test.Helpers.{await, defaultAwaitTimeout}

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

class SubscriptionDataRepositorySpec extends WordSpecLike with Matchers with OptionValues  with GuiceOneAppPerSuite with BeforeAndAfterEach {

  val testSelfEmploymentsRepository: SubscriptionDataRepository = app.injector.instanceOf[SubscriptionDataRepository]

  override def beforeEach(): Unit = {
    await(testSelfEmploymentsRepository.drop)

    await(testSelfEmploymentsRepository.collection.indexesManager.ensure(testSelfEmploymentsRepository.sessionIdIndex))
    await(testSelfEmploymentsRepository.collection.indexesManager.ensure(testSelfEmploymentsRepository.ttlIndex))
    await(testSelfEmploymentsRepository.collection.indexesManager.ensure(testSelfEmploymentsRepository.utrCredIndex))
    await(testSelfEmploymentsRepository.collection.indexesManager.ensure(testSelfEmploymentsRepository.referenceIndex))
  }

  val testSessionId: String = "testSessionIdOne"
  val testDataId: String = "testDataId"
  val testData: JsObject = Json.obj("testDataIdKey" -> "testDataIdValue")
  val reference: String = "test-reference"
  val utr: String = "testUtr"
  val credId: String = "testCredId"

  def testDocument(sessionId: String, referenceValue: String = reference): JsObject = Json.obj(
    "sessionId" -> sessionId,
    "reference" -> referenceValue,
    "testDataIdOne" -> Json.obj(
      "testDataIdOneKey" -> "testDataIdOneValue"
    ),
    "testDataIdTwo" -> Json.obj(
      "testDataIdTwoKey" -> "testDataIdTwoValue"
    )
  )

  def testDocumentWithoutSession(referenceValue: String = reference): JsObject = Json.obj(
    "reference" -> referenceValue,
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
      "a document with the sessionId, reference and the dataId is found" in new Setup(
        testDocument(testSessionId),
        testDocument("testSessionIdTwo", "test-reference-2")
      ) {
        testSelfEmploymentsRepository.getDataFromSession(reference, testSessionId, "testDataIdOne").futureValue.get shouldBe Json.obj(
          "testDataIdOneKey" -> "testDataIdOneValue"
        )
      }
    }
    "return none" when {
      "no document with the sessionId and reference was found" in new Setup(testDocument("testSessionIdTwo")) {
        testSelfEmploymentsRepository.getDataFromSession(reference, testSessionId, "testDataIdOne").futureValue shouldBe None
      }
      "a document with the sessionId and reference was found but did not contain dataId" in new Setup(testDocument(testSessionId)) {
        testSelfEmploymentsRepository.getDataFromSession(reference, testSessionId, "testDataIdThree").futureValue shouldBe None
      }
    }
  }

  "getDataFromReference" should {
    "return the data relating to the dataId" when {
      "a document with the reference and dataId is found" in new Setup(testDocumentWithoutSession(), testDocumentWithoutSession("test-reference-2")) {
        testSelfEmploymentsRepository.getDataFromReference(reference, "testDataIdOne").futureValue shouldBe Some(Json.obj(
          "testDataIdOneKey" -> "testDataIdOneValue"
        ))
      }
    }
    "return none" when {
      "no document with the reference was found" in new Setup(testDocumentWithoutSession("test-reference-2")) {
        testSelfEmploymentsRepository.getDataFromReference(reference, "testDataIdOne").futureValue shouldBe None
      }
      "a document with the reference was found but did not contain dataId" in new Setup(testDocumentWithoutSession()) {
        testSelfEmploymentsRepository.getDataFromReference(reference, "testDataIdThree").futureValue shouldBe None
      }
    }
  }

  "getSessionIdData" should {
    "return the data relating to the sessionId and reference" when {
      "a document with the sessionId and reference is found" in new Setup(
        testDocument(testSessionId),
        testDocument("testSessionIdTwo", "test-reference-2")
      ) {
        testSelfEmploymentsRepository.getSessionIdData(reference, testSessionId).futureValue.get shouldBe testDocument(testSessionId)
      }
    }
    "return none" when {
      "no document with the sessionId and reference was found" in new Setup(testDocument("testSessionIdTwo")) {
        testSelfEmploymentsRepository.getSessionIdData(reference, testSessionId).futureValue shouldBe None
      }
    }
  }

  "getReferenceData" should {
    "return the data relating to the reference" when {
      "a document with the reference is found" in new Setup(testDocumentWithoutSession(), testDocumentWithoutSession("test-reference-2")) {
        testSelfEmploymentsRepository.getReferenceData(reference).futureValue shouldBe Some(testDocumentWithoutSession(reference))
      }
    }
    "return None" when {
      "no document with the reference was found" in new Setup(testDocumentWithoutSession("test-reference-2")) {
        testSelfEmploymentsRepository.getReferenceData(reference).futureValue shouldBe None
      }
    }
  }

  "insertDataWithSession" should {
    "upsert the data relating to the sessionId and reference" when {
      "there is no document with the sessionId and reference" in new Setup(testDocument("testSessionIdTwo")) {
        await(testSelfEmploymentsRepository.insertDataWithSession("test-reference-2", testSessionId, testDataId, testData))

        val optionalData: Option[JsValue] =testSelfEmploymentsRepository.getSessionIdData("test-reference-2", testSessionId).futureValue
        optionalData.isDefined shouldBe true
        val data: JsValue = optionalData.get
        (data \ "sessionId").asOpt[String] shouldBe Some(testSessionId)
        (data \ "reference").asOpt[String] shouldBe Some("test-reference-2")
        (data \ testDataId).asOpt[JsObject] shouldBe Some(testData)
        (data \ "lastUpdatedTimestamp").isDefined shouldBe true
      }


      "there is a document with the sessionId and reference but does not contain with the dataId" in new Setup(testDocument(testSessionId)) {

        await(testSelfEmploymentsRepository.insertDataWithSession(reference, testSessionId, testDataId, testData))

        val optionalData: Option[JsValue] = testSelfEmploymentsRepository.getSessionIdData(reference, testSessionId).futureValue
        optionalData.isDefined shouldBe true
        val data: JsValue = optionalData.get
        (data \ "sessionId").asOpt[String] shouldBe Some(testSessionId)
        (data \ "reference").asOpt[String] shouldBe Some(reference)
        (data \ testDataId).asOpt[JsObject] shouldBe Some(testData)
        (data \ "lastUpdatedTimestamp").isDefined shouldBe true
        (data \ "testDataIdOne" \ "testDataIdOneKey").asOpt[String] shouldBe Some("testDataIdOneValue")
        (data \ "testDataIdTwo" \ "testDataIdTwoKey").asOpt[String] shouldBe Some("testDataIdTwoValue")
      }

      "there is a document with the sessionId, reference and the dataId" in new Setup(testDocument(testSessionId)) {
        await(testSelfEmploymentsRepository.insertDataWithSession(reference, testSessionId, "testDataIdOne", testData))

        val optionalData: Option[JsValue] = testSelfEmploymentsRepository.getSessionIdData(reference, testSessionId).futureValue
        optionalData.isDefined shouldBe true
        val data: JsValue = optionalData.get
        (data \ "sessionId").asOpt[String] shouldBe Some(testSessionId)
        (data \ "reference").asOpt[String] shouldBe Some(reference)
        (data \ "lastUpdatedTimestamp").isDefined shouldBe true
        (data \ "testDataIdOne").asOpt[JsObject] shouldBe Some(testData)
        (data \ "testDataIdTwo" \ "testDataIdTwoKey").asOpt[String] shouldBe Some("testDataIdTwoValue")
      }
    }
  }

  "insertDataWithReference" should {
    "upsert the data relating to the reference" when {
      "there is no document with the reference" in new Setup(testDocumentWithoutSession("test-reference-2")) {
        await(testSelfEmploymentsRepository.insertDataWithReference(reference, testDataId, testData))

        val optionalData: Option[JsValue] = testSelfEmploymentsRepository.getReferenceData(reference).futureValue
        optionalData.isDefined shouldBe true
        val data: JsValue = optionalData.get
        (data \ "reference").asOpt[String] shouldBe Some(reference)
        (data \ testDataId).asOpt[JsObject] shouldBe Some(testData)
        (data \ "lastUpdatedTimestamp").isDefined shouldBe true
      }
      "there is a document with the reference but does not contain the dataId" in new Setup(testDocumentWithoutSession()) {
        await(testSelfEmploymentsRepository.insertDataWithReference(reference, testDataId, testData))

        val optionalData: Option[JsValue] = testSelfEmploymentsRepository.getReferenceData(reference).futureValue
        optionalData.isDefined shouldBe true
        val data: JsValue = optionalData.get
        (data \ "reference").asOpt[String] shouldBe Some(reference)
        (data \ testDataId).asOpt[JsObject] shouldBe Some(testData)
        (data \ "lastUpdatedTimestamp").isDefined shouldBe true
        (data \ "testDataIdOne" \ "testDataIdOneKey").asOpt[String] shouldBe Some("testDataIdOneValue")
        (data \ "testDataIdTwo" \ "testDataIdTwoKey").asOpt[String] shouldBe Some("testDataIdTwoValue")
      }
      "there is a document with the reference and the dataId" in new Setup(testDocumentWithoutSession()) {
        await(testSelfEmploymentsRepository.insertDataWithReference(reference, "testDataIdOne", testData))

        val optionalData: Option[JsValue] = testSelfEmploymentsRepository.getReferenceData(reference).futureValue
        optionalData.isDefined shouldBe true
        val data: JsValue = optionalData.get
        (data \ "reference").asOpt[String] shouldBe Some(reference)
        (data \ "lastUpdatedTimestamp").isDefined shouldBe true
        (data \ "testDataIdOne").asOpt[JsObject] shouldBe Some(testData)
        (data \ "testDataIdTwo" \ "testDataIdTwoKey").asOpt[String] shouldBe Some("testDataIdTwoValue")
      }
    }
  }

  "deleteDataFromSessionId" should {
    "remove a document which has the same reference and session id when one exists" in new Setup(testDocument(testSessionId)) {
      testSelfEmploymentsRepository.getSessionIdData(reference, testSessionId).futureValue shouldBe Some(testDocument(testSessionId))

      testSelfEmploymentsRepository.deleteDataFromSessionId(reference, testSessionId).futureValue.ok shouldBe true

      testSelfEmploymentsRepository.getSessionIdData(reference, testSessionId).futureValue shouldBe None
    }
    "return success when there are no matching documents" in new Setup(testDocument("testSessionIdTwo")) {
      testSelfEmploymentsRepository.getSessionIdData(reference, testSessionId).futureValue shouldBe None

      testSelfEmploymentsRepository.deleteDataFromSessionId(reference, testSessionId).futureValue.ok shouldBe true

      testSelfEmploymentsRepository.getSessionIdData(reference, testSessionId).futureValue shouldBe None
    }
  }

  "deleteDataFromReference" should {
    "remove a document which has the same reference when one exists" in new Setup(testDocumentWithoutSession()) {
      testSelfEmploymentsRepository.getReferenceData(reference).futureValue shouldBe Some(testDocumentWithoutSession())

      testSelfEmploymentsRepository.deleteDataFromReference(reference).futureValue.ok shouldBe true

      testSelfEmploymentsRepository.getReferenceData(reference).futureValue shouldBe None
    }
    "return success when there are no matching documents" in new Setup(testDocumentWithoutSession("test-reference-2")) {
      testSelfEmploymentsRepository.getReferenceData(reference).futureValue shouldBe None

      testSelfEmploymentsRepository.deleteDataFromReference(reference).futureValue.ok shouldBe true

      testSelfEmploymentsRepository.getReferenceData(reference).futureValue shouldBe None
    }
  }

  "createReference" should {
    "create a document in the database with the details required for its use" in {
      val createdReference: String = await(testSelfEmploymentsRepository.createReference(utr, credId, testSessionId))

      val optionalData: Option[JsValue] = testSelfEmploymentsRepository.getReferenceData(createdReference).futureValue
      optionalData.isDefined shouldBe true
      val data: JsValue = optionalData.get

      (data \ "utr").asOpt[String] shouldBe Some(utr)
      (data \ "credId").asOpt[String] shouldBe Some(credId)
      (data \ "sessionId").asOpt[String] shouldBe Some(testSessionId)
      (data \ "reference").asOpt[String] shouldBe Some(createdReference)
      (data \ "lastUpdatedTimestamp").isDefined shouldBe true
    }
    "return an error if a document with the same utr, cred id combo is already in the database" in {
      await(testSelfEmploymentsRepository.createReference(utr, credId, testSessionId))

      intercept[Exception](await(testSelfEmploymentsRepository.createReference(utr, credId, "test-session-id-2")))
    }
  }

  "retrieveReference" should {
    "return a reference" when {
      "the related utr + cred id exists in the database" in {
        val createdReference: String = await(testSelfEmploymentsRepository.createReference(utr, credId, testSessionId))

        testSelfEmploymentsRepository.retrieveReference(utr, credId).futureValue shouldBe Some(createdReference)
      }
    }
    "return no reference" when {
      "the related utr + cred id does not exist in the database" in {
        testSelfEmploymentsRepository.retrieveReference(utr, credId).futureValue shouldBe None
      }
    }
  }

}
