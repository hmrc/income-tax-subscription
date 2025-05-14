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
import uk.gov.hmrc.http.HeaderNames

object CreateIncomeSourceStub extends WireMockMethods {

  private def createBusinessIncomeUrl(mtdbsaRef: String): String = s"/income-tax/income-sources/mtdbsa/$mtdbsaRef/ITSA/business"

  def stub(mtdbsaRef: String, expectedBody: JsValue, authorizationHeader: String, environmentHeader: String)
          (status: Int, body: JsValue): StubMapping = {
    when(
      method = POST,
      uri = createBusinessIncomeUrl(mtdbsaRef),
      body = expectedBody,
      headers = Map[String, String](
        "Authorization" -> authorizationHeader,
        "Environment" -> environmentHeader
      )
    ).thenReturn(status, body)
  }

  def stubItsaIncomeSource(expectedBody: JsValue)(status: Int, body: JsValue): StubMapping = {
    when(
      method = POST,
      uri = "/etmp/RESTAdapter/itsa/taxpayer/income-source",
      body = expectedBody,
      headers = Map(
        HeaderNames.authorisation -> "Bearer .*",
        "correlationId" -> "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$",
        "X-Message-Type" -> "CreateIncomeSource",
        "X-Originating-System" -> "MDTP",
        "X-Receipt-Date" -> "^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2})Z$",
        "X-Regime" -> "ITSA",
        "X-Transmitting-System" -> "HIP"
      )
    ).thenReturn(status, body)
  }
}
