/*
 * Copyright 2025 HM Revenue & Customs
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
import helpers.IntegrationTestConstants
import helpers.IntegrationTestConstants.{SERVER_ERROR, SERVER_ERROR_MODEL, testMtditId, testNino}
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, NOT_FOUND, OK, UNPROCESSABLE_ENTITY}
import play.api.libs.json.{JsValue, Json}

object ITSABusinessDetailsStub extends WireMockMethods {

  val registrationResponse: JsValue = IntegrationTestConstants.GetITSABusinessDetailsResponse.successResponse(testNino, testMtditId)
  val invalidRegistrationResponse: JsValue = IntegrationTestConstants.GetITSABusinessDetailsResponse.invalidSuccessResponse

  def getITSABusinessDetailsUri(nino: String): String = s"/etmp/RESTAdapter/itsa/taxpayer/business-details\\?nino=$nino"

  def verifyGetITSABusinessDetails(): Unit = {
    verify(method = GET, uri = getITSABusinessDetailsUri(testNino))
  }
  def stubGetITSABusinessDetailsSuccess(): StubMapping = when(method = GET, uri = getITSABusinessDetailsUri(testNino))
    .thenReturn(status = OK, body = registrationResponse)

  def stubGetITSABusinessDetailsInvalidSuccess(): StubMapping = when(method = GET, uri = getITSABusinessDetailsUri(testNino))
    .thenReturn(status = OK, body = invalidRegistrationResponse)

  def stubGetITSABusinessDetailsNotFound(): StubMapping = when(method = GET, uri = getITSABusinessDetailsUri(testNino))
    .thenReturn(status = UNPROCESSABLE_ENTITY, body = Json.obj("errors" -> Json.obj("code" -> "008")))
  def stubGetITSABusinessDetailsInternalServerError(): StubMapping = when(method = GET, uri = getITSABusinessDetailsUri(testNino))
    .thenReturn(status = INTERNAL_SERVER_ERROR, body = Json.obj("reason" -> "Internal error"))

  def stubGetITSABusinessDetailsBadRequest(): StubMapping = when(method = GET, uri = getITSABusinessDetailsUri(testNino))
    .thenReturn(status = BAD_REQUEST, body = Json.obj("reason" -> "Bad request"))

}
