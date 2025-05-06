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

package services.mocks

import config.AppConfig
import connectors.HIPSignUpTaxYearConnector
import connectors.mocks.MockHttp
import models.SignUpRequest
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.{reset, when}
import org.scalatest.Suite
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import parsers.SignUpParser.PostSignUpResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait MockHIPSignUpTaxYearConnector extends MockHttp with GuiceOneAppPerSuite {
  this: Suite =>

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockHIPSignUpTaxYearConnector)
  }

  val mockHIPSignUpTaxYearConnector: HIPSignUpTaxYearConnector = mock[HIPSignUpTaxYearConnector]
  val appConfig: AppConfig
  val connector = new HIPSignUpTaxYearConnector(mockHttpClient, appConfig)


  def hipSignUpTaxYear(signUpRequest: SignUpRequest)(response: Future[PostSignUpResponse]): Unit = {
    when(mockHIPSignUpTaxYearConnector.signUp(
      ArgumentMatchers.eq(signUpRequest),
    )(ArgumentMatchers.any())).thenReturn(response)
  }
}
