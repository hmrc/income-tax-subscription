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
import play.api.{Application, Environment, Mode}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits._

class DataMongoRepositorySpec extends UnitSpec with GuiceOneAppPerSuite with BeforeAndAfterEach {
  val TestDataMongoRepository = app.injector.instanceOf[DataMongoRepository]

  def servicesConfig: Map[String, String] = Map("mongodb.timeToLiveSeconds"-> "3600")

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .in(Environment.simple(mode = Mode.Dev))
    .configure(servicesConfig)
    .build()

  val testData = Json.obj("_id" -> "testId", "testDataKey" -> "testDataValue")
  override def beforeEach(): Unit = {
    await(TestDataMongoRepository.drop)
  }

  "storeData" should {
    "return the model when it is successfully inserted" in {
      val (insertRes, stored) = await(for {
        insertRes <- TestDataMongoRepository.insert(testData)
        stored <- TestDataMongoRepository.find("_id" -> "testId")
      } yield (insertRes, stored))

      stored.size shouldBe 1
      testData shouldBe stored.head

    }


    "fail when a duplicate is created" in {
      val res = for {
        _ <- TestDataMongoRepository.insert(testData)
        _ <- TestDataMongoRepository.insert(testData)
      } yield ()

      intercept[Exception](await(res))
    }
  }

}
