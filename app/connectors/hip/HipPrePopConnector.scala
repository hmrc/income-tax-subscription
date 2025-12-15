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

import config.AppConfig
import parsers.hip.HipPrePopParser.*
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HipPrePopConnector @Inject()(val httpClient: HttpClientV2, val appConfig: AppConfig)
                                  (implicit val ec: ExecutionContext) extends BaseHIPConnector {

  def getHipPrePopData(
                        nino: String
                      )(implicit hc: HeaderCarrier): Future[GetHipPrePopResponse] = {
    super.get[GetHipPrePopResponse](
      uri = hipPrePopUrl(nino),
      parser = GetHipPrePopResponseHttpReads
    )
  }

  private def hipPrePopUrl(nino: String) =
    s"/cesa/prepopulation/businessdata/$nino"
}
