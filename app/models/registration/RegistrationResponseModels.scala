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

package models.registration

import models.ErrorResponsesModel
import play.api.libs.json._


case class RegistrationSuccessResponseModel(safeId: String)

case class NewRegistrationFailureResponseModel(code: Option[String], reason: String) extends ErrorResponsesModel

case class GetRegistrationFailureResponseModel(reason: String) extends ErrorResponsesModel {
  override def code: Option[String] = None
}

case class GetBusinessDetailsSuccessResponseModel(mtdbsa: String)

case class GetBusinessDetailsFailureResponseModel(code: Option[String], reason: String) extends ErrorResponsesModel

object RegistrationSuccessResponseModel {
  implicit val format = Json.format[RegistrationSuccessResponseModel]
}

object NewRegistrationFailureResponseModel {
  implicit val format = Json.format[NewRegistrationFailureResponseModel]
}

object GetRegistrationFailureResponseModel {
  implicit val format = Json.format[GetRegistrationFailureResponseModel]
}

object GetBusinessDetailsSuccessResponseModel {
  implicit val format = Json.format[GetBusinessDetailsSuccessResponseModel]
}

object GetBusinessDetailsFailureResponseModel {
  implicit val format = Json.format[GetBusinessDetailsFailureResponseModel]
}
