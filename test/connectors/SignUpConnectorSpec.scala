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

package connectors

import common.CommonSpec
import config.AppConfig
import connectors.mocks.MockHttp
import models.{ErrorModel, SignUpResponse}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import parsers.SignUpParser._
import play.api.http.Status._
import play.api.libs.json.JsValue
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SignUpConnectorSpec extends CommonSpec with MockHttp with GuiceOneAppPerSuite {

  class Test(nino: String, response: PostSignUpResponse) {
    val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
    val connector = new SignUpConnector(mockHttpClient, appConfig)
    val headers: Seq[(String, String)] = Seq(
      HeaderNames.authorisation -> appConfig.desAuthorisationToken,
      appConfig.desEnvironmentHeader
    )

    when(mockHttpClient.POST[JsValue, PostSignUpResponse](
      ArgumentMatchers.eq(s"${appConfig.desURL}/income-tax/sign-up/ITSA"),
      ArgumentMatchers.eq(connector.requestBody(nino)),
      ArgumentMatchers.eq(headers)
    )(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(response))
  }

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val request: Request[_] = FakeRequest()

  "Calling sign up" when {

    "a valid PostSignUpResponse is returned" should {

      "return a mtdbsa" in new Test("AA111111A", Right(SignUpResponse("XAIT000000"))) {
        val result: PostSignUpResponse = await(connector.signUp("AA111111A"))

        result shouldBe Right(SignUpResponse("XAIT000000"))
      }
    }

    "a PostSignUpFailure is returned" should {

      "return a sign up failure with the correct status and message" in new Test("AA111111A", Left(ErrorModel(INTERNAL_SERVER_ERROR, "Failure"))) {
        val result: PostSignUpResponse = await(connector.signUp("AA111111A"))

        result shouldBe Left(ErrorModel(INTERNAL_SERVER_ERROR, "Failure"))
      }
    }
  }
}
