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

package utils

object Logging {

  val splunkString = "SPLUNK AUDIT:\n"

  val eventTypeRequest: String = "Request"
  val eventTypeSuccess: String = "Success"
  val eventTypeFailure: String = "Failure"
  val eventTypeBadRequest: String = "BadRequest"
  val eventTypeConflict: String = "Conflict"
  val eventTypeNotFound: String = "NotFound"
  val eventTypeInternalServerError: String = "InternalServerError"
  val eventTypeServerUnavailable: String = "ServerUnavailable"
  val eventTypeUnexpectedError: String = "UnexpectedError"

}

