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
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}

import java.net.URL
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetITSAStatusConnector @Inject()(
  httpClient: HttpClientV2,
  appConfig: AppConfig
)(implicit ec: ExecutionContext) extends BaseHIPConnector(
  httpClient,
  appConfig
) {

  def getItsaStatus(utr: String)(implicit hc: HeaderCarrier): Future[GetITSAStatusResponse] =
    super.get(getItsaStatusUrl(utr), GetITSAStatusHttpReads)

  private def getItsaStatusUrl(utr: String): URL = {
    url"${appConfig.taxableEntityAPI}/itsd/person-itd/itsa-status/$utr?taxYear=${AccountingPeriodUtil.getCurrentTaxYear.toShortTaxYear}&futureYears=true"
  }
}
