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
import models.subscription.AccountingPeriodUtil
import parsers.hip.GetITSAStatusParser._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, HeaderNames, StringContextOps}

import java.net.URL
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetITSAStatusConnector @Inject()(httpClient: HttpClientV2,
                                       appConfig: AppConfig)(implicit ec: ExecutionContext) {

  def getItsaStatus(utr: String)(implicit hc: HeaderCarrier): Future[GetITSAStatusResponse] = {

    val headers: Map[String, String] = Map(
      HeaderNames.authorisation -> appConfig.getITSAStatusAuthorisationToken,
      "correlationId" -> UUID.randomUUID().toString
    )

    val headerCarrier: HeaderCarrier = hc
      .copy(authorization = Some(Authorization(appConfig.getITSAStatusAuthorisationToken)))
      .withExtraHeaders((headers - HeaderNames.authorisation).toSeq: _*)

    val call = httpClient.get(getItsaStatusUrl(utr))(headerCarrier)
    headers.foldLeft(call)((a, b) => a.setHeader(b)).execute
  }

  private def getItsaStatusUrl(utr: String): URL = {
    url"${appConfig.taxableEntityAPI}/itsd/person-itd/itsa-status/$utr?taxYear=${AccountingPeriodUtil.getCurrentTaxYear.toShortTaxYear}&futureYears=true"
  }
}
