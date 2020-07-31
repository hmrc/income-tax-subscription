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

package controllers

import helpers.ComponentSpecBase
import helpers.servicemocks.AuthStub
import play.api.http.Status._
import play.api.libs.json.{JsObject, Json}
import repositories.SubscriptionDataRepository

import scala.concurrent.ExecutionContext.Implicits.global

class SubscriptionDataControllerISpec extends ComponentSpecBase {

  val repository: SubscriptionDataRepository = app.injector.instanceOf[SubscriptionDataRepository]
  val testJson: JsObject = Json.obj("testDataIdKey" -> "testDataIdValue")
  val testDocument: JsObject = Json.obj(
    "sessionId" -> "testSessionId",
    "testDataId" -> Json.obj(
      "testDataIdKey" -> "testDataIdValue",
      "testDataIdKey2" -> 1
    )
  )

  val testDocumentAll: JsObject = Json.obj(
    "sessionId" -> "testSessionId",
    "testDataId" -> Json.obj(
      "testDataIdKey" -> "testDataIdValue",
      "testDataIdKey2" -> 1
    ),
    "testDataId2" -> Json.obj(
      "testDataId2Key" -> "testDataId2Value",
      "testDataId2Key2" -> 2
    )
  )

  override def beforeEach(): Unit = {
    await(repository.drop)
    super.beforeEach()
  }

  s"GET ${controllers.routes.SubscriptionDataController.getAllSelfEmployments().url}" should {
    "return OK with all the data related to the user in mongo" when {
      "the sessionId exists in mongo for the user" in {
        AuthStub.stubAuthSuccess()
        await(repository.insert(testDocumentAll))

        IncomeTaxSubscription.getAllSelfEmployments should have(
          httpStatus(OK),
          jsonBodyOf(Json.obj(
            "sessionId" -> "testSessionId",
            "testDataId" -> Json.obj(
              "testDataIdKey" -> "testDataIdValue",
              "testDataIdKey2" -> 1
            ),
            "testDataId2" -> Json.obj(
              "testDataId2Key" -> "testDataId2Value",
              "testDataId2Key2" -> 2
            )
          ))
        )
      }
    }
    "return NO_CONTENT" when {
      "the user's sessionId could not be found in mongo" in {
        AuthStub.stubAuthSuccess()

        IncomeTaxSubscription.getAllSelfEmployments should have(
          httpStatus(NO_CONTENT),
          emptyBody
        )
      }
    }
    "return unauthorised" when {
      "the user is not authorised" in {
        AuthStub.stubAuthFailure()

        IncomeTaxSubscription.getAllSelfEmployments should have(
          httpStatus(UNAUTHORIZED)
        )
      }
    }
  }

  s"GET ${controllers.routes.SubscriptionDataController.retrieveSelfEmployments("testDataId").url}" should {
    "return OK with the data related to the key in mongo" when {
      "the data exists in mongo for the user" in {
        AuthStub.stubAuthSuccess()
        await(repository.insert(testDocument))

        IncomeTaxSubscription.getRetrieveSelfEmployments("testDataId") should have(
          httpStatus(OK),
          jsonBodyOf(Json.obj(
            "testDataIdKey" -> "testDataIdValue",
            "testDataIdKey2" -> 1
          ))
        )
      }
    }
    "return NO_CONTENT" when {
      "the data could not be retrieved from mongo" in {
        AuthStub.stubAuthSuccess()

        IncomeTaxSubscription.getRetrieveSelfEmployments("testDataId") should have(
          httpStatus(NO_CONTENT),
          emptyBody
        )
      }
    }
    "return unauthorised" when {
      "the user is not authorised" in {
        AuthStub.stubAuthFailure()

        IncomeTaxSubscription.getRetrieveSelfEmployments("testDataId") should have(
          httpStatus(UNAUTHORIZED)
        )
      }
    }
  }

  s"POST ${controllers.routes.SubscriptionDataController.insertSelfEmployments("testDataId").url}" should {
    "return OK to upsert the data in mongo" when {
      "the session document already existed for the user" in {
        AuthStub.stubAuthSuccess()
        await(repository.insert(testDocument))

        IncomeTaxSubscription.postInsertSelfEmployments("testDataId", testJson) should have(
          httpStatus(OK)
        )
      }
      "the session document did not exist for the user" in {
        AuthStub.stubAuthSuccess()

        IncomeTaxSubscription.postInsertSelfEmployments("testDataId", testJson) should have(
          httpStatus(OK)
        )
      }
      "the data being stored is simple values" in {
        AuthStub.stubAuthSuccess()

        IncomeTaxSubscription.postInsertSelfEmployments("testDataId", Json.toJson("testValue")) should have(
          httpStatus(OK)
        )
      }
    }

    "return unauthorised" when {
      "the user is not authorised" in {
        AuthStub.stubAuthFailure()

        IncomeTaxSubscription.postInsertSelfEmployments("testDataId", testJson) should have(
          httpStatus(UNAUTHORIZED)
        )
      }
    }
  }

    s"DELETE ${controllers.routes.SubscriptionDataController.deleteAllSessionData().url}" should {
    "return OK remove all the data related to the user in mongo" when {
      "the sessionId exists in mongo for the user" in {
        AuthStub.stubAuthSuccess()
        await(repository.insert(testDocumentAll))
        IncomeTaxSubscription.deleteDeleteAllSessionData should have(httpStatus(OK))
      }
    }

    "return unauthorised" when {
      "the user is not authorised" in {
        AuthStub.stubAuthFailure()

        IncomeTaxSubscription.deleteDeleteAllSessionData should have(
          httpStatus(UNAUTHORIZED)
        )
      }
    }
  }

}
