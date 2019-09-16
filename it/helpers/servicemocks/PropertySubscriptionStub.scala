
package helpers.servicemocks

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import models.subscription.incomesource.PropertyIncomeModel
import play.api.libs.json.JsObject

object PropertySubscriptionStub extends WireMockMethods{

  private def propertySubscriptionUri(nino: String): String = s"/income-tax-self-assessment/nino/$nino/property"

  def stubPropertyIncomeSubscription(nino: String)(status: Int, body: JsObject): StubMapping = {
    when(method = POST, uri = propertySubscriptionUri(nino))
      .thenReturn(status, body)
  }

}