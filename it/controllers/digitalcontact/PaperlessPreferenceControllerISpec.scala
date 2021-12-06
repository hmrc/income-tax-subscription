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

package controllers.digitalcontact

import helpers.ComponentSpecBase
import helpers.IntegrationTestConstants._
import helpers.servicemocks.AuthStub
import models.digitalcontact.PaperlessPreferenceKey
import org.scalatest.BeforeAndAfterEach
import play.api.http.Status
import play.api.libs.json.Json
import repositories.digitalcontact.PaperlessPreferenceMongoRepository

import scala.concurrent.ExecutionContext.Implicits.global

class PaperlessPreferenceControllerISpec extends ComponentSpecBase with BeforeAndAfterEach {
  val paperlessPreferenceRepository = app.injector.instanceOf[PaperlessPreferenceMongoRepository]

  override def beforeEach(): Unit = {
    super.beforeEach()
    paperlessPreferenceRepository.drop.futureValue
  }

  s"POST /identifier-mapping/$testPreferencesToken" should {
    "store the NINO successfully and return OK" in {
      Given("I setup the wiremock stubs")
      AuthStub.stubAuthSuccess()

      When("I call the store nino endpoint")
      val res = IncomeTaxSubscription.storeNino(testPreferencesToken, testNino)

      Then("The result status should be OK")
      res should have(
        httpStatus(Status.CREATED)
      )

      Then("The database should contain the inserted NINO")
      val dbRes = paperlessPreferenceRepository.find("_id" -> testPreferencesToken).futureValue.headOption

      dbRes should contain(PaperlessPreferenceKey(testPreferencesToken, testNino))
    }
  }

  s"GET /identifier-mapping/$testPreferencesToken" should {
    "GET the NINO successfully and return OK" in {
      Given("I setup the wiremock stubs")
      AuthStub.stubAuthSuccess()
      IncomeTaxSubscription.storeNino(testPreferencesToken, testNino)

      When("I call the get nino endpoint")
      val res = IncomeTaxSubscription.getNino(testPreferencesToken)

      Then("The result status should be OK")
      res should have(
        httpStatus(Status.OK),
        jsonBodyAs(
          Json.obj(
            "identifiers" ->
              List(
                Json.obj(
                  "name" -> "nino",
                  "value" -> s"$testNino"
                )
              )
          )
        )
      )
    }
  }
}
