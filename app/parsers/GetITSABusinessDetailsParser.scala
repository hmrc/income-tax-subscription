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
import play.api.http.Status.{NOT_FOUND, OK}
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
        case OK => response.json.validate[AlreadySignedUp] match {
          case JsSuccess(value, _) => value
          case JsError(errors) =>
            throw new InternalServerException(s"[GetITSABusinessDetailsParser] - Failure parsing json. Errors: $errors")
        }
        case NOT_FOUND => NotSignedUp
        case status =>
          throw new InternalServerException(s"[GetITSABusinessDetailsParser] - Unsupported status received: $status")
      }
    }
}
