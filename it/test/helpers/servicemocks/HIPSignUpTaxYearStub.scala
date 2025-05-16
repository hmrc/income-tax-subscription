/*
 * Copyright 2020 HM Revenue & Customs
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
import play.api.libs.json.JsValue

import java.time.Instant
import java.util.UUID

object HIPSignUpTaxYearStub extends WireMockMethods {

  private def signUpUri: String = s"/etmp/RESTAdapter/itsa/taxpayer/signup-mtdfb"

  def stubSignUp(expectedBody: JsValue, authorizationHeader: String)
                (status: Int, body: JsValue): StubMapping = {
    when(
      method = POST,
      uri = signUpUri,
      body = expectedBody,
      headers = Map[String, String](
        "Authorization" -> authorizationHeader,
        "correlationid" -> "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$",
        "X-Message-Type" -> "ITSASignUpMTDfB",
        "X-Originating-System" -> "MDTP",
        "X-Receipt-Date" -> """^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}Z$""",
        "X-Regime-Type" -> "ITSA",
        "X-Transmitting-System" -> "HIP"
      )
    ).thenReturn(status, body)

  }
}
