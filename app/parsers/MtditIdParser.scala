/*
 * Copyright 2019 HM Revenue & Customs
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

import play.api.http.Status.OK
import play.api.libs.json.JsSuccess
import uk.gov.hmrc.http.{HttpReads, HttpResponse, InternalServerException}

object MtditIdParser {

  implicit object MtditIdHttpReads extends HttpReads[String] {
    override def read(method: String, url: String, response: HttpResponse): String =
      response.status match {
        case OK =>
          (response.json \ "mtditId").validate[String] match {
            case JsSuccess(mtditId, _) =>
              mtditId
            case _ => throw new InternalServerException("MTDITID missing from DES response")
          }
        case status =>
          throw new InternalServerException(s"DES returned $status with response ${response.body}")
      }
  }

}
