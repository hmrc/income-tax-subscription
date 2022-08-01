/*
 * Copyright 2022 HM Revenue & Customs
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
import models.status.MandationStatusResponse
import play.api.http.Status.OK
import play.api.libs.json.{JsError, JsSuccess}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object MandationStatusParser {
  type PostMandationStatusResponse = Either[ErrorModel, MandationStatusResponse]

  implicit val mandationStatusResponseHttpReads: HttpReads[PostMandationStatusResponse] =
    (_: String, _: String, response: HttpResponse) => {
      response.status match {
        case OK => response.json.validate[MandationStatusResponse] match {
          case JsSuccess(value, _) => Right(value)
          case JsError(errors) =>
            Left(ErrorModel(OK, s"Invalid Json for mandationStatusResponseHttpReads: $errors"))
        }
        case status => Left(ErrorModel(status, response.body))
      }
    }
}
