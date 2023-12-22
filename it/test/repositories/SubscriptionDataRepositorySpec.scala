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
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.mongo.MongoUtils

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

class SubscriptionDataRepositorySpec extends AnyWordSpecLike with Matchers with OptionValues with GuiceOneAppPerSuite with BeforeAndAfterEach {

  val testSelfEmploymentsRepository: SubscriptionDataRepository = app.injector.instanceOf[SubscriptionDataRepository]

  override def beforeEach(): Unit = {
    await(testSelfEmploymentsRepository.drop())
    await(MongoUtils.ensureIndexes(testSelfEmploymentsRepository.collection, testSelfEmploymentsRepository.indexes, replaceIndexes = true))
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

  "deleteDataWithReference" when {
    "the document was found" should {
      "update the document so that it no longer contains the specified key" when {
        "The document contains the requested key" in new Setup(testDocumentWithoutSession()) {
          testSelfEmploymentsRepository.deleteDataWithReference(reference, "testDataIdOne").futureValue.isDefined shouldBe true

          val optionalData: Option[JsValue] = testSelfEmploymentsRepository.getReferenceData(reference).futureValue
          optionalData.isDefined shouldBe true
          val data: JsValue = optionalData.get
          (data \ "reference").asOpt[String] shouldBe Some(reference)
          (data \ "testDataIdOne").isDefined shouldBe false
          (data \ "testDataIdTwo").isDefined shouldBe true
        }
        "The document does not contain the requested key" in new Setup(testDocumentWithoutSession()) {
          testSelfEmploymentsRepository.deleteDataWithReference(reference, "testDataIdThree").futureValue.isDefined shouldBe true

          val optionalData: Option[JsValue] = testSelfEmploymentsRepository.getReferenceData(reference).futureValue
          optionalData.isDefined shouldBe true
          val data: JsValue = optionalData.get
          (data \ "reference").asOpt[String] shouldBe Some(reference)
          (data \ "testDataIdOne").isDefined shouldBe true
          (data \ "testDataIdTwo").isDefined shouldBe true
          (data \ "testDataIdThree").isDefined shouldBe false
        }
      }
    }
    "the document is not found" should {
      "return no document" in new Setup(testDocumentWithoutSession()) {
        testSelfEmploymentsRepository.deleteDataWithReference(reference + "-2", "testDataIdOne").futureValue shouldBe None

        val optionalData: Option[JsValue] = testSelfEmploymentsRepository.getReferenceData(reference).futureValue
        optionalData.isDefined shouldBe true
        val data: JsValue = optionalData.get
        (data \ "reference").asOpt[String] shouldBe Some(reference)
        (data \ "testDataIdOne").isDefined shouldBe true
        (data \ "testDataIdTwo").isDefined shouldBe true
      }
    }
  }

  "deleteDataFromReference" should {
    "remove a document which has the same reference when one exists" in new Setup(testDocumentWithoutSession()) {
      testSelfEmploymentsRepository.getReferenceData(reference).futureValue shouldBe Some(testDocumentWithoutSession())

      testSelfEmploymentsRepository.deleteDataFromReference(reference).futureValue.wasAcknowledged() shouldBe true

      testSelfEmploymentsRepository.getReferenceData(reference).futureValue shouldBe None
    }
    "return success when there are no matching documents" in new Setup(testDocumentWithoutSession("test-reference-2")) {
      testSelfEmploymentsRepository.getReferenceData(reference).futureValue shouldBe None

      testSelfEmploymentsRepository.deleteDataFromReference(reference).futureValue.wasAcknowledged() shouldBe true

      testSelfEmploymentsRepository.getReferenceData(reference).futureValue shouldBe None
    }
  }

  "createReference" when {
    "provided with both a utr and arn" should {
      "create a document in the database with the details required for its use" in {
        val createdReference: String = await(testSelfEmploymentsRepository.createReference(utr, Some(testArn)))

        val optionalData: Option[JsValue] = testSelfEmploymentsRepository.getReferenceData(createdReference).futureValue
        optionalData.isDefined shouldBe true

        val data: JsValue = optionalData.get

        (data \ "utr").asOpt[String] shouldBe Some(utr)
        (data \ "arn").asOpt[String] shouldBe Some(testArn)
        (data \ "reference").asOpt[String] shouldBe Some(createdReference)
        (data \ "lastUpdatedTimestamp").isDefined shouldBe true
      }
      "return an error if a document with the same utr, arn combo is already in the database" in {
        await(testSelfEmploymentsRepository.createReference(utr, Some(testArn)))

        intercept[Exception](await(testSelfEmploymentsRepository.createReference(utr, Some(testArn))))
      }
    }
    "provided with only a utr and no arn" should {
      "create a document in the database with the details required for its use" in {
        val createdReference: String = await(testSelfEmploymentsRepository.createReference(utr, None))

        val optionalData: Option[JsValue] = testSelfEmploymentsRepository.getReferenceData(createdReference).futureValue
        optionalData.isDefined shouldBe true

        val data: JsValue = optionalData.get

        (data \ "utr").asOpt[String] shouldBe Some(utr)
        (data \ "arn").asOpt[String] shouldBe None
        (data \ "reference").asOpt[String] shouldBe Some(createdReference)
        (data \ "lastUpdatedTimestamp").isDefined shouldBe true
      }
      "return an error if a document with the same utr only is already in the database" in {
        await(testSelfEmploymentsRepository.createReference(utr, None))

        intercept[Exception](await(testSelfEmploymentsRepository.createReference(utr, None)))
      }
    }
  }

  "retrieveReference" when {
    "provided with only a utr" should {
      "return a reference" when {
        "the related document with only a utr exists in the database" in {
          val createdReference: String = await(testSelfEmploymentsRepository.createReference(utr, None))

          testSelfEmploymentsRepository.retrieveReference(utr, None).futureValue shouldBe Some(createdReference)
        }
      }
      "return no reference" when {
        "the related utr without arn does not exist in the database" when {
          "no documents exist in the database" in {
            testSelfEmploymentsRepository.retrieveReference(utr, None).futureValue shouldBe None
          }
          "a document with the utr specified, but also an arn exists in the database" in {
            await(testSelfEmploymentsRepository.createReference(utr, Some(testArn)))

            testSelfEmploymentsRepository.retrieveReference(utr, None).futureValue shouldBe None
          }
        }
      }
    }
    "provided with a utr and arn" should {
      "return a reference" when {
        "the related utr + arn exists in the database" in {
          val createdReference: String = await(testSelfEmploymentsRepository.createReference(utr, Some(testArn)))

          testSelfEmploymentsRepository.retrieveReference(utr, Some(testArn)).futureValue shouldBe Some(createdReference)
        }
      }
      "return no reference" when {
        "the related utr + arn does not exist in the database" when {
          "no documents exist in the database" in {
            testSelfEmploymentsRepository.retrieveReference(utr, Some(testArn)).futureValue shouldBe None
          }
          "a document with only the utr exists in the database" in {
            await(testSelfEmploymentsRepository.createReference(utr, None))

            testSelfEmploymentsRepository.retrieveReference(utr, Some(testArn)).futureValue shouldBe None
          }
        }
      }
    }
  }

}
