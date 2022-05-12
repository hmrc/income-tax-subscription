
package controllers.throttle

import config.MicroserviceAppConfig
import helpers.ComponentSpecBase
import play.api.http.Status._

class ThrottlingControllerISpec extends ComponentSpecBase {

  val appConfig: MicroserviceAppConfig = app.injector.instanceOf[MicroserviceAppConfig]


  "throttle" should {
    "succeed with a valid configured throttle" in {
      val res = IncomeTaxSubscription.throttled("testThrottle")

      res should have(
        httpStatus(OK)
      )
    }

    "fail with an invalid unconfigured throttle" in {
      val res = IncomeTaxSubscription.throttled("notATestThrottle")

      res should have(
        httpStatus(BAD_REQUEST)
      )
    }

    "fail with an valid configured throttle with a limit of zero" in {
      val res = IncomeTaxSubscription.throttled("zeroTestThrottle")

      res should have(
        httpStatus(SERVICE_UNAVAILABLE)
      )
    }

  }
}