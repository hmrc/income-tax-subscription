/*
 * Copyright 2019 HM Revenue & Customs
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

import config.MicroserviceAppConfig
import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsObject, Json, Writes}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.HttpReads.readRaw
import uk.gov.hmrc.http.logging.Authorization
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PropertyConnector @Inject()(val http: HttpClient,
                                  val appConfig: MicroserviceAppConfig) {

  def propertySubscribe(nino: String)
                       (implicit hc: HeaderCarrier): Future[PropertyIncomeSubscriptionSuccess.type] = {

    val headerCarrier = hc
      .withExtraHeaders("Environment" -> appConfig.desEnvironment)
      .copy(authorization = Some(Authorization(appConfig.desToken)))

    http.POST(
      url = appConfig.propertySubscribeUrl(nino),
      body = Json.obj()
    )(
      implicitly[Writes[JsObject]],
      readRaw,
      headerCarrier,
      implicitly[ExecutionContext]
    ) map (_ => PropertyIncomeSubscriptionSuccess)
  }
}

case object PropertyIncomeSubscriptionSuccess

