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

package helpers.servicemocks.hip

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import helpers.servicemocks.WireMockMethods
import models.subscription.AccountingPeriodUtil
import play.api.http.HeaderNames
import play.api.libs.json.JsValue

object GetITSAStatusStub extends WireMockMethods {

  def stub(utr: String)(status: Int, body: JsValue): StubMapping = {
    when(
      method = GET,
      uri = s"/itsd/person-itd/itsa-status/$utr\\?taxYear=${AccountingPeriodUtil.getCurrentTaxYear.toShortTaxYear}&futureYears=true",
      headers = Map(
        HeaderNames.AUTHORIZATION -> "Basic .*",
        "correlationId" -> "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"
      )
    ).thenReturn(status, body)
  }

  def stubHip(nino: String, utr: String)(status: Int, body: JsValue): StubMapping = {
    val url = s"/itsa-status/signup/$nino/$utr/${AccountingPeriodUtil.getCurrentTaxYear.toShortTaxYear}"
    println(s"Stub URL: $url")
    when(
      method = GET,
      uri = url,
      headers = Map(
        "correlationId" -> "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"
      )
    ).thenReturn(status, body)
  }

}

// /itsa-status/signup/AA000000A/1234567890/25-26
// /itsa-status/signup/AA000000A/1234567890/25-26