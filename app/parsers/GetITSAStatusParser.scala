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

package parsers

import models.status.ITSAStatus
import play.api.http.Status.OK
import play.api.libs.json._
import uk.gov.hmrc.http.{HttpReads, HttpResponse, InternalServerException}

object GetITSAStatusParser {
  implicit val getITSAStatusResponseHttpReads: HttpReads[Seq[GetITSAStatusTaxYearResponse]] =
    (_: String, _: String, response: HttpResponse) => {
      response.status match {
        case OK => response.json.validate[Seq[GetITSAStatusTaxYearResponse]] match {
          case JsSuccess(value, _) =>
            value
          case JsError(errors) =>
            throw new InternalServerException(s"[GetITSAStatusParser] - Failure parsing json. Errors: $errors")
        }
        case status =>
          throw new InternalServerException(s"[GetITSAStatusParser] - Unsupported status received: $status")
      }
    }

  case class GetITSAStatusTaxYearResponse(taxYear: String, itsaStatusDetails: Seq[ITSAStatusDetail])

  object GetITSAStatusTaxYearResponse {
    implicit val format: OFormat[GetITSAStatusTaxYearResponse] = Json.format[GetITSAStatusTaxYearResponse]
  }

  case class ITSAStatusDetail(status: ITSAStatus)

  object ITSAStatusDetail {
    implicit val format: OFormat[ITSAStatusDetail] = Json.format[ITSAStatusDetail]
  }

}
