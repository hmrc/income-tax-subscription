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

import models.ErrorModel
import models.status.{ItsaStatusResponse, TaxYearStatus}
import parsers.hip.Parser
import play.api.http.Status.OK
import play.api.libs.json.{JsError, JsSuccess, JsValue}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object ItsaStatusParser {

  type GetItsaStatusResponse = Either[ErrorModel, ItsaStatusResponse]

  object ItsaStatusResponseHttpReads extends Parser[GetItsaStatusResponse] {
    val apiNumber: Int = 5197
    val apiName: String = "Determine ITSA Status for Sign Up"

    override def httpReads(correlationId: String): HttpReads[GetItsaStatusResponse] = {
      (_: String, _: String, response: HttpResponse) => {
        response.status match {
          case OK => handleOkResponse(response.json, correlationId)
          case status => handleOtherResponse(status, correlationId)
        }
      }
    }

    private def handleOkResponse(json: JsValue, correlationId: String) = {
      json.validate[List[TaxYearStatus]] match {
        case JsSuccess(value, _) => Right(ItsaStatusResponse(value))
        case JsError(_) =>
          Left(ErrorModel(
            status = OK,
            message = super.error(
              correlationId = correlationId,
              status = OK,
              reason = "Failure parsing json response"
            )
          ))
      }
    }

    private def handleOtherResponse(status: Int, correlationId: String) = {
      Left(ErrorModel(
        status = status,
        message = super.error(
          correlationId = correlationId,
          status = status,
          reason = "Unexpected status returned"
        )
      ))
    }
  }
}
