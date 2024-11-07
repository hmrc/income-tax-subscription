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

import models.{ErrorModel, PrePopData}
import play.api.Logging
import play.api.http.Status.{NOT_FOUND, OK}
import play.api.libs.json.{JsError, JsSuccess}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object PrePopParser extends Logging {

  type GetPrePopResponse = Either[ErrorModel, PrePopData]

  implicit object GetPrePopResponseHttpReads extends HttpReads[GetPrePopResponse] {
    override def read(method: String, url: String, response: HttpResponse): GetPrePopResponse = {
      response.status match {
        case OK =>
          response.json.validate[PrePopData] match {
            case JsSuccess(value, _) => Right(value)
            case JsError(_) => Left(ErrorModel(OK, s"Failure parsing json response from prepop api"))
          }
        case NOT_FOUND =>
          Right(PrePopData(None, None, None))
        case status =>
          logger.error(s"[PrePopParser] - Unexpected status from pre-pop API. Status: $status")
          Left(ErrorModel(status, "Unexpected status returned from pre-pop api"))
      }
    }
  }

}
