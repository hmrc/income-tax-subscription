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

object PrePopStub extends WireMockMethods {

  private def prePopUri(nino: String): String = s"/income-tax/pre-prop/$nino"

  private def hipPrePopUri(nino: String): String =
    s"/cesa/prepopulation/businessdata/$nino"

  def stubPrePop(nino: String)
                (authorizationHeader: String, environmentHeader: String)
                (status: Int, body: JsValue): StubMapping = {
    when(
      method = GET,
      uri = prePopUri(nino),
      headers = Map[String, String](
        "Authorization" -> authorizationHeader,
        "Environment" -> environmentHeader
      )
    ).thenReturn(status, body)

  }

  def stubHipPrePop
    (nino: String)
    (status: Int, body: JsValue): StubMapping =
  {
    when(
      method = GET,
      uri = hipPrePopUri(nino),
      headers = Map[String, String](
        "correlationid" -> "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$",
      )
    ).thenReturn(status, body)

  }
}
