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

import com.typesafe.config.Config
import config.AppConfig
import connectors.ConnectorRetries
import models.ErrorModel
import models.subscription.AccountingPeriodUtil
import org.apache.pekko.actor.ActorSystem
import parsers.GetITSAStatusParser.GetITSAStatusTaxYearResponse
import parsers.hip.GetITSAStatusParser.*
import play.api.http.Status.{BAD_GATEWAY, SERVICE_UNAVAILABLE, TOO_MANY_REQUESTS}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetITSAStatusConnector @Inject() (
  val httpClient: HttpClientV2,
  val appConfig: AppConfig,
  val configuration: Config,
  val actorSystem: ActorSystem
) (implicit val ec: ExecutionContext) extends BaseHIPConnector with ConnectorRetries {

  private val apiNumber = GetITSAStatusHttpReads.apiNumber
  private val apiName = GetITSAStatusHttpReads.apiName

  def getItsaStatus(nino: String)(implicit hc: HeaderCarrier): Future[GetITSAStatusResponse] =
    retryFor[Option[Seq[GetITSAStatusTaxYearResponse]]](apiNumber, apiName) {
      case Left(ErrorModel(TOO_MANY_REQUESTS, _, _)) => true
      case Left(ErrorModel(BAD_GATEWAY, _, _)) => true
      case Left(ErrorModel(SERVICE_UNAVAILABLE, _, _)) => true
    } {
      super.get(
        uri = getItsaStatusUrl(nino),
        parser = GetITSAStatusHttpReads
      )
  }

  private def getItsaStatusUrl(nino: String) = {
    s"/itsd/person-itd/itsa-status/$nino?taxYear=${AccountingPeriodUtil.getCurrentTaxYear.toShortTaxYear}"
  }
}
