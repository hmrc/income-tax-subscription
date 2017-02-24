/*
 * Copyright 2017 HM Revenue & Customs
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

package unit.services.mocks

import audit.Logging
import config.AppConfig
import play.api.Configuration
import services.RosmAndEnrolManagerService
import uk.gov.hmrc.play.http.HttpGet
import unit.connectors.mocks.MockAuthenticatorConnector

trait MockSubscriptionManagerService extends MockRegistrationService with MockSubscriptionService with MockEnrolmentService with MockAuthenticatorConnector {

  override lazy val config = app.injector.instanceOf[Configuration]
  override lazy val appConfig = app.injector.instanceOf[AppConfig]
  override lazy val logging = app.injector.instanceOf[Logging]
  override lazy val httpPost = mockHttpPost
  override lazy val httpGet: HttpGet = mockHttpGet

  object TestSubscriptionManagerService extends RosmAndEnrolManagerService(
    logging,
    TestRegistrationService,
    TestSubscriptionService,
    TestEnrolmentService,
    TestAuthenticatorConnector
  )

}
