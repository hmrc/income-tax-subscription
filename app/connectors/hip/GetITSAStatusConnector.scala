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
import parsers.GetITSAStatusParser.GetITSAStatusTaxYearResponse
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, HeaderNames, HttpClient}

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetITSAStatusConnector @Inject()(httpClient: HttpClient,
                                       appConfig: AppConfig)(implicit ec: ExecutionContext) {

  def getItsaStatus(utr: String)(implicit hc: HeaderCarrier): Future[Seq[GetITSAStatusTaxYearResponse]] = {

    val headers: Map[String, String] = Map(
      HeaderNames.authorisation -> appConfig.getITSAStatusAuthorisationToken,
      "correlationId" -> UUID.randomUUID().toString
    )

    val headerCarrier: HeaderCarrier = hc
      .copy(authorization = Some(Authorization(appConfig.getITSAStatusAuthorisationToken)))
      .withExtraHeaders((headers - HeaderNames.authorisation).toSeq: _*)

    httpClient.GET[Seq[GetITSAStatusTaxYearResponse]](
      url = getItsaStatusUrl(utr),
      headers = headers.toSeq
    )(
      implicitly,
      headerCarrier,
      implicitly
    )
  }

  private def getItsaStatusUrl(utr: String): String = {
    s"${appConfig.taxableEntityAPI}/itsd/person-itd/itsa-status/$utr?taxYear=${AccountingPeriodUtil.getCurrentTaxYear.toShortTaxYear}&futureYears=true"
  }

}
