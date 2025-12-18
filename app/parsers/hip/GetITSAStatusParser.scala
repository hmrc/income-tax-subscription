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
import play.api.libs.json.{JsError, JsSuccess, JsValue}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object GetITSAStatusParser extends Logging {

  type GetITSAStatusResponse = Either[ErrorModel, Seq[GetITSAStatusTaxYearResponse]]

  object GetITSAStatusHttpReads extends Parser[GetITSAStatusResponse] {
    val apiNumber = 5197
    val apiName = "Get ITSA Status"

    override def httpReads(correlationId: String): HttpReads[GetITSAStatusResponse] = {
      (_: String, _: String, response: HttpResponse) => {
        response.status match {
          case OK => handleOkResponse(response.json, correlationId)
          case status => handleOtherResponse(status, correlationId)
        }
      }
    }

    private def handleOkResponse(json: JsValue, correlationId: String) = {
      json.validate[Seq[GetITSAStatusTaxYearResponse]] match {
        case JsSuccess(value, _) => Right(value)
        case JsError(_) => Left(ErrorModel(OK,
          super.error(
            correlationId = correlationId,
            status = OK,
            reason = "Failure parsing json response"
          )
        ))
      }
    }

    private def handleOtherResponse(status: Int, correlationId: String) = {
      Left(ErrorModel(status,
        super.error(
          correlationId = correlationId,
          status = status,
          reason = s"Unexpected status returned"
        )
      ))
    }
  }

}
