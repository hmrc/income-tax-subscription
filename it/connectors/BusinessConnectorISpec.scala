
package connectors

import helpers.ComponentSpecBase
import helpers.IntegrationTestConstants.{testBusinessIncomeModel, testNino}
import helpers.servicemocks.BusinessSubscriptionStub.stubBusinessIncomeSubscription
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.http.Upstream5xxResponse

class BusinessConnectorISpec extends ComponentSpecBase {

  private lazy val BusinessConnector: BusinessConnector = app.injector.instanceOf[BusinessConnector]

  "business connector" when {
    "business subscription returns a successful response" should {
      "return businessSubscriptionPayload" in {
        stubBusinessIncomeSubscription(testNino, testBusinessIncomeModel)(OK, Json.obj())

        val res = BusinessConnector.businessSubscribe(testNino, testBusinessIncomeModel)

        await(res) shouldBe BusinessIncomeSubscriptionSuccess
      }
    }

    "business subscription errors " should {
      "throw an Exception where the nino number doesn't exist" in {
        stubBusinessIncomeSubscription(testNino, testBusinessIncomeModel)(INTERNAL_SERVER_ERROR, Json.obj())

        intercept[Upstream5xxResponse] {
          val res = BusinessConnector.businessSubscribe(testNino, testBusinessIncomeModel)
          await(res)
        }
      }
    }
  }
}


