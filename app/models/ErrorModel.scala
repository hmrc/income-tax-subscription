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

import play.api.http.Status
import play.api.libs.json.JsValue

trait ErrorResponsesModel {
  def code: Option[String]
  def reason: String
}

case class ErrorModel(status: Int, code: Option[String], reason: String) extends ErrorResponsesModel {
  override def toString: String = s"ErrorModel($status,${code.fold("")(x => x + ",")}$reason)"
}


object ErrorModel {
  def apply(status: Int, errorResponse: ErrorResponsesModel): ErrorModel = ErrorModel(status, errorResponse.code, errorResponse.reason)

  def apply(status: Int, message: String): ErrorModel = ErrorModel(status, None, message)

  def apply(status: Int, code: String, message: String): ErrorModel = ErrorModel(status, Some(code), message)

  lazy val parseFailure: JsValue => ErrorModel = (js: JsValue) => ErrorModel(Status.INTERNAL_SERVER_ERROR, "PARSE_ERROR", js.toString)
}
