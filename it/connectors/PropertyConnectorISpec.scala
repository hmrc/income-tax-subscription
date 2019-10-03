
package connectors

import helpers.ComponentSpecBase
import helpers.IntegrationTestConstants._
import helpers.servicemocks.PropertySubscriptionStub.stubPropertyIncomeSubscription
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.Json
import uk.gov.hmrc.http.InternalServerException

class PropertyConnectorISpec extends ComponentSpecBase {

  private lazy val PropertyConnector: PropertyConnector = app.injector.instanceOf[PropertyConnector]

  "property connector" when {
    "property subscription returns a successful response" should {
      "return propertySubscriptionPayload" in {
        stubPropertyIncomeSubscription(testNino, testPropertyIncomeCash)(OK, Json.obj(
          "mtditId" -> testMtditId))

        val res = PropertyConnector.propertySubscribe(testNino, testPropertyIncomeModel)

        await(res) shouldBe testMtditId
      }
    }

    "property subscription errors " should {
      "throw an exception where the nino number doesn't exist" in {
        stubPropertyIncomeSubscription(testNino, testPropertyIncomeCash)(INTERNAL_SERVER_ERROR, Json.obj())

        intercept[InternalServerException] {
          val res = PropertyConnector.propertySubscribe(testNino, testPropertyIncomeModel)
          await(res)
        }
      }
    }
  }
}
