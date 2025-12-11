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
import play.api.libs.json.{JsError, JsSuccess, Json, Reads}
import uk.gov.hmrc.http.{HttpResponse, InternalServerException}

object SignUpParser {

  type PostSignUpResponse = Either[ErrorModel, SignUpResponse]

  object HipSignUpResponseHttpReads extends Parser[PostSignUpResponse] {
    val apiNumber = 5317
    val apiDesc = "Sign-up"

    override def read(correlationId: String, response: HttpResponse): PostSignUpResponse = {
      response.status match {
        case CREATED => (response.json \ "success").validate[SignUpSuccess] match {
          case JsSuccess(value, _) => Right(value)
          case JsError(_) => jsonError(CREATED, correlationId)
        }
        case UNPROCESSABLE_ENTITY =>
          unprocessableEntity(response, correlationId)
        case FORBIDDEN =>
          throw SignUpParserException(s"Failed to read Json for MTD Sign Up Response", FORBIDDEN)
        case status =>
          generalError(status, s"Unexpected status returned: ${statuses.getDesc(status)}", correlationId)
      }
    }
    
    private def unprocessableEntity(response: HttpResponse, correlationId: String) =
      (response.json \\ "errors").map(_.validate[Error]).headOption match {
        case Some(JsSuccess(e, _)) => e.code match {
          case CustomerAlreadySignedUpEnum => Right(SignUpResponse.AlreadySignedUp)
          case _ => responseError(UNPROCESSABLE_ENTITY, e, correlationId)
        }
        case _ => jsonError(UNPROCESSABLE_ENTITY, correlationId)
      }

    private def jsonError(status: Int, correlationId: String) =
      generalError(status, "Failure parsing json response", correlationId)

    private def generalError(status: Int, message: String, correlationId: String) =
      Left(ErrorModel(status,
        super.error(
          apiNumber = apiNumber,
          apiDesc = apiDesc,
          correlationId = correlationId,
          status = status,
          reason = message
        )
      ))

    private def responseError(status: Int, e: Error, correlationId: String) =
      Left(ErrorModel(status,
        super.error(
          apiNumber = apiNumber,
          apiDesc = apiDesc,
          correlationId = correlationId,
          status = status,
          code = e.code,
          reason = e.text
        )
      ))
  }

  case class SignUpParserException(error: String, status: Int) extends InternalServerException(s"[SignUpParserException] - $error - $status")
  private val CustomerAlreadySignedUpEnum: String = "820"

  case class Error(
    processingDate: String,
    code: String,
    text: String
  )

  object Error {
    implicit val reads: Reads[Error] = Json.reads[Error]
  }
}
