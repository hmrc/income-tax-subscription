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
import models.subscription.business.Cash
import models.{DateModel, ErrorModel}
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.domain.Generator
import utils.JsonUtils

import java.time.{Instant, LocalDate, OffsetDateTime, ZoneId}
import java.util.UUID

object IntegrationTestConstants extends JsonUtils {

  lazy val testNino = new Generator().nextNino.nino
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

  object GetBusinessDetailsResponse {
    val successResponse: (String, String, String) => JsValue = (nino: String, safeId: String, mtdbsa: String) =>
      s"""{
         |   "safeId": "$safeId",
         |   "nino": "$nino",
         |   "mtdbsa": "$mtdbsa",
         |   "propertyIncome": false,
         |   "businessData": [
         |      {
         |         "incomeSourceId": "123456789012345",
         |         "accountingPeriodStartDate": "2001-01-01",
         |         "accountingPeriodEndDate": "2001-01-01",
         |         "tradingName": "RCDTS",
         |         "businessAddressDetails": {
         |            "addressLine1": "100 SuttonStreet",
         |            "addressLine2": "Wokingham",
         |            "addressLine3": "Surrey",
         |            "addressLine4": "London",
         |            "postalCode": "DH14EJ",
         |            "countryCode": "GB"
         |         },
         |         "businessContactDetails": {
         |            "phoneNumber": "01332752856",
         |            "mobileNumber": "07782565326",
         |            "faxNumber": "01332754256",
         |            "emailAddress": "stephen@manncorpone.co.uk"
         |         },
         |         "tradingStartDate": "2001-01-01",
         |         "cashOrAccruals": "cash",
         |         "seasonal": true
         |      }
         |   ]
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
       |  "code":"$code",
       |  "reason":"$reason"
       |}
    """.stripMargin

  def testSignUpSubmission(nino: String): JsValue = Json.parse(
    s"""
       |{
       | "idType" : "NINO",
       | "idValue" : "$nino"
       |}
      """.stripMargin)

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

  val testCreateIncomeSubmissionModel: BusinessSubscriptionDetailsModel = BusinessSubscriptionDetailsModel(
    nino = "AA111111A",
    accountingPeriod = AccountingPeriodModel(LocalDate.now(), LocalDate.now()),
    selfEmploymentsData = Some(Seq()),
    accountingMethod = None,
    incomeSource = FeIncomeSourceModel(selfEmployment = false, ukProperty = true, foreignProperty = false),
    propertyStartDate = Some(PropertyStartDateModel(LocalDate.now())),
    propertyAccountingMethod = Some(AccountingMethodPropertyModel(Cash))
  )

  val testCreateIncomeSubmissionJson: JsValue = {
    val date = LocalDate.now()
    Json.parse(
      s"""
         | {
         | "nino": "AA111111A",
         |
         | "agentReferenceNumber": "1223456",
         |
         | "accountingPeriod": {
         |   "startDate": {
         |    "day": "${date.getDayOfMonth}",
         |    "month": "${date.getMonthValue}",
         |    "year": "${date.getYear}"
         |   },
         |   "endDate": {
         |    "day": "${date.getDayOfMonth}",
         |    "month": "${date.getMonthValue}",
         |    "year": "${date.getYear}"
         |   }
         |   },
         |   "selfEmploymentsData": [],
         |   "incomeSource": {
         |     "selfEmployment": false,
         |     "ukProperty": true,
         |     "foreignProperty":false
         |   },
         |   "propertyStartDate": {
         |     "startDate": {
         |    "day": "${date.getDayOfMonth}",
         |    "month": "${date.getMonthValue}",
         |    "year": "${date.getYear}"
         |   }
         |   },
         |   "propertyAccountingMethod": {
         |     "propertyAccountingMethod": "cash"
         |   }
         | }
         |""".stripMargin
    )
  }

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
