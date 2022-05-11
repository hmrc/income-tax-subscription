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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.inject
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers.{await, defaultAwaitTimeout}

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

class ThrottlingRepositorySpec extends AnyWordSpecLike with Matchers with OptionValues with GuiceOneAppPerSuite with BeforeAndAfterEach {

  val testThrottleIdOne: String = "testThrottleIdOne"
  val testThrottleIdTwo: String = "testThrottleIdTwo"

  val testThrottleInstantNow: Long = 1000000
  val testThrottleTimecode: Long = testThrottleInstantNow / 60000

  val testInstantProvider: InstantProvider = new InstantProvider {

    override def getInstantNowMilli: Long = testThrottleInstantNow

  }

  case class ThrottleItem(throttleId: String, throttleTime: Long, count: Int)

  class Setup(documents: ThrottleItem*) {
    val testThrottlingRepository: ThrottlingRepository = GuiceApplicationBuilder()
      .overrides(inject.bind[InstantProvider].to(testInstantProvider))
      .build().injector.instanceOf[ThrottlingRepository]

    await(testThrottlingRepository.drop)

    await(testThrottlingRepository.collection.indexesManager.ensure(testThrottlingRepository.idTimecodeIndex))
    await(testThrottlingRepository.collection.indexesManager.ensure(testThrottlingRepository.ttlIndex))

    await(Future.sequence(documents.map(item => testThrottlingRepository.insert(
      Json.obj(
        testThrottlingRepository.throttleIdKey -> item.throttleId,
        testThrottlingRepository.timecodeKey -> item.throttleTime,
        testThrottlingRepository.countKey -> item.count
      )
    ))))

    def findThrottleDocument(throttleId: String, timecode: Long): Option[JsObject] = {
      await(testThrottlingRepository.find(
        testThrottlingRepository.throttleIdKey -> throttleId,
        testThrottlingRepository.timecodeKey -> timecode
      ) map (_.headOption))
    }
  }

  "checkThrottle" when {
    "there are no documents in the collection" should {
      "create a new document and return the starting count" in new Setup {
        await(testThrottlingRepository.checkThrottle(testThrottleIdOne)) shouldBe 1
        val optDocument: Option[JsObject] = findThrottleDocument(testThrottleIdOne, testThrottleTimecode)
        optDocument.isDefined shouldBe true
        optDocument map { document =>
          (document \ testThrottlingRepository.throttleIdKey).asOpt[String] shouldBe Some(testThrottleIdOne)
          (document \ testThrottlingRepository.timecodeKey).asOpt[Long] shouldBe Some(testThrottleTimecode)
          (document \ testThrottlingRepository.countKey).asOpt[Int] shouldBe Some(1)
          (document \ testThrottlingRepository.lastUpdatedTimestampKey).isDefined shouldBe true
        }
      }
      "return a count equal to the number of times called" in new Setup {
        await(testThrottlingRepository.checkThrottle(testThrottleIdOne))
        await(testThrottlingRepository.checkThrottle(testThrottleIdOne))
        await(testThrottlingRepository.checkThrottle(testThrottleIdOne))

        val optDocument: Option[JsObject] = findThrottleDocument(testThrottleIdOne, testThrottleTimecode)
        optDocument.isDefined shouldBe true
        optDocument map { document =>
          (document \ testThrottlingRepository.throttleIdKey).asOpt[String] shouldBe Some(testThrottleIdOne)
          (document \ testThrottlingRepository.timecodeKey).asOpt[Long] shouldBe Some(testThrottleTimecode)
          (document \ testThrottlingRepository.countKey).asOpt[Int] shouldBe Some(3)
          (document \ testThrottlingRepository.lastUpdatedTimestampKey).isDefined shouldBe true
        }
      }
    }

    "there is already a document in the collection with the same throttle and timecode" should {
      "update the existing document with its new count and return the new count" in new Setup(
        ThrottleItem(testThrottleIdOne, testThrottleTimecode, count = 1)
      ) {
        await(testThrottlingRepository.checkThrottle(testThrottleIdOne)) shouldBe 2
        val optDocument: Option[JsObject] = findThrottleDocument(testThrottleIdOne, testThrottleTimecode)
        optDocument.isDefined shouldBe true
        optDocument map { document =>
          (document \ testThrottlingRepository.throttleIdKey).asOpt[String] shouldBe Some(testThrottleIdOne)
          (document \ testThrottlingRepository.timecodeKey).asOpt[Long] shouldBe Some(testThrottleTimecode)
          (document \ testThrottlingRepository.countKey).asOpt[Int] shouldBe Some(2)
          (document \ testThrottlingRepository.lastUpdatedTimestampKey).isDefined shouldBe true
        }
      }
    }

    "there is already a document in the collection with the same throttle id but a different timecode" should {
      "create a new document and return the starting count" in new Setup(
        ThrottleItem(testThrottleIdOne, testThrottleTimecode - 1, count = 1)
      ) {
        await(testThrottlingRepository.checkThrottle(testThrottleIdOne)) shouldBe 1
        val optDocument: Option[JsObject] = findThrottleDocument(testThrottleIdOne, testThrottleTimecode)
        optDocument.isDefined shouldBe true
        optDocument map { document =>
          (document \ testThrottlingRepository.throttleIdKey).asOpt[String] shouldBe Some(testThrottleIdOne)
          (document \ testThrottlingRepository.timecodeKey).asOpt[Long] shouldBe Some(testThrottleTimecode)
          (document \ testThrottlingRepository.countKey).asOpt[Int] shouldBe Some(1)
          (document \ testThrottlingRepository.lastUpdatedTimestampKey).isDefined shouldBe true
        }
      }
    }

    "there is already a document in the collection with the same throttle timecode but a different id" should {
      "create a new document and return the starting count" in new Setup(
        ThrottleItem(testThrottleIdTwo, testThrottleTimecode, count = 1)
      ) {
        await(testThrottlingRepository.checkThrottle(testThrottleIdOne)) shouldBe 1
        val optDocument: Option[JsObject] = findThrottleDocument(testThrottleIdOne, testThrottleTimecode)
        optDocument.isDefined shouldBe true
        optDocument map { document =>
          (document \ testThrottlingRepository.throttleIdKey).asOpt[String] shouldBe Some(testThrottleIdOne)
          (document \ testThrottlingRepository.timecodeKey).asOpt[Long] shouldBe Some(testThrottleTimecode)
          (document \ testThrottlingRepository.countKey).asOpt[Int] shouldBe Some(1)
          (document \ testThrottlingRepository.lastUpdatedTimestampKey).isDefined shouldBe true
        }
      }
    }

  }
}
