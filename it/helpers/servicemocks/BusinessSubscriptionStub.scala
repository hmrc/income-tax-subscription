
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
