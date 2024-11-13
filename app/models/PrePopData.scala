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
import models.subscription.business.{AccountingMethod, Accruals, Cash}
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{Json, OWrites, Reads, __}
import uk.gov.hmrc.http.InternalServerException

import scala.util.matching.Regex


case class PrePopData(selfEmployment: Option[Seq[PrePopSelfEmployment]],
                      ukPropertyAccountingMethod: Option[AccountingMethod],
                      foreignPropertyAccountingMethod: Option[AccountingMethod])

object PrePopData {

  private val toAccountingMethod: String => AccountingMethod = {
    case "C" => Cash
    case "A" => Accruals
    case method => throw new InternalServerException(s"[PrePopData] - Could not parse accounting method from api. Received: $method")
  }

  implicit val reads: Reads[PrePopData] = (
    (__ \ "selfEmployment").readNullable[Seq[PrePopSelfEmployment]] and
      (__ \ "ukProperty" \ "accountingMethod").readNullable[String].map(_.map(toAccountingMethod)) and
      (__ \ "foreignProperty" \ 0 \ "accountingMethod").readNullable[String].map(_.map(toAccountingMethod))
    )(PrePopData.apply _)

  implicit val writes: OWrites[PrePopData] = Json.writes[PrePopData]

}

case class PrePopSelfEmployment(name: Option[String],
                                trade: Option[String],
                                address: Option[Address],
                                startDate: Option[DateModel],
                                accountingMethod: AccountingMethod)

object PrePopSelfEmployment {

  private val dateRegex: Regex = "^([0-9]{4})-([0-9]{2})-([0-9]{2})$".r

  private val tradeMaxLength: Int = 35
  private val tradeMinLetters: Int = 2

  private def fromApi(name: Option[String],
                      trade: String,
                      addressFirstLine: Option[String],
                      addressPostcode: Option[String],
                      startDate: Option[String],
                      accountingMethod: String): PrePopSelfEmployment = {

    // Any characters not defined in this list will be matched on and replaced by single spaces
    val notAllowedCharactersRegex: String = """[^ A-Za-z0-9&'/\\.,-]"""
    val adjustedName = name.map(_.replaceAll(notAllowedCharactersRegex, " ").trim)
    val adjustedTrade = trade.replaceAll(notAllowedCharactersRegex, " ").trim

    PrePopSelfEmployment(
      name = adjustedName,
      trade = adjustedTrade match {
        case value if value.length <= tradeMaxLength && value.count(_.isLetter) >= tradeMinLetters => Some(value)
        case _ => None
      },
      address = addressFirstLine match {
        case Some(firstLine) => Some(Address(Seq(firstLine), addressPostcode))
        case _ => None
      },
      startDate = startDate map {
        case dateRegex(year, month, day) => DateModel(day = day, month = month, year = year)
      },
      accountingMethod = accountingMethod match {
        case "A" => Accruals
        case "C" => Cash
        case method => throw new InternalServerException(s"[PrePopSelfEmployment] - Could not parse accounting method from api. Received: $method")
      }
    )
  }

  implicit val reads: Reads[PrePopSelfEmployment] = (
    (__ \ "businessName").readNullable[String] and
      (__ \ "businessDescription").read[String] and
      (__ \ "businessAddressFirstLine").readNullable[String] and
      (__ \ "businessAddressPostcode").readNullable[String] and
      (__ \ "dateBusinessStarted").readNullable[String] and
      (__ \ "accountingMethod").read[String]
    )(PrePopSelfEmployment.fromApi _)

  implicit val writes: OWrites[PrePopSelfEmployment] = Json.writes[PrePopSelfEmployment]

}