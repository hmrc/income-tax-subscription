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
import uk.gov.hmrc.http.HttpReads

import java.time.{Instant, ZoneId}
import java.time.format.DateTimeFormatter

trait Parser[T] extends Logging {

  val apiNumber: Int
  val apiName: String
  
  private val timePattern: DateTimeFormatter = DateTimeFormatter
    .ofPattern("HH:mm")
    .withZone(ZoneId.of("UTC"))

  def httpReads(correlationId: String): HttpReads[T]

  protected def error(correlationId: String,
                      status: Int,
                      maybeCode: Option[String] = None,
                      reason: String): String = {

    val errorMessage = Seq(
      s"API #$apiNumber: $apiName",
      s"Status: $status",
      maybeCode match {
        case Some(code) => s"Code: $code, Reason: $reason"
        case None => s"Message: $reason"
      }
    ).mkString(", ")

    logger.error(s"${timePattern.format(Instant.now())} -> $errorMessage, CorrelationId: $correlationId")
    errorMessage
  }
}
