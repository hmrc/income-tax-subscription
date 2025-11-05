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

package connectors

import com.typesafe.config.Config
import config.AppConfig
import connectors.hip.BaseHIPConnector
import org.apache.pekko.actor.ActorSystem
import parsers.GetITSABusinessDetailsParser._
import play.api.http.Status.FORBIDDEN
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, HeaderNames, Retries, StringContextOps}

import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId}
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetITSABusinessDetailsConnector @Inject()(
  httpClient: HttpClientV2,
  appConfig: AppConfig,
  val configuration: Config,
  val actorSystem: ActorSystem
)(implicit ec: ExecutionContext) extends BaseHIPConnector(
  httpClient,
  appConfig
) with Retries {

  private val formatter = DateTimeFormatter
    .ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
    .withZone(ZoneId.of("UTC"))

  def getHIPBusinessDetails(nino: String)(implicit hc: HeaderCarrier): Future[GetITSABusinessDetailsResponse] = {
    retryFor("HIP API #5266 - Get Business Details") {
      case GetITSABusinessDetailsParserException(_, FORBIDDEN) => true
      case _ => false
    } {
      val headers: Map[String, String] = Map(
        "X-Message-Type" -> "TaxpayerDisplay",
        "X-Originating-System" -> "MDTP",
        "X-Receipt-Date" -> formatter.format(Instant.now()),
        "X-Regime-Type" -> "ITSA",
        "X-Transmitting-System" -> "HIP"
      )

      super.get(getHIPBusinessDetailsUrl(nino), GetITSABusinessDetailsResponseHttpReads, headers)
    }
  }

  private def getHIPBusinessDetailsUrl(nino: String) =
    url"${appConfig.hipBusinessDetailsURL}/etmp/RESTAdapter/itsa/taxpayer/business-details?nino=$nino"

}
