
package helpers.servicemocks

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import models.subscription.incomesource.BusinessIncomeModel
import play.api.libs.json.JsObject

object BusinessSubscriptionStub extends WireMockMethods {

  private def businessSubscriptionUri(nino: String): String = s"/income-tax-self-assessment/nino/$nino/business"

  def stubBusinessIncomeSubscription(nino: String, businessIncomeModel: BusinessIncomeModel)(status: Int, body: JsObject): StubMapping = {
    when(method = POST, uri = businessSubscriptionUri(nino), body = BusinessIncomeModel.writeToDes(businessIncomeModel))
      .thenReturn(status, body)
  }

}
