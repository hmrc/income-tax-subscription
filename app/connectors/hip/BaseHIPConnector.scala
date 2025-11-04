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
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, HeaderNames, HttpResponse}
import parsers.hip.Parser
import play.api.http.Status.INTERNAL_SERVER_ERROR

import java.net.URL
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

abstract class BaseHIPConnector(
  httpClient: HttpClientV2,
  appConfig: AppConfig
)(implicit ec: ExecutionContext) {

  def get[T](
    url: URL,
    parser: Parser[T]
  )(implicit hc: HeaderCarrier): Future[T] = {
    val headers: Map[String, String] = Map(
      HeaderNames.authorisation -> appConfig.getITSAStatusAuthorisationToken,
      "correlationId" -> UUID.randomUUID().toString
    )

    val headerCarrier: HeaderCarrier = hc
      .copy(authorization = Some(Authorization(appConfig.getITSAStatusAuthorisationToken)))
      .withExtraHeaders((headers - HeaderNames.authorisation).toSeq: _*)

    val call = httpClient.get(url)(headerCarrier)
    headers.foldLeft(call)((a, b) => a.setHeader(b)).execute.map { r =>
      parser.read(r)
    }.recover { t =>
      parser.read(HttpResponse(INTERNAL_SERVER_ERROR, t.toString))
    }
  }
}
