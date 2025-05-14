/*
 * Copyright 2018 HM Revenue & Customs
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

package helpers

import common.Constants.hmrcAsAgent
import models.lockout.LockoutRequest
import models.subscription._
import models.subscription.business.{Accruals, Cash}
import models.{DateModel, ErrorModel}
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.domain.Generator
import utils.JsonUtils
import utils.TestConstants.testNino

import java.time.{Instant, LocalDate}
import java.util.UUID

object IntegrationTestConstants extends JsonUtils {

  lazy val testNino = new Generator().nextNino.nino
  lazy val testTaxYear = "2023-24"
  // for the purpose of unit tests we only need a random string for the ARN
  lazy val testArn: String = new Generator().nextNino.nino
  lazy val testSafeId = "XE0001234567890"
  lazy val testMtditId = "mtditId001"
  lazy val testSourceId = "sourceId0001"
  lazy val testMtdbsaRef = "XQIT00000000001"
  lazy val testErrorReason = "Error Reason"
  lazy val requestUri = "/"
  lazy val testPreferencesToken: String = s"${UUID.randomUUID()}"
  lazy val testTradingName: String = UUID.randomUUID().toString
  lazy val testStartDate: DateModel = LocalDate.now()
  lazy val testEndDate: DateModel = LocalDate.now().plusDays(2)

  object Audit {
    val testAuditType = "testAuditType"
    val testTransactionName = "testTransactionName"
    val testDetail = Map("foo" -> "bar")
    val agentServiceIdentifierKey = "AgentReferenceNumber"
    val agentServiceEnrolmentName = hmrcAsAgent
  }

  val INVALID_NINO_MODEL = ErrorModel(BAD_REQUEST, "INVALID_NINO", "Submission has not passed validation. Invalid parameter NINO.")
  val INVALID_PAYLOAD_MODEL = ErrorModel(BAD_REQUEST, "INVALID_PAYLOAD", "Submission has not passed validation. Invalid PAYLOAD.")
  val MALFORMED_PAYLOAD_MODEL = ErrorModel(BAD_REQUEST, "MALFORMED_PAYLOAD", "Invalid JSON message received.")
  val NOT_FOUND_NINO_MODEL = ErrorModel(NOT_FOUND, "NOT_FOUND_NINO", "The remote endpoint has indicated that no data can be found")
  val SERVER_ERROR_MODEL = ErrorModel(INTERNAL_SERVER_ERROR, "SERVER_ERROR", "DES is currently experiencing problems that require live service intervention.")
  val UNAVAILABLE_MODEL = ErrorModel(SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", "Dependent systems are currently not responding.")
  val CONFLICT_ERROR_MODEL = ErrorModel(CONFLICT, "CONFLICT", "Duplicated trading name.")

  val INVALID_NINO = (BAD_REQUEST, failureResponse(INVALID_NINO_MODEL.code.get, INVALID_NINO_MODEL.reason))
  val INVALID_PAYLOAD = (BAD_REQUEST, failureResponse(INVALID_PAYLOAD_MODEL.code.get, INVALID_PAYLOAD_MODEL.reason))
  val MALFORMED_PAYLOAD = (BAD_REQUEST, failureResponse(MALFORMED_PAYLOAD_MODEL.code.get, MALFORMED_PAYLOAD_MODEL.reason))
  val NOT_FOUND_NINO = (NOT_FOUND, failureResponse(NOT_FOUND_NINO_MODEL.code.get, NOT_FOUND_NINO_MODEL.reason))
  val SERVER_ERROR = (INTERNAL_SERVER_ERROR, failureResponse(SERVER_ERROR_MODEL.code.get, SERVER_ERROR_MODEL.reason))
  val UNAVAILABLE = (SERVICE_UNAVAILABLE, failureResponse(UNAVAILABLE_MODEL.code.get, UNAVAILABLE_MODEL.reason))
  val CONFLICT_ERROR = (CONFLICT, failureResponse(CONFLICT_ERROR_MODEL.code.get, CONFLICT_ERROR_MODEL.reason))

  // mongo uses millis, so we need to get an instant with millis
  val instant = Instant.ofEpochMilli(Instant.now().toEpochMilli)

  val testPropertyIncomeCash = Json.obj("cashAccrualsFlag" -> "C")
  val testPropertyIncomeAccruals = Json.obj("cashAccrualsFlag" -> "A")
  val testPropertyIncomeNone = Json.obj()

  val lockoutRequest = LockoutRequest(
    timeoutSeconds = 10
  )

  object GetITSABusinessDetailsResponse {
    val successResponse: (String, String) => JsValue = (nino: String, mtdbsa: String) =>
      Json.parse(
        s"""
           |{
           |   "success": {
           |      "taxPayerDisplayResponse": {
           |        "nino": "$nino",
           |        "mtdId": "$mtdbsa"
           |      }
           |   }
           |}
     """.stripMargin
      )

    val invalidSuccessResponse: JsValue =
      Json.parse(
        s"""
           |{
           |  "success": {
           |      "taxPayerDisplayResponse": {
           |        "nino": "AA123456A"
           |      }
           |   }
           |}
           |""".stripMargin
      )
  }
  object GetBusinessDetailsResponse {
    val successResponse: (String, String, String) => JsValue = (nino: String, safeId: String, mtdbsa: String) =>
      s"""{
         |   "taxPayerDisplayResponse": {
         |      "safeId": "$safeId",
         |      "nino": "$nino",
         |      "mtdId": "$mtdbsa"
         |  }
         |}
      """.stripMargin


    val failureResponse: (String, String) => JsValue = (code: String, reason: String) =>
      s"""
         |{
         |    "code": "$code",
         |    "reason":"$reason"
         |}
      """.stripMargin
  }

  def failureResponse(code: String, reason: String): JsValue =
    s"""
       |{
       |  "failures":[
       |    {
       |      "code":"$code",
       |      "reason":"$reason"
       |    }
       |  ]
       |}
    """.stripMargin

  def testTaxYearSignUpRequestBody(nino: String, taxYear: String): JsValue = Json.obj(
    "nino" -> nino,
    "signupTaxYear" -> taxYear
  )

  def testTaxYearSignUpRequestBodyWithUtr(nino: String, utr: String, taxYear:String): JsValue = Json.obj(
    "nino" -> nino,
    "utr" -> utr,
    "signupTaxYear" -> taxYear
  )

  val testSignUpSuccessBody: JsValue = Json.parse(
    """
      |{
      | "mtdbsa": "XQIT00000000001"
      |}
    """.stripMargin
  )

  val testSignUpInvalidBody: JsValue = Json.parse(
    """
      |{
      | "mtdbs": "XQIT00000000001"
      |}
    """.stripMargin
  )

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

  def hipTestTaxYearSignUpRequestBodyWithUtr(nino: String, utr: String, taxYear:String): JsValue = Json.parse(
    s"""
      |{
      |  "signUpMTDfB": {
      |    "nino": "$nino",
      |    "utr": "$utr",
      |    "signupTaxYear": "$taxYear"
      |  }
      |}
    """.stripMargin
  )

  val hipTestSignUpSuccessBody: JsValue = Json.parse(
    """
      |{
      |  "success": {
      |    "processingDate": "2022-01-31T09:26:17Z",
      |    "mtdbsa": "XQIT00000000001"
      |  }
      |}
    """.stripMargin
  )

  val hipTestSignUpInvalidBody: JsValue = Json.parse(
    """
      |{
      | "mtdbs": "XQIT00000000001"
      |}
    """.stripMargin
  )


  val testCreateIncomeSuccessBody: JsValue = Json.parse(
    """
      | {
      |	"incomeSourceId": "AAIS12345678901"
      |	}
    """.stripMargin
  )

  val testCreateIncomeFailureBody: JsValue = Json.parse(
    """
      |{
      | "mtdbsa": "XQIT00000000001"
      |}
    """.stripMargin
  )
}
