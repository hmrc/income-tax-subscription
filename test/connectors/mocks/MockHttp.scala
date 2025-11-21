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

package connectors.mocks

import common.CommonSpec
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import uk.gov.hmrc.http.client.HttpClientV2

import scala.concurrent.{ExecutionContext, Future}

trait MockHttp extends CommonSpec with MockitoSugar with BeforeAndAfterEach {

  lazy implicit val hc: HeaderCarrier=HeaderCarrier()
  val mockHttpClient: HttpClientV2 = mock[HttpClientV2]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockHttpClient)
  }
  
  def setupMockHttpPost[I](url: Option[String] = None, body: Option[I] = None)(status: Int, response: JsValue): Unit = {
    lazy val urlMatcher = url.fold(ArgumentMatchers.any[String]())(x => ArgumentMatchers.eq(x))
    lazy val bodyMatcher = body.fold(ArgumentMatchers.any[I]())(x => ArgumentMatchers.eq(x))
    when(
      mockHttpClient
        .post(url"$urlMatcher")
        .withBody(bodyMatcher)(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())
        .execute[HttpResponse](ArgumentMatchers.any(), ArgumentMatchers.any())
    ).thenReturn(Future.successful(HttpResponse(status, body = response.toString())))
  }

  def verifyHttpPost[I](url: Option[String] = None, body: Option[I] = None)(count: Int): Unit = {
    lazy val urlMatcher = url.fold(ArgumentMatchers.any[String]())(x => ArgumentMatchers.eq(x))
    lazy val bodyMatcher = body.fold(ArgumentMatchers.any[I]())(x => ArgumentMatchers.eq(x))
    verify(mockHttpClient, times(count)).post(url"$urlMatcher")
      .withBody(bodyMatcher)(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())
      .execute[HttpResponse](ArgumentMatchers.any(), ArgumentMatchers.any())
  }

  def setupMockHttpPostEmpty(url: Option[String] = None)(status: Int, response: JsValue): Unit = {
    lazy val urlMatcher = url.fold(ArgumentMatchers.any[String]())(x => ArgumentMatchers.eq(x))
    when(
      mockHttpClient
        .post(url"$urlMatcher")
        .execute[HttpResponse](ArgumentMatchers.any(), ArgumentMatchers.any())
    ).thenReturn(Future.successful(HttpResponse(status, body = response.toString())))
  }

  def verifyMockHttpPostEmpty(url: Option[String] = None)(count: Int): Unit = {
    lazy val urlMatcher = url.fold(ArgumentMatchers.any[String]())(x => ArgumentMatchers.eq(x))
    verify(mockHttpClient, times(count))
      .post(url"$urlMatcher")
      .execute[HttpResponse](ArgumentMatchers.any(), ArgumentMatchers.any())
  }

  def setupMockHttpGet(url: Option[String] = None, headers: Option[Seq[(String, String)]] = None)(status: Int, response: JsValue): Unit = {
    lazy val urlMatcher = url.fold(ArgumentMatchers.any[String]())(x => ArgumentMatchers.eq(x))
    lazy val headersMatcher = headers.fold(ArgumentMatchers.any[Seq[(String, String)]])(x => ArgumentMatchers.eq(x))
    when(
      mockHttpClient
        .get(url"$urlMatcher")
        .execute[HttpResponse](ArgumentMatchers.any(), ArgumentMatchers.any())
    ).thenReturn(Future.successful(HttpResponse(status, body = response.toString())))
  }

  def setupMockHttpGetWithParams(url: Option[String], params: Option[Seq[(String, String)]])(status: Int, response: JsValue): Unit = {
    lazy val urlMatcher = url.fold(ArgumentMatchers.any[String]())(x => ArgumentMatchers.eq(x))
    lazy val paramsMatcher = params.fold(ArgumentMatchers.any[Seq[(String, String)]]())(x => ArgumentMatchers.eq(x))
    when(
      mockHttpClient
        .get(url"$urlMatcher $paramsMatcher")
        .execute[HttpResponse](ArgumentMatchers.any(), ArgumentMatchers.any())
    ).thenReturn(Future.successful(HttpResponse(status, body = response.toString())))
  }
}
