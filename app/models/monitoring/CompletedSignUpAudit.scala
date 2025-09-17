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

package models.monitoring

import models.subscription.CreateIncomeSourcesModel
import play.api.libs.json.{JsArray, JsObject, JsValue, Json}
import services.monitoring.ExtendedAuditModel
import utils.JsonUtils.JsObjectUtil

case class CompletedSignUpAudit(agentReferenceNumber: Option[String],
                                createIncomeSourcesModel: CreateIncomeSourcesModel,
                                urlHeaderAuthorization: String) extends ExtendedAuditModel {

  implicit class JsArrayUtil(jsArray: JsArray) {
    def ++(optJson: Option[JsObject]): JsArray = {
      optJson match {
        case Some(json) => jsArray :+ json
        case None => jsArray
      }
    }
  }

  val nino: String = createIncomeSourcesModel.nino
  val userType: String = if (agentReferenceNumber.isDefined) {
    "agent"
  } else {
    "individual"
  }

  val taxYear: String = (createIncomeSourcesModel.soleTraderBusinesses.map(_.accountingPeriod) orElse
    createIncomeSourcesModel.ukProperty.map(_.accountingPeriod) orElse
    createIncomeSourcesModel.overseasProperty.map(_.accountingPeriod))
    .map(accountingPeriod => s"${accountingPeriod.taxEndYear - 1}-${accountingPeriod.taxEndYear}").getOrElse("-")

  val soleTraderIncomeSources: Option[JsObject] = createIncomeSourcesModel.soleTraderBusinesses.map { soleTraderBusinesses =>
    Json.obj(
      "incomeSource" -> "selfEmployment",
      "businesses" -> soleTraderBusinesses.businesses.map { business =>
        Json.obj() ++
          business.businessStartDate.map(businessStartDate => Json.obj("businessCommencementDate" -> businessStartDate.startDate.toDesDateFormat)) ++
          business.businessName.map(businessName => Json.obj("businessName" -> businessName.businessName)) ++
          business.businessTradeName.map(businessTrade => Json.obj("businessTrade" -> businessTrade.businessTradeName)) ++
          business.businessAddress.map { businessAddress =>
            Json.obj(
              "businessAddress" -> Json.obj(
                "lines" -> businessAddress.address.lines,
                "postcode" -> businessAddress.address.postcode
              )
            )
          }
      },
      "numberOfBusinesses" -> soleTraderBusinesses.businesses.length.toString
    )
  }

  val ukPropertyIncomeSource: Option[JsObject] = createIncomeSourcesModel.ukProperty.map { ukProperty =>
    Json.obj(
      "incomeSource" -> "ukProperty",
      "commencementDate" -> ukProperty.tradingStartDate.toDesDateFormat
    )
  }

  val overseasPropertyIncomeSource: Option[JsObject] = createIncomeSourcesModel.overseasProperty.map { overseasProperty =>
    Json.obj(
      "incomeSource" -> "foreignProperty",
      "commencementDate" -> overseasProperty.tradingStartDate.toDesDateFormat
    )
  }

  val income: JsArray = Json.arr() ++ soleTraderIncomeSources ++ ukPropertyIncomeSource ++ overseasPropertyIncomeSource

  override val auditType: String = "mtdItsaSubscription"
  override val transactionName: Option[String] = Some("Customer-subscribed-to-send-quarterly-SA-income-tax-reports")

  override val detail: JsValue = Json.obj(
    "nino" -> nino,
    "userType" -> userType,
    "taxYear" -> taxYear,
    "income" -> income,
    "Authorization" -> urlHeaderAuthorization
  ) ++ agentReferenceNumber.map(value => Json.obj("agentReferenceNumber" -> value))

}


