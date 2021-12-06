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

package connectors.mocks

import common.CommonSpec
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.{HttpClient, HttpResponse}

import scala.concurrent.{ExecutionContext, Future}



trait MockHttp extends CommonSpec with MockitoSugar with BeforeAndAfterEach {

  val mockHttpClient = mock[HttpClient]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockHttpClient)
  }

  def setupMockHttpPost[I](url: Option[String] = None, body: Option[I] = None)(status: Int, response: JsValue): Unit = {
    lazy val urlMatcher = url.fold(ArgumentMatchers.any[String]())(x => ArgumentMatchers.eq(x))
    lazy val bodyMatcher = body.fold(ArgumentMatchers.any[I]())(x => ArgumentMatchers.eq(x))
    when(mockHttpClient.POST[I, HttpResponse](urlMatcher, bodyMatcher, ArgumentMatchers.any()
    )(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any[ExecutionContext])).thenReturn(Future.successful(HttpResponse(status, body = response.toString())))
  }

  def verifyHttpPost[I](url: Option[String] = None, body: Option[I] = None)(count: Int): Unit = {
    lazy val urlMatcher = url.fold(ArgumentMatchers.any[String]())(x => ArgumentMatchers.eq(x))
    lazy val bodyMatcher = body.fold(ArgumentMatchers.any[I]())(x => ArgumentMatchers.eq(x))
    verify(mockHttpClient, times(count)).POST[I, HttpResponse](urlMatcher, bodyMatcher, ArgumentMatchers.any()
    )(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any[ExecutionContext])
  }

  def setupMockHttpPostEmpty(url: Option[String] = None)(status: Int, response: JsValue): Unit = {
    lazy val urlMatcher = url.fold(ArgumentMatchers.any[String]())(x => ArgumentMatchers.eq(x))
    when(
      mockHttpClient.POSTEmpty[HttpResponse](urlMatcher)(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any[ExecutionContext])
    ).thenReturn(Future.successful(HttpResponse(status, body = response.toString())))
  }

  def verifyMockHttpPostEmpty(url: Option[String] = None)(count: Int): Unit = {
    lazy val urlMatcher = url.fold(ArgumentMatchers.any[String]())(x => ArgumentMatchers.eq(x))
    verify(mockHttpClient, times(count)).POSTEmpty[HttpResponse](urlMatcher)(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any[ExecutionContext])
  }

  def setupMockHttpGet(url: Option[String] = None, headers: Option[Seq[(String, String)]] = None)(status: Int, response: JsValue): Unit = {
    lazy val urlMatcher = url.fold(ArgumentMatchers.any[String]())(x => ArgumentMatchers.eq(x))
    lazy val headersMatcher = headers.fold(ArgumentMatchers.any[Seq[(String, String)]])(x => ArgumentMatchers.eq(x))

    when(
      mockHttpClient.GET[HttpResponse](urlMatcher, ArgumentMatchers.any() , headersMatcher)(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any[ExecutionContext])
    ).thenReturn(Future.successful(HttpResponse(status, body = response.toString())))
  }

  def setupMockHttpGetWithParams(url: Option[String], params: Option[Seq[(String, String)]])(status: Int, response: JsValue): Unit = {
    lazy val urlMatcher = url.fold(ArgumentMatchers.any[String]())(x => ArgumentMatchers.eq(x))
    lazy val paramsMatcher = params.fold(ArgumentMatchers.any[Seq[(String, String)]]())(x => ArgumentMatchers.eq(x))
    when(
      mockHttpClient.GET[HttpResponse](urlMatcher, paramsMatcher)(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any[ExecutionContext])
    ).thenReturn(Future.successful(HttpResponse(status, body = response.toString())))
  }
}
