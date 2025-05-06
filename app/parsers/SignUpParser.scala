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

import models.SignUpResponse.SignUpSuccess
import models.{ErrorModel, SignUpResponse}
import play.api.http.Status._
import play.api.libs.json.{JsError, JsSuccess}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}


object SignUpParser {

  type PostSignUpResponse = Either[ErrorModel, SignUpResponse]

  implicit val signUpResponseHttpReads: HttpReads[PostSignUpResponse] = {
    (_: String, _: String, response: HttpResponse) =>
      response.status match {
        case OK => response.json.validate[SignUpSuccess] match {
          case JsSuccess(value, _) => Right(value)
          case JsError(_) => Left(ErrorModel(OK, s"Failed to read Json for MTD Sign Up Response"))
        }
        case UNPROCESSABLE_ENTITY => (response.json \ "failures" \\ "code").map(_.validate[String]).headOption match {
          case Some(JsSuccess(CustomerAlreadySignedUp, _)) => Right(SignUpResponse.AlreadySignedUp)
          case Some(JsSuccess(code, _)) => Left(ErrorModel(UNPROCESSABLE_ENTITY, code))
          case _ => Left(ErrorModel(UNPROCESSABLE_ENTITY, s"Failed to read Json for MTD Sign Up Response"))
        }
        case status => Left(ErrorModel(status, response.body))
      }
  }

  private val CustomerAlreadySignedUp: String = "CUSTOMER_ALREADY_SIGNED_UP"

  implicit val hipSignUpResponseHttpReads: HttpReads[PostSignUpResponse] = {
    (_: String, _: String, response: HttpResponse) =>
      response.status match {
        case CREATED => (response.json \ "success").validate[SignUpSuccess] match {
          case JsSuccess(value, _) => Right(value)
          case JsError(_) => Left(ErrorModel(CREATED, s"Failed to read Json for MTD Sign Up Response"))
        }
        case UNPROCESSABLE_ENTITY if (response.json \ "errors").isDefined =>
          (response.json \ "errors" \\ "code").map(_.validate[String]).headOption match {
            case Some(JsSuccess(code, _)) => code match {
              case CustomerAlreadySignedUpEnum => Right(SignUpResponse.AlreadySignedUp)
              case _ => Left(ErrorModel(UNPROCESSABLE_ENTITY, code))
            }
            case _ => Left(ErrorModel(UNPROCESSABLE_ENTITY, s"Failed to read Json for MTD Sign Up Response"))
          }
        case UNPROCESSABLE_ENTITY => Left(ErrorModel(UNPROCESSABLE_ENTITY, s"Failed to read Json for MTD Sign Up Response"))
        case status => Left(ErrorModel(status, response.body))
      }
  }

  private val CustomerAlreadySignedUpEnum: String = "820"


}
