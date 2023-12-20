/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

    "succeed when configured with a limit of one" in {
      val res = IncomeTaxSubscription.throttled("oneTestThrottle")

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