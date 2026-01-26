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
import models.status.GetITSAStatus
import parsers.hip.Parser
import play.api.http.Status.OK
import play.api.libs.json.*
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object GetITSAStatusParser {

  type GetItsaStatusResponse = Either[ErrorModel, Seq[GetITSAStatusTaxYearResponse]]

  object GetItsaStatusResponseHttpReads extends Parser[GetItsaStatusResponse] {

    override val apiNumber: Int = 5197
    override val apiName: String = "Get ITSA Status"

    override def httpReads(correlationId: String): HttpReads[GetItsaStatusResponse] = {
      (_: String, _: String, response: HttpResponse) => {
        response.status match {
          case OK => handleOkResponse(response.json, correlationId)
          case status => handleOtherResponse(status, correlationId)
        }
      }
    }

    private def handleOkResponse(json: JsValue, correlationId: String) = {

      json.validate[Seq[GetITSAStatusTaxYearResponse]] match {
        case JsSuccess(value, _) =>
          Right(value)
        case JsError(errors) =>
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


  case class GetITSAStatusTaxYearResponse(taxYear: String, itsaStatusDetails: Seq[ITSAStatusDetail])

  object GetITSAStatusTaxYearResponse {
    implicit val format: OFormat[GetITSAStatusTaxYearResponse] = Json.format[GetITSAStatusTaxYearResponse]
  }

  case class ITSAStatusDetail(status: GetITSAStatus)

  object ITSAStatusDetail {
    implicit val format: OFormat[ITSAStatusDetail] = Json.format[ITSAStatusDetail]
  }

}
