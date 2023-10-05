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

import models.{ErrorModel, SignUpResponse}
import play.api.http.Status.OK
import uk.gov.hmrc.http.{HttpReads, HttpResponse}


object SignUpParser {
  type PostSignUpResponse = Either[ErrorModel, SignUpResponse]

  implicit val signUpResponseHttpReads: HttpReads[PostSignUpResponse] = {
    (_: String, _: String, response: HttpResponse) =>
      response.status match {
        case OK => response.json.asOpt[SignUpResponse] match {
          case Some(successResponse) => Right(successResponse)
          case None => Left(ErrorModel(OK, "Failed to read Json for MTD Sign Up Response"))
        }
        case status => Left(ErrorModel(status, response.body))
      }
  }

}
