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
import play.api.http.Status.{NOT_FOUND, OK, SERVICE_UNAVAILABLE}
import play.api.libs.json.{JsError, JsSuccess}
import uk.gov.hmrc.http.HttpResponse

object HipPrePopParser extends Logging {

  type GetHipPrePopResponse = Either[ErrorModel, SelfEmpHolder]

  implicit object GetHipPrePopResponseHttpReads extends Parser[GetHipPrePopResponse] {
    override def read(correlationId: String, response: HttpResponse): GetHipPrePopResponse = {
      response.status match {
        case OK =>
          response.json.validate[SelfEmpHolder] match {
            case JsSuccess(value, _) => Right(value)
            case JsError(_) => Left(ErrorModel(OK, s"Failure parsing json response from prepop api"))
          }
        case NOT_FOUND | SERVICE_UNAVAILABLE =>
          Right(SelfEmpHolder(None))
        case status =>
          logger.error(s"[HipPrePopParser] - Unexpected status from pre-pop API. Status: $status")
          Left(ErrorModel(status, "Unexpected status returned from pre-pop api"))
      }
    }
  }

}
