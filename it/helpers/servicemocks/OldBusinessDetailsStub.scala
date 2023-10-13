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
import connectors.OldBusinessDetailsConnector._
import helpers.IntegrationTestConstants
import helpers.IntegrationTestConstants._
import models.registration.GetBusinessDetailsFailureResponseModel
import play.api.http.Status._
import play.api.libs.json.JsValue

object OldBusinessDetailsStub extends WireMockMethods {
  val registrationResponse: JsValue = IntegrationTestConstants.GetBusinessDetailsResponse.successResponse(testNino, testSafeId, testMtditId)

  val errorReason = "Submission has not passed validation. Invalid parameter NINO."

  val getBusinessDetailsFailureResponse: GetBusinessDetailsFailureResponseModel = GetBusinessDetailsFailureResponseModel(
    code = Some("INVALID_NINO"),
    reason = errorReason
  )

  def verifyGetBusinessDetails(): Unit = {
    verify(method = GET, uri = getBusinessDetailsUri(testNino))
  }

  def stubGetBusinessDetailsSuccess(): StubMapping = when(method = GET, uri = getBusinessDetailsUri(testNino))
    .thenReturn(status = OK, body = registrationResponse)

  def stubGetBusinessDetailsFailure(): StubMapping =
    when(method = GET, uri = getBusinessDetailsUri(testNino))
      .thenReturn(
        status = BAD_REQUEST,
        body = getBusinessDetailsFailureResponse
      )
}
