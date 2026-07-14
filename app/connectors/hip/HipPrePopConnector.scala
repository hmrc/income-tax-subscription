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

import com.typesafe.config.Config
import config.AppConfig
import connectors.ConnectorRetries
import models.ErrorModel
import models.hip.SelfEmpHolder
import parsers.hip.HipPrePopParser.*
import uk.gov.hmrc.http.HeaderCarrier
import org.apache.pekko.actor.ActorSystem
import play.api.http.Status.{BAD_GATEWAY, INTERNAL_SERVER_ERROR, SERVICE_UNAVAILABLE, TOO_MANY_REQUESTS}
import uk.gov.hmrc.http.client.HttpClientV2

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HipPrePopConnector @Inject()(val httpClient: HttpClientV2,
                                   val appConfig: AppConfig,
                                   val configuration: Config,
                                   val actorSystem: ActorSystem)
                                  (implicit val ec: ExecutionContext) extends BaseHIPConnector with ConnectorRetries {

  private val apiNumber = GetHipPrePopResponseHttpReads.apiNumber
  private val apiName = GetHipPrePopResponseHttpReads.apiName

  def getHipPrePopData(nino: String)(implicit hc: HeaderCarrier): Future[GetHipPrePopResponse] =
    retryFor[GetHipPrePopResponse](apiNumber, apiName) {
      case Left(ErrorModel(TOO_MANY_REQUESTS, _, _)) => true
      case Left(ErrorModel(BAD_GATEWAY, _, _)) => true
      case Left(ErrorModel(SERVICE_UNAVAILABLE, _, _)) => true
      case Left(ErrorModel(INTERNAL_SERVER_ERROR, _, _)) => true
    } {
      super.get[SelfEmpHolder](
        uri = hipPrePopUrl(nino),
        parser = GetHipPrePopResponseHttpReads
      )
    }

  private def hipPrePopUrl(nino: String) =
    s"/cesa/prepopulation/businessdata/$nino"
}
