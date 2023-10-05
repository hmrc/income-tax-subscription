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
import parsers.SignUpParser.{PostSignUpResponse, signUpResponseHttpReads}
import play.api.libs.json.{JsObject, JsValue, Json}
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, HeaderNames, HttpClient, HttpReads}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SignUpTaxYearConnector @Inject()(http: HttpClient,
                                       appConfig: AppConfig)(implicit ec: ExecutionContext) {

  def signUpUrl: String = s"${appConfig.signUpServiceURL}/income-tax/sign-up/ITSA"

  def requestBody(nino: String, taxYear: String): JsObject = Json.obj(
    "nino" -> nino,
    "signupTaxYear" -> taxYear
  )

  def signUp(nino: String, taxYear: String)(implicit hc: HeaderCarrier): Future[PostSignUpResponse] = {

    val headerCarrier: HeaderCarrier = hc
      .copy(authorization = Some(Authorization(appConfig.signUpServiceAuthorisationToken)))
      .withExtraHeaders("Environment" -> appConfig.signUpServiceEnvironment)

    val headers: Seq[(String, String)] = Seq(
      HeaderNames.authorisation -> appConfig.signUpServiceAuthorisationToken,
      "Environment" -> appConfig.signUpServiceEnvironment
    )

    http.POST[JsValue, PostSignUpResponse](signUpUrl, requestBody(nino, taxYear), headers = headers)(
      implicitly, implicitly[HttpReads[PostSignUpResponse]], headerCarrier, implicitly)
  }
}
