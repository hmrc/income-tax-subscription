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
import models.subscription.incomesource.BusinessIncomeModel
import play.api.libs.json.JsObject

object BusinessSubscriptionStub extends WireMockMethods {

  private def businessSubscriptionUri(nino: String): String = s"/income-tax-self-assessment/nino/$nino/business"

  def stubBusinessIncomeSubscription(nino: String, expectedBody: JsObject, authorizationHeader: String, environmentHeader: String)
                                    (status: Int, body: JsObject): StubMapping = {
    when(
      method = POST,
      uri = businessSubscriptionUri(nino),
      body = expectedBody,
      headers = Map[String, String](
        "Authorization" -> authorizationHeader,
        "Environment" -> environmentHeader
      )
    ).thenReturn(status, body)

  }

}
