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

package parsers.hip

import models.ErrorModel
import parsers.GetITSAStatusParser.GetITSAStatusTaxYearResponse
import play.api.Logging
import play.api.http.Status.OK
import play.api.libs.json.{JsError, JsSuccess}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object GetITSAStatusParser extends Logging {

  type GetITSAStatusResponse = Either[ErrorModel, Seq[GetITSAStatusTaxYearResponse]]

  implicit object GetITSAStatusHttpReads extends HttpReads[GetITSAStatusResponse] {
    override def read(method: String, url: String, response: HttpResponse): GetITSAStatusResponse =
      response.status match {
        case OK => response.json.validate[Seq[GetITSAStatusTaxYearResponse]] match {
          case JsSuccess(value, _) => Right(value)
          case JsError(_) => Left(ErrorModel(OK, "Failure parsing json response from itsa status api"))
        }
        case status =>
          logger.error(s"[GetITSAStatusParser] - Unexpected status from itsa status API. Status: $status")
          Left(ErrorModel(status, "Unexpected status returned from itsa status api"))
      }
  }
}
