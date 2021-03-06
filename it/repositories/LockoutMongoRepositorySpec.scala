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

import helpers.IntegrationTestConstants._
import models.matching.LockoutResponse
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.modules.reactivemongo.ReactiveMongoComponent
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits._

class LockoutMongoRepositorySpec extends UnitSpec with GuiceOneAppPerSuite with BeforeAndAfterEach {
  implicit lazy val mongo = app.injector.instanceOf[ReactiveMongoComponent]

  object TestLockoutMongoRepository extends LockoutMongoRepository

  override def beforeEach(): Unit = {
    await(TestLockoutMongoRepository.drop)
  }

  "lockoutAgent" should {
    "return the model when there is no lock" in {
      val (insertRes, stored) = await(for {
        insertRes <- TestLockoutMongoRepository.lockoutAgent(testArn, 10)
        stored <- TestLockoutMongoRepository.find(LockoutResponse.arn -> testArn)
      } yield (insertRes.get, stored))

      stored.size shouldBe 1
      insertRes shouldBe stored.head
    }
  }

  "getLockoutStatus" should {
    "return None when there is no lock" in {
      val res = await(TestLockoutMongoRepository.getLockoutStatus(testArn))
      res shouldBe empty
    }

    "return a LockModel if there is a lock" in {
      val lockOutModel = LockoutResponse(testArn, offsetDateTime)

      val res = await(for {
        _ <- TestLockoutMongoRepository.insert(lockOutModel)
        res <- TestLockoutMongoRepository.getLockoutStatus(testArn)
      } yield res)

      res shouldBe Some(lockOutModel)
    }
  }
}
