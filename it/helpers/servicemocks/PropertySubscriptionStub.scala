
package helpers.servicemocks

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import models.subscription.incomesource.PropertyIncomeModel
import play.api.libs.json.JsObject

object PropertySubscriptionStub extends WireMockMethods {

  private def propertySubscriptionUri(nino: String): String = s"/income-tax-self-assessment/nino/$nino/properties"

  def stubPropertyIncomeSubscription(nino: String, propertyIncomeModel: PropertyIncomeModel)(status: Int, body: JsObject): StubMapping = {
    when(method = POST, uri = propertySubscriptionUri(nino), body = PropertyIncomeModel.writeToDes(propertyIncomeModel))
      .thenReturn(status, body)
  }

}