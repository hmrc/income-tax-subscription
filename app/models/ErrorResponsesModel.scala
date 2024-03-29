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

package models

import play.api.http.Status._
import play.api.libs.json.{JsValue, Json, Writes}

sealed abstract class ErrorResponseModel(
                                     val httpStatusCode: Int,
                                     val errorCode: String,
                                     val message: String)

object ErrorResponseModel {
  def writes(e: ErrorResponseModel): JsValue = Json.obj("code" -> e.errorCode, "message" -> e.message)
}

case object ErrorUnauthorized extends ErrorResponseModel(UNAUTHORIZED, "UNAUTHORIZED", "Bearer token is missing or not authorized") {
  implicit val implicitWrites: Writes[ErrorUnauthorized.type] = (error: ErrorUnauthorized.type) => {
    ErrorResponseModel.writes(error)
  }
}

case object ErrorNotFound extends ErrorResponseModel(NOT_FOUND, "NOT_FOUND", "Resource was not found") {
  implicit val implicitWrites: Writes[ErrorNotFound.type] = (error: ErrorNotFound.type) => {
    ErrorResponseModel.writes(error)
  }
}

case object ErrorGenericBadRequest extends ErrorResponseModel(BAD_REQUEST, "BAD_REQUEST", "Bad Request") {
  implicit val implicitWrites: Writes[ErrorGenericBadRequest.type] = (error: ErrorGenericBadRequest.type) => {
    ErrorResponseModel.writes(error)
  }
}

case object ErrorAcceptHeaderInvalid extends ErrorResponseModel(BAD_REQUEST, "ACCEPT_HEADER_INVALID", "The accept header is missing or invalid") {
  implicit val implicitWrites: Writes[ErrorAcceptHeaderInvalid.type] = (error: ErrorAcceptHeaderInvalid.type) => {
    ErrorResponseModel.writes(error)
  }
}

case object ErrorInternalServerError extends ErrorResponseModel(INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "Internal server error") {
  implicit val implicitWrites: Writes[ErrorInternalServerError.type] = (error: ErrorInternalServerError.type) => {
    ErrorResponseModel.writes(error)
  }
}
