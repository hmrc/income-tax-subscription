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

package connectors.hip

import config.AppConfig
import parsers.hip.HipPrePopParser._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, HeaderNames, HttpClient, HttpReads, StringContextOps}

import java.net.URL
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HipPrePopConnector @Inject()(
  http: HttpClientV2,
  appConfig: AppConfig
)(implicit ec: ExecutionContext) {

  private def hipPrePopUrl(nino: String): URL =
    url"${appConfig.hipPrePopURL}/cesa/prepopulation/businessdata/$nino"

  def getHipPrePopData(
    nino: String
  )(implicit hc: HeaderCarrier): Future[GetHipPrePopResponse] = {

    val headers: Map[String, String] = Map(
      HeaderNames.authorisation -> appConfig.hipPrePopAuthorisationToken,
      "correlationId" -> UUID.randomUUID().toString
    )

    val headerCarrier: HeaderCarrier = hc
      .copy(authorization = Some(Authorization(appConfig.hipPrePopAuthorisationToken)))

    val call = http.get(hipPrePopUrl(nino))(headerCarrier)
    headers.foldLeft(call)((a, b) => a.setHeader(b)).execute
  }
}
