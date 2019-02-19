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

package models

import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import utils.Implicits._

trait ErrorResponsesModel {

  def code: Option[String]

  def reason: String

}

trait ErrorModel extends ErrorResponsesModel {

  def status: Int

  override def equals(obj: scala.Any): Boolean =
    obj match {
      case that: ErrorModel if that.status.equals(this.status) && that.code.equals(this.code) && that.reason.equals(this.reason) => true
      case _ => false
    }

  override def hashCode(): Int = {
    val prime = 37
    ((prime + status.hashCode) * prime + code.hashCode) * prime + reason.hashCode
  }

  override def toString: String = s"ErrorModel($status,${code.fold("")(x => x + ",")}$reason)"
}


object ErrorModel {

  private def newErrorModel(statusCode: Int, msgCode: Option[String], msg: String): ErrorModel = new ErrorModel {
    override def reason: String = msg

    override def code: Option[String] = msgCode

    override def status: Int = statusCode
  }

  def apply(status: Int, errorResponse: ErrorResponsesModel): ErrorModel = newErrorModel(status, errorResponse.code, errorResponse.reason)

  def apply(status: Int, message: String): ErrorModel = newErrorModel(status, None, message)

  def apply(status: Int, code: Option[String], message: String): ErrorModel = newErrorModel(status, code, message)

  def unapply(error: ErrorModel): Option[(Int, Option[String], String)] = Some((error.status, error.code, error.reason))

  lazy val parseFailure: JsValue => ErrorModel = (js: JsValue) => ErrorModel(Status.INTERNAL_SERVER_ERROR, "PARSE_ERROR", js.toString)

  implicit val format = Json.format[ErrorModel]

}
