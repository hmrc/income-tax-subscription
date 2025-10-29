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
import parsers.GetITSAStatusParser.GetITSAStatusTaxYearResponse
import play.api.http.Status.OK
import play.api.libs.json.JsSuccess
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object GetITSAStatusParser {

  type GetITSAStatusResponse = Either[ConnectorFailure, Seq[GetITSAStatusTaxYearResponse]]

  implicit object GetITSAStatusHttpReads extends HttpReads[GetITSAStatusResponse] {
    override def read(method: String, url: String, response: HttpResponse): GetITSAStatusResponse =
      response.status match {
        case OK => response.json.validate[Seq[GetITSAStatusTaxYearResponse]] match {
          case JsSuccess(value, _) => Right(value)
          case _ => Left(InvalidJson)
        }
        case status => Left(UnexpectedStatus(status))
      }
  }
}
