
package connectors

import config.MicroserviceAppConfig
import helpers.ComponentSpecBase
import helpers.IntegrationTestConstants._
import helpers.servicemocks.PropertySubscriptionStub.stubPropertyIncomeSubscription
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.Json
import play.api.mvc.Request
import play.api.test.FakeRequest
import uk.gov.hmrc.http.InternalServerException

class PropertyConnectorISpec extends ComponentSpecBase {

  private lazy val propertyConnector: PropertyConnector = app.injector.instanceOf[PropertyConnector]
  private lazy val appConfig: MicroserviceAppConfig = app.injector.instanceOf[MicroserviceAppConfig]
  implicit val request: Request[_] = FakeRequest()

  "property connector" when {
    "property subscription returns a successful response" should {
      "return propertySubscriptionPayload" in {
        stubPropertyIncomeSubscription(testNino, testPropertyIncomeCash, appConfig.desAuthorisationToken, appConfig.desEnvironment)(
          OK, Json.obj("mtditId" -> testMtditId)
        )

        val res = propertyConnector.propertySubscribe(testNino, testPropertyIncomeModel, Some(testArn))

        await(res) shouldBe testMtditId
      }
    }

    "property subscription errors " should {
      "throw an exception where the nino number doesn't exist" in {
        stubPropertyIncomeSubscription(testNino, testPropertyIncomeCash, appConfig.desAuthorisationToken, appConfig.desEnvironment)(
          INTERNAL_SERVER_ERROR, Json.obj()
        )

        intercept[InternalServerException] {
          val res = propertyConnector.propertySubscribe(testNino, testPropertyIncomeModel, Some(testArn))
          await(res)
        }
      }
    }
  }
}
