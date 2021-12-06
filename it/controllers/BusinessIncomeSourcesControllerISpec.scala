
package controllers

import config.MicroserviceAppConfig
import config.featureswitch.{FeatureSwitching, SaveAndRetrieve}
import helpers.ComponentSpecBase
import helpers.IntegrationTestConstants._
import helpers.servicemocks.AuditStub.{stubAuditing, verifyAudit}
import helpers.servicemocks.{AuthStub, CreateIncomeSourceStub}
import models.subscription._
import models.subscription.business.{Accruals, Cash}
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NO_CONTENT, OK}
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WSResponse

import java.time.LocalDate


class BusinessIncomeSourcesControllerISpec extends ComponentSpecBase with FeatureSwitching {

  override def beforeEach(): Unit = {
    super.beforeEach()
    disable(SaveAndRetrieve)
  }

  val appConfig: MicroserviceAppConfig = app.injector.instanceOf[MicroserviceAppConfig]

  val now: LocalDate = LocalDate.now

  val testCreateIncomeSources: CreateIncomeSourcesModel = CreateIncomeSourcesModel(
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
            auditRef = "testAuditRef",
            address = Address(lines = Seq("line 1", "line 2"), postcode = "testPostcode")
          ))
        )
      )
    )),
    ukProperty = Some(UkProperty(
      accountingPeriod = AccountingPeriodModel(now, now),
      tradingStartDate = LocalDate.now,
      accountingMethod = Accruals
    )),
    overseasProperty = Some(OverseasProperty(
      accountingPeriod = AccountingPeriodModel(now, now),
      tradingStartDate = LocalDate.now,
      accountingMethod = Cash
    ))
  )

  val testCreateIncomeSourcesJson: JsValue = Json.obj(
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
            "auditRef" -> "testAuditRef",
            "address" -> Json.obj(
              "lines" -> Json.arr(
                "line 1",
                "line 2"
              ),
              "postcode" -> "testPostcode"
            )
          )
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
      "tradingStartDate" -> Json.obj(
        "day" -> now.getDayOfMonth.toString,
        "month" -> now.getMonthValue.toString,
        "year" -> now.getYear.toString
      ),
      "accountingMethod" -> "Cash"
    )
  )

  "POST /mis/create/mtditid" when {
    "the save and retrieve feature switch is disabled" should {
      s"return a $NO_CONTENT response" when {
        "income sources are successfully submitted" in {
          AuthStub.stubAuth(OK, Json.obj())
          stubAuditing()
          CreateIncomeSourceStub.stub(testMtdbsaRef, Json.toJson(testCreateIncomeSubmissionModel), appConfig.desAuthorisationToken, appConfig.desEnvironment)(
            OK, testCreateIncomeSuccessBody
          )

          val result: WSResponse = IncomeTaxSubscription.businessIncomeSource(testMtdbsaRef, testCreateIncomeSubmissionJson)

          result should have(
            httpStatus(NO_CONTENT)
          )
          verifyAudit()
        }
      }
      s"return a $INTERNAL_SERVER_ERROR" when {
        "the submission of income sources failed" in {
          AuthStub.stubAuth(OK, Json.obj())
          stubAuditing()
          CreateIncomeSourceStub.stub(testMtdbsaRef, Json.toJson(testCreateIncomeSubmissionModel), appConfig.desAuthorisationToken, appConfig.desEnvironment)(
            INTERNAL_SERVER_ERROR, testCreateIncomeFailureBody
          )

          val result: WSResponse = IncomeTaxSubscription.businessIncomeSource(testMtdbsaRef, testCreateIncomeSubmissionJson)

          result should have(
            httpStatus(INTERNAL_SERVER_ERROR),
            bodyOf("Business Income Source Failure")
          )
          verifyAudit()
        }
      }
    }
    "the save and retrieve feature switch is enabled" should {
      s"return a $NO_CONTENT response" when {
        "income sources are successfully submitted" in {
          enable(SaveAndRetrieve)
          AuthStub.stubAuth(OK, Json.obj())
          stubAuditing()
          CreateIncomeSourceStub.stub(testMtdbsaRef, Json.toJson(testCreateIncomeSources), appConfig.desAuthorisationToken, appConfig.desEnvironment)(
            OK, testCreateIncomeSuccessBody
          )

          val result: WSResponse = IncomeTaxSubscription.businessIncomeSource(testMtdbsaRef, testCreateIncomeSourcesJson)

          result should have(
            httpStatus(NO_CONTENT)
          )
          verifyAudit()
        }
      }
      s"return a $INTERNAL_SERVER_ERROR" when {
        "the submission of income sources failed" in {
          enable(SaveAndRetrieve)
          AuthStub.stubAuth(OK, Json.obj())
          stubAuditing()
          CreateIncomeSourceStub.stub(testMtdbsaRef, Json.toJson(testCreateIncomeSources), appConfig.desAuthorisationToken, appConfig.desEnvironment)(
            INTERNAL_SERVER_ERROR, testCreateIncomeFailureBody
          )

          val result: WSResponse = IncomeTaxSubscription.businessIncomeSource(testMtdbsaRef, testCreateIncomeSourcesJson)

          result should have(
            httpStatus(INTERNAL_SERVER_ERROR),
            bodyOf("Business Income Source Failure")
          )
          verifyAudit()
        }
      }
    }
  }
}
