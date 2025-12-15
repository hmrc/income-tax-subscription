/*
 * Copyright 2025 HM Revenue & Customs
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

package parsers.hip

import play.api.Logging
import uk.gov.hmrc.http.HttpResponse

abstract class Parser[T] extends Logging {
  def read(correlationId: String, response: HttpResponse): T

  protected final def error(
                             apiNumber: Int,
                             apiDesc: String,
                             correlationId: String,
                             status: Int,
                             code: String = "",
                             reason: String
                           ): String = {
    val start = s"API #$apiNumber: $apiDesc - Status: $status, "
    val middle = code match {
      case "" => s"Message: $reason"
      case _ => s"Code: $code, Reason: $reason"
    }
    val error = s"$start$middle"
    logger.error(s"$error, CorrelationId: $correlationId")
    error
  }

  protected val statuses = Map(
    400 -> "BAD_REQUEST",
    401 -> "UNAUTHORIZED",
    422 -> "UNPROCESSABLE_ENTITY",
    500 -> "INTERNAL_SERVER_ERROR",
    501 -> "NOT_IMPLEMENTED",
    502 -> "BAD_GATEWAY",
    503 -> "SERVICE_UNAVAILABLE"
  )

  implicit class Desc(map: Map[Int, String]) {
    def getDesc(status: Int): String =
      map.getOrElse(status, s"$status")
  }
}
