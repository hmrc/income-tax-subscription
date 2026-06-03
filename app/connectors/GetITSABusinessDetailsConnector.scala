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
import models.ErrorModel
import org.apache.pekko.actor.ActorSystem
import parsers.GetITSABusinessDetailsParser.*
import play.api.http.Status.FORBIDDEN
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetITSABusinessDetailsConnector @Inject()(val httpClient: HttpClientV2,
                                                val appConfig: AppConfig,
                                                val configuration: Config,
                                                val actorSystem: ActorSystem)
                                               (implicit val ec: ExecutionContext) extends BaseHIPConnector with ConnectorRetries {

  def getHIPBusinessDetails(nino: String)(implicit hc: HeaderCarrier): Future[Either[ErrorModel, GetITSABusinessDetailsResponse]] = {
    retryFor[Either[ErrorModel, GetITSABusinessDetailsResponse]](5266, "Get Business Details") {
      case Left(ErrorModel(FORBIDDEN, _, _)) => true
    } {
      val headers: Map[String, String] = Map(
        "X-Message-Type" -> "TaxpayerDisplay"
      )

      super.get(
        uri = getHIPBusinessDetailsUrl(nino),
        parser = GetITSABusinessDetailsResponseHttpReads,
        additionalHeaders = headers
      )
    }
  }

  private def getHIPBusinessDetailsUrl(nino: String) =
    s"/etmp/RESTAdapter/itsa/taxpayer/business-details?nino=$nino"
}
