/*
 * Copyright 2020 HM Revenue & Customs
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

import models.{SignUpFailure, SignUpResponse, SignUpResponseFailure}
import play.api.http.Status.OK
import uk.gov.hmrc.http.{HttpReads, HttpResponse}


object SignUpParser {
  type PostSignUpResponse = Either[SignUpResponseFailure, SignUpResponse]

  implicit val signUpResponseHttpReads: HttpReads[PostSignUpResponse] = {
    new HttpReads[PostSignUpResponse] {
      override def read(method: String, url: String, response: HttpResponse): PostSignUpResponse =
        response.status match {
          case OK => response.json.asOpt[SignUpResponse] match {
            case Some(successResponse) => Right(successResponse)
            case None => Left(SignUpFailure(OK, "Failed to read Json for MTD Sign Up Response"))
          }
          case status => Left(SignUpFailure(status, response.body))
        }
    }
  }

}
