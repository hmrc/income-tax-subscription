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

package models.registration

import models.ErrorResponsesModel
import play.api.libs.json._

case class OldGetBusinessDetailsSuccessResponseModel(mtdbsa: String)

case class GetBusinessDetailsSuccessResponseModel(mtdId: String)

case class GetBusinessDetailsFailureResponseModel(code: Option[String], reason: String) extends ErrorResponsesModel

object OldGetBusinessDetailsSuccessResponseModel {
  implicit val format: OFormat[OldGetBusinessDetailsSuccessResponseModel] = Json.format[OldGetBusinessDetailsSuccessResponseModel]
}

object GetBusinessDetailsSuccessResponseModel {
  implicit val reads: Reads[GetBusinessDetailsSuccessResponseModel] = Reads[GetBusinessDetailsSuccessResponseModel](json =>
    (json \ "taxPayerDisplayResponse" \ "mtdId").validate[String].map(GetBusinessDetailsSuccessResponseModel.apply)
  )
}

object GetBusinessDetailsFailureResponseModel {
  implicit val format: OFormat[GetBusinessDetailsFailureResponseModel] = Json.format[GetBusinessDetailsFailureResponseModel]
}
