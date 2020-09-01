/*
 * Copyright 2020 HM Revenue & Customs
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

package connectors

import config.AppConfig
import connectors.mocks.MockHttp
import models.{SignUpFailure, SignUpResponse}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import parsers.SignUpParser._
import play.api.http.Status._
import play.api.libs.json.JsValue
import play.api.mvc.Request
import play.api.test.FakeRequest
import services.monitoring.AuditService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SignUpConnectorSpec extends UnitSpec with MockHttp with GuiceOneAppPerSuite {

  class Test(nino: String, response: PostSignUpResponse) {
    val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
    val auditService: AuditService = app.injector.instanceOf[AuditService]
    val connector = new SignUpConnector(mockHttpClient, appConfig, auditService)


    when(mockHttpClient.POST[JsValue, PostSignUpResponse](
      ArgumentMatchers.eq(s"${appConfig.desURL}/income-tax/sign-up/ITSA"),
      ArgumentMatchers.eq(connector.requestBody(nino)),
      ArgumentMatchers.any()
    )(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(response))
  }

  implicit val hc = HeaderCarrier()
  implicit val request: Request[_] = FakeRequest()

  "Calling sign up" when {

    "a valid PostSignUpResponse is returned" should {

      "return a mtdbsa" in new Test("AA111111A", Right(SignUpResponse("XAIT000000"))) {
        val result: PostSignUpResponse = await(connector.signUp("AA111111A"))

        result shouldBe Right(SignUpResponse("XAIT000000"))
      }
    }

    "a PostSignUpFailure is returned" should {

      "return a sign up failure with the correct status and message" in new Test("AA111111A", Left(SignUpFailure(INTERNAL_SERVER_ERROR, "Failure"))) {
        val result: PostSignUpResponse = await(connector.signUp("AA111111A"))

        result shouldBe Left(SignUpFailure(INTERNAL_SERVER_ERROR, "Failure"))
      }
    }
  }
}
