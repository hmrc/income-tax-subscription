/*
 * Copyright 2021 HM Revenue & Customs
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
import connectors.SignUpConnector
import connectors.mocks.MockHttp
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.{reset, when}
import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import parsers.SignUpParser.PostSignUpResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait MockSignUpConnector extends BeforeAndAfterEach with MockHttp with GuiceOneAppPerSuite {
  this: Suite =>

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSignUpConnector)
  }
  val mockSignUpConnector: SignUpConnector = mock[SignUpConnector]
  val appConfig: AppConfig = app.injector.instanceOf[AppConfig]
  val connector = new SignUpConnector(mockHttpClient, appConfig)


  def signUp(nino: String)(response: Future[PostSignUpResponse])(): Unit = {
    when(mockSignUpConnector.signUp(ArgumentMatchers.eq(nino))(ArgumentMatchers.any())).thenReturn(response)

  }
}
