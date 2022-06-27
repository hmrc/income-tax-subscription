/*
 * Copyright 2022 HM Revenue & Customs
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

package utils

import common.Constants
import models.ErrorModel
import models.frontend._
import models.lockout.LockoutRequest
import models.matching.LockoutResponse
import models.subscription._
import models.subscription.business.Cash
import models.subscription.property.PropertySubscriptionResponseModel
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.domain.Generator

import java.time.{Instant, LocalDate, OffsetDateTime, ZoneId}
import java.util.UUID

object TestConstants extends JsonUtils {

  lazy val testNino: String = new Generator().nextNino.nino
  // for the purpose of unit tests we only need a random string for the ARN
  lazy val testArn: String = new Generator().nextNino.nino
  lazy val testSafeId = "XE0001234567890"
  lazy val testMtditId = "mtditId001"
  lazy val testSourceId = "sourceId0001"
  lazy val testErrorReason = "Error Reason"
  lazy val testPreferencesToken: String = s"${UUID.randomUUID()}"

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

  def offsetDateTime: OffsetDateTime = OffsetDateTime.ofInstant(Instant.now, ZoneId.systemDefault())

  lazy val testLockoutRequest = LockoutRequest(timeoutSeconds = 10)
  lazy val testLockoutResponse = LockoutResponse(testArn, offsetDateTime)

  lazy val testLockoutSuccess = Right(Some(testLockoutResponse))
  lazy val testLockoutFailure = Left(ErrorModel(BAD_REQUEST, ""))
  lazy val testLockoutNone = Right(None)

  lazy val testException = new Exception("an error")


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

  object PaperlessPreferenceResponse {
    val successResponse: String => JsValue = (nino: String) =>
      s"""
         |{
         |    "identifiers": [
         |        {
         |            "name":"nino",
         |            "value":"$nino"
         |        }
         |    ]
         |}
         """.stripMargin
  }

  object NewRegistrationResponse {
    val successResponse: String => JsValue = (safeId: String) =>
      s"""
         | {
         |  "safeId": "$safeId",
         |  "agentReferenceNumber": "AARN1234567",
         |  "isEditable": true,
         |  "isAnAgent": false,
         |  "isAnIndividual": true,
         |  "individual": {
         |    "firstName": "Stephen",
         |    "lastName": "Wood",
         |    "dateOfBirth": "1990-04-03"
         |  },
         |  "address": {
         |    "addressLine1": "100 SuttonStreet",
         |    "addressLine2": "Wokingham",
         |    "addressLine3": "Surrey",
         |    "addressLine4": "London",
         |    "postalCode": "DH14EJ",
         |    "countryCode": "GB"
         |  },
         |  "contactDetails": {
         |    "primaryPhoneNumber": "01332752856",
         |    "secondaryPhoneNumber": "07782565326",
         |    "faxNumber": "01332754256",
         |    "emailAddress": "stephen@manncorpone.co.uk"
         |  }
         | }
         |
      """.stripMargin

    val failureResponse: (String, String) => JsValue = (code: String, reason: String) =>
      s"""
         |{
         |    "code": "$code",
         |    "reason":"$reason"
         |}
      """.stripMargin
  }

  object GetRegistrationResponse {
    val successResponse: String => JsValue = (safeId: String) =>
      s"""{
         |"sapNumber": "1234567890",
         |"safeId": "$safeId",
         |"agentReferenceNumber": "AARN1234567",
         |"nonUKIdentification":
         |{
         |"idNumber": "123456",
         |"issuingInstitution": "France Institution",
         |"issuingCountryCode": "FR"
         |},
         |"isEditable": true,
         |"isAnAgent": false,
         |"isAnIndividual": true,
         |"individual": {
         |"firstName": "Stephen",
         |"lastName": "Wood",
         |"dateOfBirth": "1990-04-03"
         |},
         |"addressDetails": {
         |"addressLine1": "100 SuttonStreet",
         |"addressLine2": "Wokingham",
         |"addressLine3": "Surrey",
         |"addressLine4": "London",
         |"postalCode": "DH14EJ",
         |"countryCode": "GB"
         |},
         |"contactDetails": {
         |"phoneNumber": "01332752856",
         |"mobileNumber": "07782565326",
         |"faxNumber": "01332754256",
         |"eMailAddress": "stephen@manncorpone.co.uk"
         |}
         |}
      """.stripMargin

    val failureResponse: String => JsValue = (reason: String) =>
      s"""
         |{
         |    "reason":"$reason"
         |}
      """.stripMargin
  }

  object BusinessSubscriptionResponse {
    def successResponse(safeId: String, mtditId: String, sourceId: String): JsValue =
      s"""{
         |  "safeId": "$safeId",
         |  "mtditId": "$mtditId",
         |  "incomeSources": [{
         |    "incomeSourceId": "$sourceId"
         |  }]
         |}
      """.stripMargin
  }

  object PropertySubscriptionResponse {
    def successResponse(safeId: String, mtditId: String, sourceId: String): JsValue =
      s"""
         |{
         | "safeId": "$safeId",
         | "mtditId": "$mtditId",
         | "incomeSource":
         | {
         |   "incomeSourceId": "$sourceId"
         | }
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


  object GovernmentGateway {
    val MTDITID = "MTDITID"
    val NINO = "NINO"
    val ggPortalId = "Default"
    val ggServiceName = "HMRC-MTD-IT"
    val ggFriendlyName = "Making Tax Digital Income Tax Self-Assessment enrolment"
  }

  val hmrcAsAgent = Constants.hmrcAsAgent

  val propertySubscriptionSuccess = PropertySubscriptionResponseModel(testSafeId, testMtditId, IncomeSourceModel(testSourceId))

  val feSuccessResponse = FESuccessResponse(Some(testMtditId))

  val testStartDate = LocalDate.now()

  val testEndDate = LocalDate.now().plusDays(1)


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
