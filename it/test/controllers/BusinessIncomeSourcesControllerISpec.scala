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

package controllers

import helpers.ComponentSpecBase
import helpers.IntegrationTestConstants._
import helpers.servicemocks.{AuthStub, CreateIncomeSourceStub}
import models.subscription._
import models.subscription.business.{Accruals, Cash}
import play.api.http.Status.{CREATED, INTERNAL_SERVER_ERROR, NO_CONTENT, OK}
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WSResponse
import utils.TestConstants.testNino

import java.time.LocalDate

class BusinessIncomeSourcesControllerISpec extends ComponentSpecBase {

  "POST /mis/create/mtditid" when {
    s"return a $NO_CONTENT response" when {
      "income sources are successfully submitted" in {
        AuthStub.stubAuth(OK)
        CreateIncomeSourceStub.stubItsaIncomeSource(Json.toJson(testCreateIncomeSources)(CreateIncomeSourcesModel.hipWrites(testMtdbsaRef))
        )(CREATED, testCreateIncomeSuccessBody)

        val result: WSResponse = IncomeTaxSubscription.businessIncomeSource(testMtdbsaRef, testCreateIncomeSourcesJson)

        result should have(httpStatus(NO_CONTENT))
      }
    }
    s"return a $INTERNAL_SERVER_ERROR response" when {
      "the submission of income sources failed" in {
        AuthStub.stubAuth(OK)
        CreateIncomeSourceStub.stubItsaIncomeSource(Json.toJson(testCreateIncomeSources)(CreateIncomeSourcesModel.hipWrites(testMtdbsaRef))
        )(INTERNAL_SERVER_ERROR, testCreateIncomeFailureBody)

        val result: WSResponse = IncomeTaxSubscription.businessIncomeSource(testMtdbsaRef, testCreateIncomeSourcesJson)

        result should have(
          httpStatus(INTERNAL_SERVER_ERROR),
          bodyOf("Create Income Sources Failure")
        )
      }
    }
  }

  lazy val now: LocalDate = LocalDate.now

  lazy val testCreateIncomeSources: CreateIncomeSourcesModel = CreateIncomeSourcesModel(
    nino = testNino,
    soleTraderBusinesses = Some(SoleTraderBusinesses(
      accountingPeriod = AccountingPeriodModel(now, now),
      accountingMethod = Cash,
      businesses = Seq(
        SelfEmploymentData(
          id = "testBusinessId",
          businessStartDate = Some(BusinessStartDate(now)),
          businessName = Some(BusinessNameModel("testBusinessName")),
          businessTradeName = Some(BusinessTradeNameModel("testBusinessTrade")),
          businessAddress = Some(BusinessAddressModel(
            address = Address(lines = Seq("line 1", "line 2"), postcode = Some("testPostcode"))
          )),
          startDateBeforeLimit = false
        )
      )
    )),
    ukProperty = Some(UkProperty(
      accountingPeriod = AccountingPeriodModel(now, now),
      startDateBeforeLimit = false,
      tradingStartDate = LocalDate.now,
      accountingMethod = Accruals
    )),
    overseasProperty = Some(OverseasProperty(
      accountingPeriod = AccountingPeriodModel(now, now),
      startDateBeforeLimit = false,
      tradingStartDate = LocalDate.now,
      accountingMethod = Cash
    ))
  )

  lazy val testCreateIncomeSourcesJson: JsValue = Json.obj(
    "nino" -> testNino,
    "soleTraderBusinesses" -> Json.obj(
      "accountingPeriod" -> Json.obj(
        "startDate" -> Json.obj(
          "day" -> now.getDayOfMonth.toString,
          "month" -> now.getMonthValue.toString,
          "year" -> now.getYear.toString
        ),
        "endDate" -> Json.obj(
          "day" -> now.getDayOfMonth.toString,
          "month" -> now.getMonthValue.toString,
          "year" -> now.getYear.toString
        )
      ),
      "accountingMethod" -> "Cash",
      "businesses" -> Json.arr(
        Json.obj(
          "id" -> "testBusinessId",
          "businessStartDate" -> Json.obj(
            "startDate" -> Json.obj(
              "day" -> now.getDayOfMonth.toString,
              "month" -> now.getMonthValue.toString,
              "year" -> now.getYear.toString
            )
          ),
          "businessName" -> Json.obj(
            "businessName" -> "testBusinessName"
          ),
          "businessTradeName" -> Json.obj(
            "businessTradeName" -> "testBusinessTrade"
          ),
          "businessAddress" -> Json.obj(
            "address" -> Json.obj(
              "lines" -> Json.arr(
                "line 1",
                "line 2"
              ),
              "postcode" -> "testPostcode"
            )
          ),
          "startDateBeforeLimit" -> false
        )
      )
    ),
    "ukProperty" -> Json.obj(
      "accountingPeriod" -> Json.obj(
        "startDate" -> Json.obj(
          "day" -> now.getDayOfMonth.toString,
          "month" -> now.getMonthValue.toString,
          "year" -> now.getYear.toString
        ),
        "endDate" -> Json.obj(
          "day" -> now.getDayOfMonth.toString,
          "month" -> now.getMonthValue.toString,
          "year" -> now.getYear.toString
        )
      ),
      "startDateBeforeLimit" -> false,
      "tradingStartDate" -> Json.obj(
        "day" -> now.getDayOfMonth.toString,
        "month" -> now.getMonthValue.toString,
        "year" -> now.getYear.toString
      ),
      "accountingMethod" -> "Accruals"
    ),
    "overseasProperty" -> Json.obj(
      "accountingPeriod" -> Json.obj(
        "startDate" -> Json.obj(
          "day" -> now.getDayOfMonth.toString,
          "month" -> now.getMonthValue.toString,
          "year" -> now.getYear.toString
        ),
        "endDate" -> Json.obj(
          "day" -> now.getDayOfMonth.toString,
          "month" -> now.getMonthValue.toString,
          "year" -> now.getYear.toString
        )
      ),
      "startDateBeforeLimit" -> false,
      "tradingStartDate" -> Json.obj(
        "day" -> now.getDayOfMonth.toString,
        "month" -> now.getMonthValue.toString,
        "year" -> now.getYear.toString
      ),
      "accountingMethod" -> "Cash"
    )
  )

}
