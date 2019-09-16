
package connectors

import helpers.ComponentSpecBase
import helpers.IntegrationTestConstants.testNino
import helpers.servicemocks.PropertySubscriptionStub.stubPropertyIncomeSubscription
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.Json
import uk.gov.hmrc.http.Upstream5xxResponse

class PropertyConnectorISpec extends ComponentSpecBase {

    private lazy val PropertyConnector: PropertyConnector = app.injector.instanceOf[PropertyConnector]

    "property connector" when {
      "property subscription returns a successful response" should {
        "return propertySubscriptionPayload" in {
          stubPropertyIncomeSubscription(testNino)(OK, Json.obj())

          val res = PropertyConnector.propertySubscribe(testNino)

          await(res) shouldBe PropertyIncomeSubscriptionSuccess
        }
      }

      "property subscription errors " should {
        "throw an exception where the nino number doesn't exist" in {
          stubPropertyIncomeSubscription(testNino)(INTERNAL_SERVER_ERROR, Json.obj())

          intercept[Upstream5xxResponse] {
            val res = PropertyConnector.propertySubscribe(testNino)
            await(res)
          }
        }
      }
    }
  }
