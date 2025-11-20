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

package helpers.servicemocks

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import helpers.IntegrationTestConstants.Audit.{agentServiceEnrolmentName, agentServiceIdentifierKey}
import helpers.IntegrationTestConstants.testArn
import play.api.http.HeaderNames
import play.api.http.Status._
import play.api.libs.json.{JsObject, Json}

object AuthStub extends WireMockMethods {
  val authIDs = "/uri/to/ids"
  val authority = "/auth/authorise"

  val gatewayID = "12345"
  val internalID = "internal"
  val externalID = "external"


  def stubAuthSuccess(): StubMapping = {
    when(method = POST, uri = authority)
      .thenReturn(status = OK, body = successfulAuthResponse)
  }

  def stubAuth(status: Int): StubMapping = {
    when(method = POST, uri = authority)
      .thenReturn(status = status, body = successfulAuthResponse(arnEnrolment))
  }

  def stubAgentAuthSuccess(): StubMapping = {
    when(method = POST, uri = authority)
      .thenReturn(status = OK, body = successfulAuthResponse(arnEnrolment))
  }

  private val arnEnrolment = Json.obj(
    "key" -> agentServiceEnrolmentName,
    "identifiers" -> Json.arr(
      Json.obj(
        "key" -> agentServiceIdentifierKey,
        "value" -> testArn
      )
    )
  )

  private def successfulAuthResponse(enrolments: JsObject*): JsObject =
  //Written out manually as the json writer for Enrolment doesn't match the reader
    Json.obj(
      "allEnrolments" -> enrolments
    )

  private def exceptionHeaders(value: String) = Map(HeaderNames.WWW_AUTHENTICATE -> s"""MDTP detail="$value"""")

  def stubAuthFailure(): StubMapping = {
    when(method = POST, uri = authority)
      .thenReturn(status = UNAUTHORIZED, headers = exceptionHeaders("MissingBearerToken"))
  }

  val testCredId: String = "test-cred-id"

  val successfulAuthResponse: JsObject = {
    Json.obj(
      "allEnrolments" -> Json.arr()
    )
  }

}
