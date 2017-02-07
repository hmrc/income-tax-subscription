/*
 * Copyright 2017 HM Revenue & Customs
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

package utils

import play.api.libs.json._
import uk.gov.hmrc.play.http.HttpResponse

trait JsonUtil extends Implicits {

  implicit def toJsValue[T](data: T)(implicit writer: Writes[T]): JsValue = Json.toJson(data)

  implicit def toJsValue(str: String): JsValue = Json.parse(str)

  implicit def parseUtil[T](jsValue: JsValue)(implicit reader: Reads[T]): JsResult[T] = Json.fromJson[T](jsValue)

  implicit def parseUtil[T](str: String)(implicit reader: Reads[T]): JsResult[T] = str: JsValue

  implicit def parseUtil[T](response: HttpResponse)(implicit reader: Reads[T]): JsResult[T] = response.body

  def parseAsLeft[L, R](jsValue: JsValue, parseError: L)(implicit lReader: Reads[L], rReader: Reads[R]): Either[L, R] = {
    val jsL: JsResult[L] = parseUtil[L](jsValue)
    jsL.fold(
      invalid => parseError,
      valid => valid
    )
  }

  def parse[L, R](jsValue: JsValue, parseError: L)(implicit lReader: Reads[L], rReader: Reads[R]): Either[L, R] = {
    val jsR: JsResult[R] = parseUtil[R](jsValue)
    jsR.fold(
      invalid => parseAsLeft[L, R](jsValue, parseError),
      valid => valid
    )
  }

}

object JsonUtil extends JsonUtil