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

package connectors

import config.AppConfig
import models.SignUpRequest
import parsers.SignUpParser.{PostSignUpResponse, hipSignUpResponseHttpReads}
import play.api.libs.json.{JsObject, JsValue, Json}
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, HeaderNames, HttpClient, HttpReads}

import java.time.{Instant, ZoneId}
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HIPSignUpTaxYearConnector @Inject()(http: HttpClient,
                                          val appConfig: AppConfig)(implicit ec: ExecutionContext) {

  def signUpUrl: String = s"${appConfig.hipSignUpServiceURL}/RESTAdapter/itsa/taxpayer/signup-mtdfb"

  def requestBody(signUpRequest: SignUpRequest): JsObject =
      Json.obj(
        "signUpMTDfB" -> Json.obj(
          "nino" -> signUpRequest.nino,
          "utr" -> signUpRequest.utr,
          "signupTaxYear" -> signUpRequest.taxYear
        )
      )

  def signUp(signUpRequest: SignUpRequest)(implicit hc: HeaderCarrier): Future[PostSignUpResponse] = {

    val headerCarrier: HeaderCarrier = hc
      .copy(authorization = Some(Authorization(appConfig.hipSignUpServiceAuthorisationToken)))

    val formatter = DateTimeFormatter
      .ofPattern("yyyy-MM-dd'T'HH:mm:ssZ")
      .withZone(ZoneId.of("UTC"))

    val headers: Seq[(String, String)] = Seq(
      HeaderNames.authorisation -> appConfig.hipSignUpServiceAuthorisationToken,
      "correlationid" -> UUID.randomUUID().toString,
      "X-Message-Type" -> "ITSASignUpMTDfB",
      "X-Originating-System" -> "MDTP",
      "X-Receipt-Date" -> formatter.format(Instant.now()),
      "X-Regime-Type" -> "ITSA",
      "X-Transmitting-System" -> "HIP"
    )

    http.POST[JsValue, PostSignUpResponse](signUpUrl, requestBody(signUpRequest), headers = headers)(
      implicitly, implicitly[HttpReads[PostSignUpResponse]], headerCarrier, implicitly)
  }
}
