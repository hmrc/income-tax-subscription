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

import models.subscription.business.{CreateIncomeSourceErrorModel, CreateIncomeSourceSuccessModel}
import parsers.hip.Parser
import play.api.Logging
import play.api.http.Status.{CREATED, FORBIDDEN, UNPROCESSABLE_ENTITY}
import play.api.libs.json.{JsSuccess, JsValue, Json, Reads}
import uk.gov.hmrc.http.{HttpReads, HttpResponse, InternalServerException}

object ITSAIncomeSourceParser extends Logging {

  type PostITSAIncomeSourceResponse = Either[CreateIncomeSourceErrorModel, CreateIncomeSourceSuccessModel]

  object ITSAIncomeSourceResponseHttpReads extends Parser[PostITSAIncomeSourceResponse] {

    val apiNumber: Int = 5265
    val apiName: String = "Create income sources"

    override def httpReads(correlationId: String): HttpReads[PostITSAIncomeSourceResponse] = {
      (_: String, _: String, response: HttpResponse) => {
        response.status match {
          case CREATED => handleCreatedResponse()
          case FORBIDDEN => handleForbiddenResponse(correlationId)
          case UNPROCESSABLE_ENTITY => handleUnprocessableEntityResponse(response.json, correlationId)
          case status => handleOtherResponse(status, correlationId)
        }
      }
    }

    private def handleCreatedResponse() = {
      Right(CreateIncomeSourceSuccessModel())
    }

    private def handleForbiddenResponse(correlationId: String): Nothing = {
      throw ITSAIncomeSourceForbiddenException(super.error(
        correlationId = correlationId,
        status = FORBIDDEN,
        reason = "Unexpected status received"
      ))
    }

    private def handleUnprocessableEntityResponse(json: JsValue, correlationId: String) = {
      (json \ "errors").validate[Error] match {
        case JsSuccess(error, _) =>
          Left(CreateIncomeSourceErrorModel(
            status = UNPROCESSABLE_ENTITY,
            reason = super.error(
              correlationId = correlationId,
              status = UNPROCESSABLE_ENTITY,
              maybeCode = Some(error.code),
              reason = error.text
            )
          ))
        case _ =>
          Left(CreateIncomeSourceErrorModel(
            status = UNPROCESSABLE_ENTITY,
            reason = super.error(
              correlationId = correlationId,
              status = UNPROCESSABLE_ENTITY,
              reason = "Failure parsing json response"
            )
          ))
      }

    }

    private def handleOtherResponse(status: Int, correlationId: String) = {
      Left(CreateIncomeSourceErrorModel(
        status = status,
        reason = super.error(
          correlationId = correlationId,
          status = status,
          reason = "Unexpected status received"
        )
      ))
    }

    case class Error(processingDate: String,
                     code: String,
                     text: String)

    object Error {
      implicit val reads: Reads[Error] = Json.reads[Error]
    }

  }

  case class ITSAIncomeSourceForbiddenException(text: String) extends InternalServerException(text)
}
