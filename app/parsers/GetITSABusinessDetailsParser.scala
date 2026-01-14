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

package parsers

import models.ErrorModel
import parsers.hip.Parser
import play.api.http.Status.{FORBIDDEN, OK, UNPROCESSABLE_ENTITY}
import play.api.libs.json.*
import uk.gov.hmrc.http.{HttpReads, HttpResponse, InternalServerException}

object GetITSABusinessDetailsParser {

  sealed trait GetITSABusinessDetailsResponse

  case class AlreadySignedUp(
    mtdId: String,
    channel: Option[String]
  ) extends GetITSABusinessDetailsResponse

  object AlreadySignedUp {
    implicit val reads: Reads[AlreadySignedUp] = Reads[AlreadySignedUp](json =>
      val mtdId = (json \ "success" \ "taxPayerDisplayResponse" \ "mtdId").validate[String]
      val channel = (json \ "success" \ "taxPayerDisplayResponse" \ "channel").validate[String]
      mtdId match {
        case JsSuccess(mtdId, _) =>
          JsSuccess(AlreadySignedUp(
            mtdId,
            channel match {
              case JsSuccess(channel, _) => Some(channel)
              case JsError(_) => None
            }
          ))
        case JsError(errors) =>
          JsError(errors)
      }
    )
  }

  case object NotSignedUp extends GetITSABusinessDetailsResponse

  object GetITSABusinessDetailsResponseHttpReads extends Parser[Either[ErrorModel, GetITSABusinessDetailsResponse]] {
    val apiNumber = 5266
    val apiName = "Get Business Details"

    override def httpReads(correlationId: String): HttpReads[Either[ErrorModel, GetITSABusinessDetailsResponse]] = {
      (_: String, _: String, response: HttpResponse) => {
        response.status match {
          case OK => handleOkResponse(response.json, correlationId)
          case FORBIDDEN => handleForbiddenResponse(response.body, correlationId)
          case UNPROCESSABLE_ENTITY => handleUnprocessableEntityResponse(response.json, correlationId)
          case status => handleOtherResponse(status, correlationId)
        }
      }
    }

    private def handleOkResponse(json: JsValue, correlationId: String) = {
      json.validate[AlreadySignedUp] match {
        case JsSuccess(value, _) => Right(value)
        case JsError(_) => jsonError(OK, correlationId)
      }
    }

    private def handleForbiddenResponse(responseBody: String, correlationId: String): Nothing = {
      throw GetITSABusinessDetailsParserException(
        error = super.error(
          correlationId = correlationId,
          status = FORBIDDEN,
          reason = responseBody
        ),
        status = FORBIDDEN
      )
    }

    private def handleUnprocessableEntityResponse(json: JsValue, correlationId: String) = {
      (json \ "errors").validate[Error] match {
        case JsSuccess(error, _) =>
          error.code match {
            case SubscriptionDataNotFound | IdNotFound => Right(NotSignedUp)
            case _ => responseError(UNPROCESSABLE_ENTITY, error, correlationId)
          }
        case _ =>
          jsonError(UNPROCESSABLE_ENTITY, correlationId)
      }
    }

    private def handleOtherResponse(status: Int, correlationId: String) = {
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

    private def responseError(status: Int, errorModel: Error, correlationId: String): Left[ErrorModel, Nothing] =
      Left(ErrorModel(status,
        super.error(
          correlationId = correlationId,
          status = status,
          maybeCode = Some(errorModel.code),
          reason = errorModel.text
        )
      ))

  }

  case class GetITSABusinessDetailsParserException(error: String, status: Int) extends InternalServerException(
    s"[GetITSABusinessDetailsParser] - $error - $status"
  )

  case class Error(
                    processingDate: String,
                    code: String,
                    text: String
                  )

  object Error {
    implicit val reads: Reads[Error] = Json.reads[Error]
  }

  private val SubscriptionDataNotFound: String = "006"
  private val IdNotFound: String = "008"

}
