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

import common.CommonSpec
import models.subscription._
import models.subscription.business.{Accruals, Cash, CreateIncomeSourceErrorModel, CreateIncomeSourceSuccessModel}
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{ControllerComponents, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, status, stubControllerComponents}
import services.mocks.{MockAuthService, MockIncomeSourcesConnector}
import uk.gov.hmrc.http.HeaderCarrier
import utils.TestConstants.testNino

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class BusinessIncomeSourcesControllerSpec extends CommonSpec
  with MockAuthService
  with MockIncomeSourcesConnector {

  "createIncomeSource" when {
    s"return a $NO_CONTENT response" when {
      "the income sources were successfully submitted" in {
        mockAgentAuthSuccess()
        mockCreateIncomeSources(Some(testArn), mtditid, testCreateIncomeSources)(
          Right(CreateIncomeSourceSuccessModel())
        )

        val result: Future[Result] = TestController.createIncomeSource(mtditid)(postSaveAndRetrieveRequest)

        status(result) shouldBe NO_CONTENT
      }
    }
    s"return a $INTERNAL_SERVER_ERROR response" when {
      "there was an error submitting" in {
        mockAgentAuthSuccess()
        mockCreateIncomeSources(Some(testArn), mtditid, testCreateIncomeSources)(
          Left(CreateIncomeSourceErrorModel(INTERNAL_SERVER_ERROR, "error"))
        )

        val result: Future[Result] = TestController.createIncomeSource(mtditid)(postSaveAndRetrieveRequest)

        status(result) shouldBe INTERNAL_SERVER_ERROR
        contentAsString(result) shouldBe "Create Income Sources Failure"
      }
    }
  }

  lazy val mockCC: ControllerComponents = stubControllerComponents()

  object TestController extends BusinessIncomeSourcesController(mockAuthService, mockItsaIncomeSourceConnector, mockCC)

  lazy implicit val hc: HeaderCarrier = HeaderCarrier()

  lazy val mtditid: String = "XAIT0000006"

  lazy val now: LocalDate = LocalDate.now

  lazy val testCreateIncomeSources: CreateIncomeSourcesModel = CreateIncomeSourcesModel(
    nino = testNino,
    soleTraderBusinesses = Some(SoleTraderBusinesses(
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
    )),
    ukProperty = Some(UkProperty(
      accountingPeriod = AccountingPeriodModel(now, now),
      startDateBeforeLimit = false,
      tradingStartDate = LocalDate.now,
      accountingMethod = Some(Accruals)
    )),
    overseasProperty = Some(OverseasProperty(
      accountingPeriod = AccountingPeriodModel(now, now),
      startDateBeforeLimit = false,
      tradingStartDate = LocalDate.now,
      accountingMethod = Some(Cash)
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

  lazy val postSaveAndRetrieveRequest: Request[JsValue] = FakeRequest().withBody(testCreateIncomeSourcesJson)


}
