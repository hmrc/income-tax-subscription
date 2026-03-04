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

package parsers.hip

import models.ErrorModel
import models.hip.SelfEmpHolder
import play.api.Logging
import play.api.http.Status.{BAD_GATEWAY, NOT_FOUND, OK, SERVICE_UNAVAILABLE}
import play.api.libs.json.{JsError, JsSuccess, JsValue}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object HipPrePopParser extends Logging {

  type GetHipPrePopResponse = Either[ErrorModel, SelfEmpHolder]

  implicit object GetHipPrePopResponseHttpReads extends Parser[GetHipPrePopResponse] {
    val apiNumber = 5646
    val apiName = "Business Data"

    override def httpReads(correlationId: String): HttpReads[GetHipPrePopResponse] = {
      (_: String, _: String, response: HttpResponse) => {
        response.status match {
          case OK => handleOkResponse(response.json, correlationId)
          case NOT_FOUND | SERVICE_UNAVAILABLE | BAD_GATEWAY => Right(SelfEmpHolder(None))
          case status => handleOtherResponse(status, correlationId)
        }
      }
    }

    private def handleOkResponse(json: JsValue, correlationId: String) = {
      json.validate[SelfEmpHolder] match {
        case JsSuccess(value, _) => Right(value)
        case JsError(_) => Left(ErrorModel(
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
