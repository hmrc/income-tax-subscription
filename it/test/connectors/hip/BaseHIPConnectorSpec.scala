/*
 * Copyright 2025 HM Revenue & Customs
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

package connectors.hip

import com.typesafe.config.Config
import config.AppConfig
import connectors.ConnectorRetries
import models.ErrorModel
import org.apache.pekko.actor.ActorSystem
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers.shouldBe
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.play.PlaySpec
import parsers.hip.Parser
import play.api.http.Status.GATEWAY_TIMEOUT
import play.api.libs.json.JsString
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, HttpResponse}

import java.time.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future, TimeoutException}

class BaseHIPConnectorSpec extends PlaySpec
  with ScalaFutures
  with BeforeAndAfterEach {

  private val oneMilliSec: Duration = Duration.ofMillis(1)

  private val specific: java.util.List[Duration] = java.util.List.of(
    oneMilliSec,
    oneMilliSec,
    oneMilliSec
  )

  private val http = mock[HttpClientV2]
  private val appConf = mock[AppConfig]
  private val config = mock[Config]
  private val testUrl = "http://localhost:8080/"
  private val builder = mock[RequestBuilder]

  implicit val hc: HeaderCarrier = HeaderCarrier()

  object TestConnector extends BaseHIPConnector with ConnectorRetries {
    override val appConfig: AppConfig = appConf
    override val httpClient: HttpClientV2 = http
    override implicit val ec: ExecutionContext = global

    override protected def actorSystem: ActorSystem = ActorSystem("test-actor-system")

    override protected def configuration: Config = config

    def testPost: Future[Either[ErrorModel, String]] =
      retryFor[Either[ErrorModel, String]](TestParser.apiNumber, TestParser.apiName) {
        case Left(ErrorModel(GATEWAY_TIMEOUT, _, _)) => true
      } {
        super.post[String](
          uri = testUrl,
          body = JsString(testUrl),
          parser = TestParser
        )
    }

    def testGet: Future[Either[ErrorModel, String]] =
      retryFor[Either[ErrorModel, String]](TestParser.apiNumber, TestParser.apiName) {
        case Left(ErrorModel(GATEWAY_TIMEOUT, _, _)) => true
      } {
        super.get[String](
          uri = testUrl,
          parser = TestParser
        )
      }
  }

  object TestParser extends Parser[Either[ErrorModel, String]] {
    override val apiNumber: Int = -1
    override val apiName: String = "Test API"

    override def httpReads(correlationId: String): HttpReads[Either[ErrorModel, String]] = {
      (_: String, _: String, response: HttpResponse) => Right(testUrl)
    }
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    when(config.getDurationList(ArgumentMatchers.eq(s"retries.intervals.${TestParser.apiNumber}"))).thenReturn(specific)
    when(http.post(any())(any())).thenReturn(builder)
    when(builder.setHeader(any())).thenReturn(builder)
    when(builder.withBody(any())(any(), any(), any())).thenReturn(builder)
    when(appConf.getHipBaseURL).thenReturn("")
    when(appConf.getHipAuthToken).thenReturn("")
  }

  "should retry 2 times and return a successful response when receiving a timeout" when {
    "POST" in {
      when(builder.execute(any(), any()))
        .thenThrow(new TimeoutException)
        .thenThrow(new TimeoutException)
        .thenReturn(Future.successful(Right(testUrl)))

      val result = TestConnector.testPost

      result.futureValue shouldBe Right(testUrl)
      verify(http, times(3)).post(any())(any())
    }
    
    "GET" in {
      when(builder.execute(any(), any()))
        .thenThrow(new TimeoutException)
        .thenThrow(new TimeoutException)
        .thenReturn(Future.successful(Right(testUrl)))

      val result = TestConnector.testGet

      result.futureValue shouldBe Right(testUrl)
      verify(http, times(3)).get(any())(any())
    }
  }
}
