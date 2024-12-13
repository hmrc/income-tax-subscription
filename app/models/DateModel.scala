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

import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate
import java.time.format.{DateTimeFormatter, ResolverStyle}
import scala.language.implicitConversions

case class DateModel(day: String, month: String, year: String) {

  private val outputFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM uuuu").withResolverStyle(ResolverStyle.STRICT)
  private val desFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("uuuu-MM-dd").withResolverStyle(ResolverStyle.STRICT)
  private val auditFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("uuuu/MM/DD").withResolverStyle(ResolverStyle.STRICT)

  def toLocalDate: LocalDate = LocalDate.of(year.toInt, month.toInt, day.toInt)

  def toOutputDateFormat: String = toLocalDate.format(outputFormat)

  def toDesDateFormat: String = toLocalDate.format(desFormat)

  def toAuditFormat: String = toLocalDate.format(auditFormat)

  def diffInMonth(that: DateModel): Int = {
    import java.time.temporal.ChronoUnit
    ChronoUnit.MONTHS.between(this, that).toInt
  }
}

object DateModel {
  implicit def dateConvert(date: DateModel): LocalDate = date.toLocalDate

  implicit def dateConvert(date: LocalDate): DateModel = DateModel(date.getDayOfMonth.toString, date.getMonthValue.toString, date.getYear.toString)

  implicit val format: OFormat[DateModel] = Json.format[DateModel]
}
