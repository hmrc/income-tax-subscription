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

package connectors.utilities

import models.{ErrorModel, ErrorResponsesModel}
import play.api.libs.json.{JsValue, Reads}
import utils.JsonUtils

trait ConnectorUtils[L <: ErrorResponsesModel, R] extends JsonUtils {

  type Response = Either[ErrorModel, R]

  def parseFailure(status: Int, jsValue: JsValue)(implicit lReader: Reads[L]): Left[ErrorModel, R] = Left (
    parseUtil[L](jsValue).fold(
      _ => ErrorModel.parseFailure(jsValue),
      valid => ErrorModel(status, valid)
    )
  )

  def parseSuccess(jsValue: JsValue)(implicit rReader: Reads[R]): Response  = {
    parseUtil[R](jsValue).fold(
      _ => Left(ErrorModel.parseFailure(jsValue)),
      valid => Right(valid)
    )
  }

}
