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
import play.api.libs.json.JsObject
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, HeaderNames, HttpReads, HttpResponse}

import java.net.URL
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

abstract class BaseHIPConnector(
  httpClient: HttpClientV2,
  appConfig: AppConfig
)(implicit ec: ExecutionContext) {

  private case class Env(
    headers: Map[String, String],
    headerCarrier: HeaderCarrier
  )

  def get[T](
    url: URL,
    parser: Parser[T],
    custom: Map[String, String] = Map.empty
  )(implicit hc: HeaderCarrier): Future[T] = {
    val env = getEnv(custom)

    doCallWithHeaders(
      httpClient.get(url)(env.headerCarrier),
      env.headers,
      parser
    )
  }

  def post[T](
    url: URL,
    body: JsObject,
    parser: Parser[T],
    custom: Map[String, String] = Map.empty
  )(implicit hc: HeaderCarrier): Future[T] = {
    val env = getEnv(custom)

    doCallWithHeaders(
      httpClient.post(url)(env.headerCarrier).withBody(body),
      env.headers,
      parser
    )
  }

  private def doCallWithHeaders[T](
    call: RequestBuilder,
    headers: Map[String, String],
    parser: Parser[T]
  ): Future[T] = {
    implicit val reads: Reade[T] = new Reade[T](parser)
    headers.foldLeft(call)((a, b) => a.setHeader(b)).execute
  }

  private def getEnv(
    custom: Map[String, String]
  )(implicit hc: HeaderCarrier): Env = {
    val headers: Map[String, String] = Map(
      HeaderNames.authorisation -> appConfig.getITSAStatusAuthorisationToken,
      "correlationId" -> UUID.randomUUID().toString
    ) ++ custom

    val headerCarrier: HeaderCarrier = hc
      .copy(authorization = Some(Authorization(appConfig.getITSAStatusAuthorisationToken)))
      .withExtraHeaders((headers - HeaderNames.authorisation).toSeq: _*)

    Env(headers, headerCarrier)
  }
}

private class Reade[T](
  parser: Parser[T]
) extends HttpReads[T] {
  override def read(method: String, url: String, response: HttpResponse): T =
    parser.read(response)
}