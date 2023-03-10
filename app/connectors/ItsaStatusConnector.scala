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

package connectors

import config.AppConfig
import models.subscription.AccountingPeriodUtil
import parsers.ItsaStatusParser.{GetItsaStatusResponse, itsaStatusResponseHttpReads}
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, HeaderNames, HttpClient, HttpReads}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ItsaStatusConnector @Inject()(httpClient: HttpClient,
                                    appConfig: AppConfig)(implicit ec: ExecutionContext) {
  def getItsaStatus(nino: String, utr: String)
                   (implicit hc: HeaderCarrier): Future[GetItsaStatusResponse] = {

    val headerCarrier: HeaderCarrier = hc
      .copy(authorization = Some(Authorization(appConfig.statusDeterminationServiceAuthorisationToken)))
      .withExtraHeaders("Environment" -> appConfig.statusDeterminationServiceEnvironment)

    val headers: Seq[(String, String)] = Seq(
      HeaderNames.authorisation -> appConfig.statusDeterminationServiceAuthorisationToken,
      "Environment" -> appConfig.statusDeterminationServiceEnvironment
    )

    httpClient.GET[GetItsaStatusResponse](url = getItsaStatusUrl(nino, utr), headers = headers)(
      implicitly[HttpReads[GetItsaStatusResponse]],
      headerCarrier,
      implicitly
    )
  }

  private def getItsaStatusUrl(nino: String, utr: String) =
    s"${appConfig.statusDeterminationServiceURL}/income-tax/itsa-status/$nino/$utr/${AccountingPeriodUtil.getCurrentTaxYear.toItsaStatusShortTaxYear}"
}
