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

import connectors.{ConnectorFailure, InvalidJson, UnexpectedStatus}
import models.status.{ItsaStatusResponse, TaxYearStatus}
import play.api.http.Status.OK
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.JsSuccess
import play.api.libs.json.OFormat.oFormatFromReadsAndOWrites
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object HipItsaStatusParser {

  type HipItsaStatusResponse = Either[ConnectorFailure, ItsaStatusResponse]

  implicit object GetITSAStatusHttpReads extends HttpReads[HipItsaStatusResponse] {
    override def read(method: String, url: String, response: HttpResponse): HipItsaStatusResponse =
      response.status match {
        case OK => response.json.validate[List[TaxYearStatus]] match {
          case JsSuccess(value, _) => Right(ItsaStatusResponse(value))
          case _ => Left(InvalidJson)
        }
        case status => Left(UnexpectedStatus(status))
      }
  }

}
