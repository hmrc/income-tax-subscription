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

import models.DataModel
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

  val testData = Json.obj("testDataKey" -> "testDataValue")

  val testDataModel1 = new DataModel("session-id", "data-id-1", testData)
  val testDataModel2 = new DataModel("session-id", "data-id-2", testData)
  val testDataModel3 = new DataModel("session-id", "data-id-3", testData)

  override def beforeEach(): Unit = {
    await(TestDataMongoRepository.drop)
  }

  "storeData" should {
    "return the model when it is successfully inserted by dataId and sessionId" in {
      val (retrieveOnDataId, retrieveOnSessionId) = await(for {
        insertRes1 <- TestDataMongoRepository.insert(testDataModel1)
        insertRes2 <- TestDataMongoRepository.insert(testDataModel2)
        insertRes3 <- TestDataMongoRepository.insert(testDataModel3)
        retrieveOnSessionId <- TestDataMongoRepository.findAllBySessionId("session-id")
        retrieveOnDataId <- TestDataMongoRepository.findByDataId("data-id-2")
      } yield (retrieveOnDataId, retrieveOnSessionId))

      retrieveOnSessionId.size shouldBe 3
      retrieveOnSessionId shouldBe List(testDataModel1, testDataModel2, testDataModel3)
      retrieveOnDataId.get shouldBe testDataModel2
    }
  }
}
