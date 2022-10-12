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

import config.AppConfig
import config.featureswitch.FeatureSwitching
import helpers.ComponentSpecBase
import helpers.servicemocks.AuthStub
import play.api.http.Status._
import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import repositories.SubscriptionDataRepository

class SubscriptionDataControllerISpec extends ComponentSpecBase with FeatureSwitching {

  val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  val repository: SubscriptionDataRepository = app.injector.instanceOf[SubscriptionDataRepository]

  val reference: String = "test-reference"
  val utr: String = "1234567890"

  val testJson: JsObject = Json.obj("testDataIdKey" -> "testDataIdValue")
  val testDocument: JsObject = Json.obj(
    "sessionId" -> "testSessionId",
    "reference" -> reference,
    "testDataId" -> Json.obj(
      "testDataIdKey" -> "testDataIdValue",
      "testDataIdKey2" -> 1
    )
  )

  val testDocumentAll: JsObject = Json.obj(
    "sessionId" -> "testSessionId",
    "reference" -> reference,
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
    await(repository.drop())
    super.beforeEach()
  }

  s"POST ${controllers.routes.SubscriptionDataController.retrieveReference.url}" should {
    "return OK with a reference" when {
      "it already exists in the database" in {
        AuthStub.stubAuthSuccess()
        await(repository.insert(Json.obj(
          "reference" -> reference,
          "utr" -> utr,
          "credId" -> "test-cred-id"
        )))

        IncomeTaxSubscription.postRetrieveReference(utr) should have(
          httpStatus(OK),
          jsonBodyOf(Json.obj(
            "reference" -> reference
          ))
        )
      }
    }
    "return CREATED with a reference" when {
      "it is not already exists in the database" in {
        AuthStub.stubAuthSuccess()

        IncomeTaxSubscription.postRetrieveReference(utr) should have(
          httpStatus(CREATED),
          jsonBodyContainsField("reference")
        )
      }
    }
    "return UNAUTHORISED" when {
      "the user is not authorised" in {
        AuthStub.stubAuthFailure()

        IncomeTaxSubscription.postUnauthorisedRetrieveReference(utr) should have(
          httpStatus(UNAUTHORIZED)
        )
      }
    }
  }

  s"GET ${controllers.routes.SubscriptionDataController.getAllSubscriptionData(reference).url}" should {
    "return OK with all the data related to the user in mongo" when {
      "the sessionId exists in mongo for the user" in {

        AuthStub.stubAuthSuccess()
        await(repository.insert(testDocumentAll))

        IncomeTaxSubscription.getAllSelfEmployments(reference) should have(
          httpStatus(OK),
          jsonBodyOf(Json.obj(
            "sessionId" -> "testSessionId",
            "reference" -> reference,
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

        IncomeTaxSubscription.getAllSelfEmployments(reference) should have(
          httpStatus(NO_CONTENT),
          emptyBody
        )
      }
    }
    "return unauthorised" when {
      "the user is not authorised" in {

        AuthStub.stubAuthFailure()

        IncomeTaxSubscription.getAllSelfEmployments(reference) should have(
          httpStatus(UNAUTHORIZED)
        )
      }
    }
  }

  s"GET ${controllers.routes.SubscriptionDataController.retrieveSubscriptionData(reference, "testDataId").url}" should {
    "return OK with the data related to the key in mongo" when {
      "the data exists in mongo for the user" in {

        AuthStub.stubAuthSuccess()
        await(repository.insert(testDocument))

        IncomeTaxSubscription.getRetrieveSelfEmployments(reference, "testDataId") should have(
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

        IncomeTaxSubscription.getRetrieveSelfEmployments(reference, "testDataId") should have(
          httpStatus(NO_CONTENT),
          emptyBody
        )
      }
    }
    "return unauthorised" when {
      "the user is not authorised" in {

        AuthStub.stubAuthFailure()

        IncomeTaxSubscription.getRetrieveSelfEmployments(reference, "testDataId") should have(
          httpStatus(UNAUTHORIZED)
        )
      }
    }
  }

  s"POST ${controllers.routes.SubscriptionDataController.insertSubscriptionData(reference, "testDataId").url}" should {
    "return OK to upsert the data in mongo" when {
      "the session document already existed for the user" in {

        AuthStub.stubAuthSuccess()
        await(repository.insert(testDocument))

        IncomeTaxSubscription.postInsertSelfEmployments(reference, "testDataId", testJson) should have(
          httpStatus(OK)
        )
      }
      "the session document did not exist for the user" in {

        AuthStub.stubAuthSuccess()

        IncomeTaxSubscription.postInsertSelfEmployments(reference, "testDataId", testJson) should have(
          httpStatus(OK)
        )
      }
      "the data being stored is simple values" in {

        AuthStub.stubAuthSuccess()

        IncomeTaxSubscription.postInsertSelfEmployments(reference, "testDataId", Json.toJson("testValue")) should have(
          httpStatus(OK)
        )
      }
    }

    "return unauthorised" when {
      "the user is not authorised" in {

        AuthStub.stubAuthFailure()

        val res = IncomeTaxSubscription.postInsertSelfEmployments(reference, "testDataId", testJson)

        res should have(
          httpStatus(UNAUTHORIZED)
        )
      }
    }
  }

  s"DELETE ${controllers.routes.SubscriptionDataController.deleteSubscriptionData(reference, "testDataId").url}" should {
    "return OK and remove select data related to the user in mongo" when {
      "the sessionId exists in mongo for the user" in {
        AuthStub.stubAuthSuccess()
        await(repository.insert(testDocumentAll))
        IncomeTaxSubscription.deleteSubscriptionData(reference, id = "testDataId") should have(httpStatus(OK))
      }
    }
  }


  s"DELETE ${controllers.routes.SubscriptionDataController.deleteAllSubscriptionData(reference).url}" should {
    "return OK remove all the data related to the user in mongo" when {
      "the sessionId exists in mongo for the user" in {

        AuthStub.stubAuthSuccess()
        await(repository.insert(testDocumentAll))
        IncomeTaxSubscription.deleteDeleteAllSessionData(reference) should have(httpStatus(OK))
      }
    }

    "return unauthorised" when {
      "the user is not authorised" in {

        AuthStub.stubAuthFailure()

        IncomeTaxSubscription.deleteDeleteAllSessionData(reference) should have(
          httpStatus(UNAUTHORIZED)
        )
      }
    }
  }
}
