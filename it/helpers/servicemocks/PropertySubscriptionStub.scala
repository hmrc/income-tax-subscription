
package helpers.servicemocks

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.libs.json.JsObject

object PropertySubscriptionStub extends WireMockMethods {

  private def propertySubscriptionUri(nino: String): String = s"/income-tax-self-assessment/nino/$nino/properties"

  def stubPropertyIncomeSubscription(nino: String, expectedBody: JsObject)(status: Int, body: JsObject): StubMapping = {
    when(method = POST, uri = propertySubscriptionUri(nino), body = expectedBody)
      .thenReturn(status, body)
  }



}