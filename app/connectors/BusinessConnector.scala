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
import models.subscription.incomesource.BusinessIncomeModel
import parsers.MtditIdParser.MtditIdHttpReads
import play.api.libs.json.{JsObject, Writes}
import uk.gov.hmrc.http.logging.Authorization
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads}
import uk.gov.hmrc.play.bootstrap.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class BusinessConnector @Inject()(val http: HttpClient,
                                  val appConfig: MicroserviceAppConfig)(implicit ec: ExecutionContext) {

  def businessSubscribe(nino: String, businessIncomeModel: BusinessIncomeModel)
                       (implicit hc: HeaderCarrier): Future[String] = {

    val headerCarrier: HeaderCarrier = hc
      .copy(authorization = Some(Authorization(appConfig.desAuthorisationToken)))
      .withExtraHeaders(appConfig.desEnvironmentHeader)

    http.POST(
      url = appConfig.businessSubscribeUrl(nino),
      body = BusinessIncomeModel.writeToDes(businessIncomeModel)
    )(
      implicitly[Writes[JsObject]],
      implicitly[HttpReads[String]],
      headerCarrier,
      implicitly[ExecutionContext]
    )
  }
}