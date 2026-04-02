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

import models.SignUpResponse.SignUpSuccess
import models.{ErrorModel, SignUpResponse}
import parsers.hip.Parser
import play.api.http.Status.*
import play.api.libs.json.*
import uk.gov.hmrc.http.{HttpReads, HttpResponse, InternalServerException}

object SignUpParser {

  type PostSignUpResponse = Either[ErrorModel, SignUpResponse]

  implicit object HIPSignUpResponseParser extends Parser[PostSignUpResponse] {
    val apiNumber: Int = 5317
    val apiName: String = "ITSA Sign Up"

    override def httpReads(correlationId: String): HttpReads[PostSignUpResponse] = {
      (_: String, _: String, response: HttpResponse) => {
        response.status match {
          case CREATED => handleCreatedResponse(response.json, correlationId)
          case UNPROCESSABLE_ENTITY => handleUnprocessableEntity(response.json, correlationId)
          case FORBIDDEN => handleForbidden(response, correlationId)
          case status => handleOther(status, correlationId)
        }
      }
    }

    private def handleCreatedResponse(json: JsValue, correlationId: String) = {
      (json \ "success").validate[SignUpSuccess] match {
        case JsSuccess(value, _) => Right(value)
        case JsError(_) => jsonError(CREATED, correlationId)
      }
    }

    private def handleUnprocessableEntity(json: JsValue, correlationId: String) = {
      (json \ "errors").validate[Error] match {
        case JsSuccess(error, _) =>
          responseError(UNPROCESSABLE_ENTITY, error, correlationId)
        case _ =>
          jsonError(UNPROCESSABLE_ENTITY, correlationId)
      }
    }

    private def handleForbidden(response: HttpResponse, correlationId: String): Nothing = {
      throw SignUpParserException(
        error = super.error(
          correlationId = correlationId,
          status = response.status,
          reason = response.body
        ),
        status = response.status
      )
    }

    private def handleOther(status: Int, correlationId: String) = {
      generalError(status, "Unexpected status returned", correlationId)
    }

    private def jsonError(status: Int, correlationId: String) =
      generalError(status, "Failure parsing json response", correlationId)

    private def generalError(status: Int, message: String, correlationId: String) =
      Left(ErrorModel(status,
        super.error(
          correlationId = correlationId,
          status = status,
          reason = message
        )
      ))

    private def responseError(status: Int, error: Error, correlationId: String) =
      Left(ErrorModel(status, error.code,
        super.error(
          correlationId = correlationId,
          status = status,
          maybeCode = Some(error.code),
          reason = error.text
        )
      ))

  }

  case class SignUpParserException(error: String, status: Int) extends InternalServerException(s"[SignUpParserException] - $error - $status")

  private val CustomerAlreadySignedUpEnum: String = "820"

  case class Error(processingDate: String,
                   code: String,
                   text: String)

  object Error {
    implicit val reads: Reads[Error] = Json.reads[Error]
  }

}
