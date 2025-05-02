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

import config.AppConfig
import models.subscription.CreateIncomeSourcesModel
import parsers.ITSAIncomeSourceParser.{PostITSAIncomeSourceResponse, itsaIncomeSourceResponseHttpReads}
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, HttpClient, HttpReads}

import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId}
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ItsaIncomeSourceConnector @Inject()(http: HttpClient,
                                          appConfig: AppConfig
                                         )(implicit ec: ExecutionContext) {

  private def itsaIncomeSourceUrl: String = {
    s"${appConfig.desURL}/RESTAdapter/itsa/taxpayer/income-source"
  }

  private val isoDatePattern: DateTimeFormatter = DateTimeFormatter
    .ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
    .withZone(ZoneId.of("UTC"))


  def createIncomeSources(mtdbsaRef: String,
                          createIncomeSources: CreateIncomeSourcesModel)
                         (implicit hc: HeaderCarrier): Future[PostITSAIncomeSourceResponse] = {

    val hipHeaders: Seq[(String, String)] = Seq(
      "correlationid" -> UUID.randomUUID().toString,
      "X-Message-Type" -> "CreateIncomeSource",
      "X-Originating-System" -> "MDTP",
      "X-Receipt-Date" -> isoDatePattern.format(Instant.now()),
      "X-Regime" -> "ITSA",
      "X-Transmitting-System" -> "HIP"
    )

    val headerCarrier: HeaderCarrier = hc
      .copy(authorization = Some(Authorization(appConfig.itsaIncomeSourceAuthorisationToken)))
      .withExtraHeaders(hipHeaders: _*)


    http.POST[JsValue, PostITSAIncomeSourceResponse](
      url = itsaIncomeSourceUrl,
      body = Json.toJson(createIncomeSources)(CreateIncomeSourcesModel.hipWrites(mtdbsaRef)),
      headers = hipHeaders.toSeq
    )(
      implicitly,
      implicitly[HttpReads[PostITSAIncomeSourceResponse]],
      headerCarrier,
      implicitly
    ) map {
      case Left(error) =>
        Left(error)
      case Right(value) =>
        Right(value)
    }

  }

}
