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
import parsers.ItsaStatusParser.{GetItsaStatusResponse, itsaStatusResponseHttpReads}
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, HeaderNames, HttpClient, HttpReads}

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HipItsaStatusConnector @Inject()(
  httpClient: HttpClient,
  appConfig: AppConfig
)(implicit ec: ExecutionContext) {

  def getItsaStatus(
    nino: String,
    utr: String
  )(implicit hc: HeaderCarrier): Future[GetItsaStatusResponse] = {

    val headers: Map[String, String] = Map(
      HeaderNames.authorisation -> appConfig.hipItsaStatusAuthorisationToken,
      "correlationId" -> UUID.randomUUID().toString
    )

    val headerCarrier: HeaderCarrier = hc
      .copy(authorization = Some(Authorization(appConfig.getITSAStatusAuthorisationToken)))
      .withExtraHeaders((headers - HeaderNames.authorisation).toSeq: _*)

    httpClient.GET[GetItsaStatusResponse](
      url = getItsaStatusUrl(nino, utr),
      headers = headers.toSeq
    )(
      implicitly[HttpReads[GetItsaStatusResponse]],
      headerCarrier,
      implicitly
    )
  }

  private def getItsaStatusUrl(nino: String, utr: String): String = {
    s"${appConfig.hipItsaStatusURL}/itsa-status/signup/$nino/$utr/${AccountingPeriodUtil.getCurrentTaxYear.toShortTaxYear}"
  }

}
