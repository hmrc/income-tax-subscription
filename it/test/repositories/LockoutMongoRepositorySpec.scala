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

import helpers.ComponentSpecBase
import helpers.IntegrationTestConstants._
import models.matching.LockoutResponse
import org.scalatest.OptionValues
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import scala.concurrent.ExecutionContext.Implicits._

class LockoutMongoRepositorySpec extends ComponentSpecBase with AnyWordSpecLike
  with Matchers with OptionValues with DefaultPlayMongoRepositorySupport[JsObject] {
  lazy val testLockoutMongoRepository: LockoutMongoRepository = app.injector.instanceOf[LockoutMongoRepository]

  val repository: LockoutMongoRepository = testLockoutMongoRepository

  override def overriddenConfig(): Map[String, String] = Map("mongodb.uri" -> mongoUri)

  val timeoutSeconds = 10
  "lockoutAgent" should {
    "return the model when there is no lock" in {
      val result = for {
        insertRes <- testLockoutMongoRepository.lockoutAgent(testArn, timeoutSeconds)
        stored <- testLockoutMongoRepository.find(Json.obj(LockoutResponse.arn -> testArn), None)
      } yield (insertRes.get, stored)

      val r = await(result)

      val (insertRes, stored) = r
      stored.size shouldBe 1
      val head1 = stored.head
      val head = head1.as[LockoutResponse]
      insertRes.expiryTimestamp.toEpochMilli shouldBe head.expiryTimestamp.toEpochMilli
      insertRes.arn shouldBe head.arn
    }
  }

  "getLockoutStatus" should {
    "return None when there is no lock" in {
      val res = testLockoutMongoRepository.getLockoutStatus(testArn).futureValue
      res shouldBe empty
    }

    "return a LockModel if there is a lock" in {
      val lockOutModel = LockoutResponse(testArn, instant)

      val res = for {
        _ <- testLockoutMongoRepository.insert(Json.toJson(lockOutModel).as[JsObject])
        res <- testLockoutMongoRepository.getLockoutStatus(testArn)
      } yield res

      res.futureValue shouldBe Some(lockOutModel)
    }
  }
}
