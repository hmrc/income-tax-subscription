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

import play.api.libs.json.{JsObject, JsValue, Json}
import services.monitoring.ExtendedAuditModel

case class PrePopAuditModel(prePopData: PrePopData, nino: String, maybeArn: Option[String]) extends ExtendedAuditModel {

  override val auditType: String = "PrePopulated"
  override val transactionName: Option[String] = None

  val userType: String = if (maybeArn.isDefined) "agent" else "individual"

  val agentReferenceNumber: JsObject = maybeArn.map(arn => Json.obj("agentReferenceNumber" -> arn)).getOrElse(Json.obj())

  val selfEmployments: JsObject = prePopData.selfEmployment.map { selfEmployments =>
    Json.obj(
      "selfEmployments" -> selfEmployments.map { selfEmployment =>
        val name: JsObject = selfEmployment.name
          .map(name => Json.obj("name" -> name))
          .getOrElse(Json.obj())
        val description: JsObject = selfEmployment.trade
          .map(trade => Json.obj("description" -> trade))
          .getOrElse(Json.obj())
        val addressFirstLine: JsObject = selfEmployment.address
          .flatMap(_.lines.headOption)
          .map(firstLine => Json.obj("addressFirstLine" -> firstLine))
          .getOrElse(Json.obj())
        val addressPostcode: JsObject = selfEmployment.address
          .flatMap(_.postcode)
          .map(postcode => Json.obj("addressPostcode" -> postcode))
          .getOrElse(Json.obj())
        val startDate: JsObject = selfEmployment.startDate
          .map(startDate => Json.obj("startDate" -> startDate.toAuditFormat))
          .getOrElse(Json.obj())
        name ++ description ++ addressFirstLine ++ addressPostcode ++ startDate ++ Json.obj(
          "accountingMethod" -> selfEmployment.accountingMethod.stringValue
        )
      }
    )
  }.getOrElse(Json.obj())

  val ukProperty: JsObject = prePopData.ukPropertyAccountingMethod.map { accountingMethod =>
    Json.obj(
      "ukProperty" -> Json.obj(
        "accountingMethod" -> accountingMethod.stringValue
      )
    )
  }.getOrElse(Json.obj())

  val foreignProperty: JsObject = prePopData.foreignPropertyAccountingMethod.map { accountingMethod =>
    Json.obj(
      "overseasProperty" -> Json.obj(
        "accountingMethod" -> accountingMethod.stringValue
      )
    )
  }.getOrElse(Json.obj())


  val incomeSources: JsObject = {
    val allIncomeSources: JsObject = selfEmployments ++ ukProperty ++ foreignProperty

    if (allIncomeSources.values.nonEmpty) {
      Json.obj("incomeSources" -> allIncomeSources)
    } else {
      Json.obj()
    }
  }

  override val detail: JsValue = Json.obj(
    "userType" -> userType,
    "nino" -> nino
  ) ++ agentReferenceNumber ++ incomeSources

}
