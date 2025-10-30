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

import com.typesafe.config.Config
import config.AppConfig
import models.SignUpRequest
import org.apache.pekko.actor.ActorSystem
import parsers.SignUpParser._
import play.api.http.Status.FORBIDDEN
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, HeaderNames, Retries, StringContextOps}

import java.net.URL
import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId}
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HIPSignUpTaxYearConnector @Inject()(http: HttpClientV2,
                                          val appConfig: AppConfig,
                                          val configuration: Config,
                                          val actorSystem: ActorSystem)(implicit ec: ExecutionContext) extends Retries {

  def signUpUrl: URL =
    url"${appConfig.hipSignUpServiceURL}/etmp/RESTAdapter/itsa/taxpayer/signup-mtdfb"

  private val formatter = DateTimeFormatter
    .ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
    .withZone(ZoneId.of("UTC"))

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

    retryFor("HIP API #1565 - Sign Up") {
      case SignUpParserException(_, FORBIDDEN) => true
      case _ => false
    } {
      val headers: Seq[(String, String)] = Seq(
        HeaderNames.authorisation -> appConfig.hipSignUpServiceAuthorisationToken,
        "correlationid" -> UUID.randomUUID().toString,
        "X-Message-Type" -> "ITSASignUpMTDfB",
        "X-Originating-System" -> "MDTP",
        "X-Receipt-Date" -> formatter.format(Instant.now()),
        "X-Regime-Type" -> "ITSA",
        "X-Transmitting-System" -> "HIP"
      )

      val call = http.post(signUpUrl)(headerCarrier).withBody(requestBody(signUpRequest))
      headers.foldLeft(call)((a, b) => a.setHeader(b)).execute
    }
  }
}
