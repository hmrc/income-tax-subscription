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

import common.CommonSpec
import models.subscription._
import models.subscription.business.{Accruals, Cash}
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.Json

import java.time.LocalDate

class CompletedSignUpAuditSpec extends CommonSpec with Matchers {
  private val now: LocalDate = LocalDate.of(2021, 11, 26)

  private val testNino = "testNino"

  private val testSoleTraderBusinesses = SoleTraderBusinesses(
    accountingPeriod = AccountingPeriodModel(now, now),
    accountingMethod = Some(Cash),
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
  )

  private val testUkProperty = UkProperty(
    accountingPeriod = AccountingPeriodModel(now, now),
    startDateBeforeLimit = false,
    tradingStartDate = now,
    accountingMethod = Some(Accruals)
  )

  private val testOverseasProperty = OverseasProperty(
    accountingPeriod = AccountingPeriodModel(now, now),
    startDateBeforeLimit = false,
    tradingStartDate = now,
    accountingMethod = Some(Cash)
  )

  "CompletedSignUpAudit" when {
    "an agentReferenceNumber is provided" should {
      val testCompletedSignUpAudit = getCompletedSignUpAudit(agentReferenceNumber = Some("testRef"))

      "convert detail to JSON" in {
        testCompletedSignUpAudit.detail shouldBe Json.obj(
          "nino" -> testNino,
          "userType" -> "agent",
          "taxYear" -> "-",
          "agentReferenceNumber" -> "testRef",
          "income" -> Json.arr(),
          "Authorization" -> "test"
        )
      }
    }

    "an agentReferenceNumber is not provided" should {
      "convert detail to JSON" in {
        getCompletedSignUpAudit().detail shouldBe Json.obj(
          "nino" -> testNino,
          "userType" -> "individual",
          "taxYear" -> "-",
          "income" -> Json.arr(),
          "Authorization" -> "test"
        )
      }
    }

    "soleTraderBusinesses is provided" should {
      val testCreateIncomeSourcesModel =
        CreateIncomeSourcesModel(
          nino = testNino,
          soleTraderBusinesses = Some(testSoleTraderBusinesses)
        )

      val testCompletedSignUpAudit = getCompletedSignUpAudit(testCreateIncomeSourcesModel)

      "convert soleTraderIncomeSources to JSON" in {
        testCompletedSignUpAudit.soleTraderIncomeSources shouldBe
          Some(Json.obj(
          "incomeSource" -> "selfEmployment",
          "businesses" -> Json.arr(
            Json.obj(
              "businessCommencementDate"-> "2021-11-26",
              "businessTrade" -> "testBusinessTrade",
              "businessName" -> "testBusinessName",
              "businessAddress" -> Json.obj(
                "lines" -> Json.arr("line 1", "line 2"),
                "postcode" -> "testPostcode"
              )
            )
          ),
          "accountingType" -> "cash",
          "numberOfBusinesses" -> "1"
        ))
      }

      "convert detail to JSON" in {
        testCompletedSignUpAudit.detail shouldBe Json.obj(
          "nino" -> testNino,
          "userType" -> "individual",
          "taxYear" -> "2021-2022",
          "income" -> Json.arr(
            Json.obj(
              "incomeSource" -> "selfEmployment",
              "businesses" -> Json.arr(
                Json.obj(
                  "businessCommencementDate"-> "2021-11-26",
                  "businessTrade" -> "testBusinessTrade",
                  "businessName" -> "testBusinessName",
                  "businessAddress" -> Json.obj(
                    "lines" -> Json.arr("line 1", "line 2"),
                    "postcode" -> "testPostcode"
                  )
                )
              ),
              "accountingType" -> "cash",
              "numberOfBusinesses" -> "1"
            )
          ),
          "Authorization" -> "test"
        )
      }
    }

    "ukProperty is provided" should {
      val testCreateIncomeSourcesModel =
        CreateIncomeSourcesModel(
          nino = testNino,
          ukProperty = Some(testUkProperty)
        )

      val testCompletedSignUpAudit = getCompletedSignUpAudit(testCreateIncomeSourcesModel)

      "convert ukPropertyIncomeSource to JSON" in {
        testCompletedSignUpAudit.ukPropertyIncomeSource shouldBe
          Some(Json.obj(
            "incomeSource" -> "ukProperty",
            "commencementDate" -> "2021-11-26",
            "accountingType" -> "accruals"
          ))
      }

      "convert detail to JSON" in {
        testCompletedSignUpAudit.detail shouldBe Json.obj(
          "nino" -> testNino,
          "userType" -> "individual",
          "taxYear" -> "2021-2022",
          "income" -> Json.arr(Json.obj(
            "incomeSource" -> "ukProperty",
            "commencementDate" -> "2021-11-26",
            "accountingType" -> "accruals"
          )),
          "Authorization" -> "test"
        )
      }
    }

    "overseasProperty is provided" should {
      val testCreateIncomeSourcesModel =
        CreateIncomeSourcesModel(
          nino = testNino,
          overseasProperty = Some(testOverseasProperty)
        )

      val testCompletedSignUpAudit = getCompletedSignUpAudit(testCreateIncomeSourcesModel)

      "convert overseasPropertyIncomeSource to JSON" in {
        testCompletedSignUpAudit.overseasPropertyIncomeSource shouldBe
          Some(Json.obj(
            "incomeSource" -> "foreignProperty",
            "commencementDate" -> "2021-11-26",
            "accountingType" -> "cash"
          ))
      }

      "convert detail to JSON" in {
        testCompletedSignUpAudit.detail shouldBe Json.obj(
          "nino" -> testNino,
          "userType" -> "individual",
          "taxYear" -> "2021-2022",
          "income" -> Json.arr(Json.obj(
            "incomeSource" -> "foreignProperty",
            "commencementDate" -> "2021-11-26",
            "accountingType" -> "cash"
          )),
          "Authorization" -> "test"
        )
      }
    }
  }

  private def getCompletedSignUpAudit(
   testCreateIncomeSources: CreateIncomeSourcesModel = CreateIncomeSourcesModel(nino = testNino),
   agentReferenceNumber: Option[String] = None) = {
    CompletedSignUpAudit(
      agentReferenceNumber = agentReferenceNumber,
      createIncomeSourcesModel = testCreateIncomeSources,
      urlHeaderAuthorization = "test"
    )
  }
}
