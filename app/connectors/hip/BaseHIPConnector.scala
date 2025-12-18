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

import config.AppConfig
import parsers.hip.Parser
import play.api.libs.json.JsValue
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, HeaderNames, HttpReads}

import java.net.URL
import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId}
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

trait BaseHIPConnector {

  val appConfig: AppConfig
  val httpClient: HttpClientV2

  implicit val ec: ExecutionContext

  def get[A](uri: String, parser: Parser[A], additionalHeaders: Map[String, String] = Map.empty)
            (implicit hc: HeaderCarrier): Future[A] = {

    val correlationId: String = UUID.randomUUID().toString
    val headers = fullHeaders(correlationId, additionalHeaders)
    val headerCarrier = fullHeaderCarrier(hc, headers)

    implicit val reads: HttpReads[A] = parser.httpReads(correlationId)

    httpClient
      .get(getFullUrl(uri))(headerCarrier)
      .setHeader(headers.toSeq: _*)
      .execute[A]
  }

  def post[A](uri: String, body: JsValue, parser: Parser[A], additionalHeaders: Map[String, String] = Map.empty)
             (implicit hc: HeaderCarrier): Future[A] = {

    val correlationId: String = UUID.randomUUID().toString
    val headers = fullHeaders(correlationId, additionalHeaders)
    val headerCarrier = fullHeaderCarrier(hc, headers)

    implicit val reads: HttpReads[A] = parser.httpReads(correlationId)

    httpClient
      .post(getFullUrl(uri))(headerCarrier)
      .withBody(body)
      .setHeader(headers.toSeq: _*)
      .execute[A]
  }

  private def fullHeaders(correlationId: String, additionalHeaders: Map[String, String]): Map[String, String] = Map(
    HeaderNames.authorisation -> appConfig.getHipAuthToken,
    "correlationId" -> correlationId,
    "X-Originating-System" -> "MDTP",
    "X-Receipt-Date" -> isoDatePattern.format(Instant.now()),
    "X-Regime" -> "ITSA",
    "X-Regime-Type" -> "ITSA",
    "X-Transmitting-System" -> "HIP"
  ) ++ additionalHeaders

  private def fullHeaderCarrier(headerCarrier: HeaderCarrier, headers: Map[String, String]): HeaderCarrier = headerCarrier
    .copy(authorization = Some(Authorization(appConfig.getHipAuthToken)))
    .withExtraHeaders((headers - HeaderNames.authorisation).toSeq: _*)

  private val isoDatePattern: DateTimeFormatter = DateTimeFormatter
    .ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
    .withZone(ZoneId.of("UTC"))

  private def getFullUrl(uri: String) =
    new URL(s"${appConfig.getHipBaseURL}$uri")

}
