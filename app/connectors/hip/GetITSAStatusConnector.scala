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
import parsers.hip.GetITSAStatusParser.*
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetITSAStatusConnector @Inject()(val httpClient: HttpClientV2, val appConfig: AppConfig)
                                      (implicit val ec: ExecutionContext) extends BaseHIPConnector {

  def getItsaStatus(utr: String)(implicit hc: HeaderCarrier): Future[GetITSAStatusResponse] = {
    super.get(
      uri = getItsaStatusUrl(utr),
      parser = GetITSAStatusHttpReads
    )
  }

  private def getItsaStatusUrl(utr: String) = {
    s"/itsd/person-itd/itsa-status/$utr?taxYear=${AccountingPeriodUtil.getCurrentTaxYear.toShortTaxYear}&futureYears=true"
  }
}
