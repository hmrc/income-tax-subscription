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

import play.api.http.Status.{OK, UNPROCESSABLE_ENTITY}
import play.api.libs.json.{JsError, JsSuccess, Reads}
import uk.gov.hmrc.http.{HttpReads, HttpResponse, InternalServerException}

object GetITSABusinessDetailsParser {

  sealed trait GetITSABusinessDetailsResponse

  case class AlreadySignedUp(mtdId: String) extends GetITSABusinessDetailsResponse

  object AlreadySignedUp {
    implicit val reads: Reads[AlreadySignedUp] = Reads[AlreadySignedUp](json =>
      (json \ "success" \ "taxPayerDisplayResponse" \ "mtdId").validate[String].map(AlreadySignedUp.apply)
    )
  }

  case object NotSignedUp extends GetITSABusinessDetailsResponse

  implicit val getITSABusinessDetailsResponseHttpReads: HttpReads[GetITSABusinessDetailsResponse] =
    (_: String, _: String, response: HttpResponse) => {
      response.status match {
        case OK =>
          response.json.validate[AlreadySignedUp] match {
            case JsSuccess(value, _) => value
            case JsError(_) => throw GetITSABusinessDetailsParserException("Failure parsing json", OK)
          }
        case UNPROCESSABLE_ENTITY =>
          (response.json \ "errors" \ "code").validate[String] match {
            case JsSuccess(IDNotFound, _) => NotSignedUp
            case JsSuccess(code, _) => throw GetITSABusinessDetailsParserException(s"Unsupported error code returned: $code", UNPROCESSABLE_ENTITY)
            case JsError(_) => throw GetITSABusinessDetailsParserException("Failure parsing json", UNPROCESSABLE_ENTITY)
          }
        case status =>
          throw GetITSABusinessDetailsParserException("Unsupported status received", status)
      }
    }

  private case class GetITSABusinessDetailsParserException(error: String, status: Int) extends InternalServerException(
    s"[GetITSABusinessDetailsParser] - $error - $status"
  )

  private val IDNotFound: String = "008"

}
