/*
 * Copyright 2024 HM Revenue & Customs
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

import models.subscription.Address
import play.api.Logging
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json._
import uk.gov.hmrc.http.InternalServerException

import scala.util.matching.Regex


case class PrePopData(selfEmployment: Option[Seq[PrePopSelfEmployment]])

object PrePopData {
  implicit val format: OFormat[PrePopData] = Json.format[PrePopData]
}

case class PrePopSelfEmployment(name: Option[String],
                                trade: Option[String],
                                address: Option[Address],
                                startDate: Option[DateModel])

object PrePopSelfEmployment extends Logging {

  private val dateRegex: Regex = "^([0-9]{4})-([0-9]{2})-([0-9]{2})$".r

  private val tradeMaxLength: Int = 35
  private val tradeMinLetters: Int = 2

  private def isValidPostcode(postcode: Option[String]): Option[String] = {
    val isValidPostcodeRegex: Regex = "^[A-Z]{1,2}[0-9][0-9A-Z]? ?[0-9][A-Z]{2}$".r
    postcode.flatMap {
      case isValidPostcodeRegex() => postcode
      case _ => None
    }
  }

  def fromApi(name: Option[String],
              trade: String,
              addressFirstLine: Option[String],
              addressPostcode: Option[String],
              startDate: Option[String]): PrePopSelfEmployment = {

    // Any characters not defined in this list will be matched on and replaced by single spaces
    val notAllowedCharactersRegex: String = """[^ A-Za-z0-9&'/\\.,-]"""
    val notAllowedPostcodeCharacters: String = "[^A-Za-z0-9 ]"
    val adjustedName = name.map(_.replaceAll(notAllowedCharactersRegex, " ").trim)
    val adjustedTrade = trade.replaceAll(notAllowedCharactersRegex, " ").trim
    val adjustedPostcode = addressPostcode.map(_.replaceAll(notAllowedPostcodeCharacters, "").trim.toUpperCase)

    PrePopSelfEmployment(
      name = adjustedName,
      trade = adjustedTrade match {
        case value if value.length <= tradeMaxLength && value.count(_.isLetter) >= tradeMinLetters => Some(value)
        case _ => None
      },
      address = (addressFirstLine, isValidPostcode(adjustedPostcode)) match {
        case (Some(firstLine), Some(postcode)) => Some(Address(Seq(firstLine), Some(postcode)))
        case (Some(_), None) =>
          logger.warn("[PrePopSelfEmployment] - Did not receive a postcode from the api.")
          None
        case _ => None
      },
      startDate = startDate map {
        case dateRegex(year, month, day) => DateModel(day = day, month = month, year = year)
        case invalid => throw new InternalServerException(s"[PrePopSelfEmployment] - Could not parse date received from api. Received: $invalid")
      }
    )
  }

  implicit val reads: Reads[PrePopSelfEmployment] = (
    (__ \ "businessName").readNullable[String] and
      (__ \ "businessDescription").read[String] and
      (__ \ "businessAddressFirstLine").readNullable[String] and
      (__ \ "businessAddressPostcode").readNullable[String] and
      (__ \ "dateBusinessStarted").readNullable[String]
    )(PrePopSelfEmployment.fromApi _)

  implicit val writes: OWrites[PrePopSelfEmployment] = Json.writes[PrePopSelfEmployment]

}