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
import parsers.PrePopParser.{GetPrePopResponse, GetPrePopResponseHttpReads}
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, HeaderNames, HttpClient, HttpReads}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HipPrePopConnector @Inject()(http: HttpClient,
                                   appConfig: AppConfig)(implicit ec: ExecutionContext) {

  def prepopUrl(nino: String): String = s"${appConfig.prePopURL}/income-tax/pre-prop/$nino"

  def getPrePopData(nino: String)(implicit hc: HeaderCarrier): Future[GetPrePopResponse] = {

    val headerCarrier: HeaderCarrier = hc
      .copy(authorization = Some(Authorization(appConfig.prePopAuthorisationToken)))
      .withExtraHeaders("Environment" -> appConfig.prePopEnvironment)

    val headers: Seq[(String, String)] = Seq(
      HeaderNames.authorisation -> appConfig.prePopAuthorisationToken,
      "Environment" -> appConfig.prePopEnvironment
    )

    http.GET[GetPrePopResponse](prepopUrl(nino), headers = headers)(implicitly[HttpReads[GetPrePopResponse]], headerCarrier, implicitly)
  }
}
