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
import parsers.SignUpParser.Error
import parsers.hip.Parser
import play.api.http.Status.{OK, UNPROCESSABLE_ENTITY}
import play.api.libs.json.{JsError, JsSuccess, Json, Reads}
import uk.gov.hmrc.http.{HttpResponse, InternalServerException}

object GetITSABusinessDetailsParser {

  sealed trait GetITSABusinessDetailsResponse

  case class AlreadySignedUp(mtdId: String) extends GetITSABusinessDetailsResponse

  object AlreadySignedUp {
    implicit val reads: Reads[AlreadySignedUp] = Reads[AlreadySignedUp](json =>
      (json \ "success" \ "taxPayerDisplayResponse" \ "mtdId").validate[String].map(AlreadySignedUp.apply)
    )
  }

  case object NotSignedUp extends GetITSABusinessDetailsResponse

  object GetITSABusinessDetailsResponseHttpReads extends Parser[GetITSABusinessDetailsResponse] {

    val apiNumber = 5266
    val apiDesc = "Business-Details"
    
    override def read(correlationId: String, response: HttpResponse): Either[ErrorModel,GetITSABusinessDetailsResponse] = {
      response.status match {
        case OK => (response.json \ "success").validate[AlreadySignedUp] match {
          case JsSuccess(value, _) => Right(value)
          case JsError(_) => jsonError(OK, correlationId)
        }
        case UNPROCESSABLE_ENTITY =>
            unprocessableEntity(response, correlationId)
        case status =>
        generalError(status, s"Unexpected status returned: ${statuses.getDesc(status)}", correlationId)
      }
    }

    private def unprocessableEntity(response: HttpResponse, correlationId: String) =
      (response.json \\ "errors").map(_.validate[Error]).headOption match {
        case Some(JsSuccess(e, _)) => e.code match {
          case SubscriptionDataNotFound | IdNotFound => Right(NotSignedUp)
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
    
    private def responseError(status: Int, e: Error, correlationId: String): Left[ErrorModel, Nothing] =
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
