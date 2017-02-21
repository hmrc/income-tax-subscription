/*
 * Copyright 2017 HM Revenue & Customs
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

import models.frontend.{Both, Business, FERequest, Property}
import models.gg.{KnownFactsRequest, TypeValuePair}
import models.registration.RegistrationRequestModel
import models.subscription.business.{BusinessDetailsModel, BusinessSubscriptionRequestModel}
import models.{DateModel, ErrorModel}
import play.api.http.Status._
import play.api.libs.json.JsValue
import uk.gov.hmrc.domain.Generator
import utils.JsonUtils._

object TestConstants {

  lazy val testNino = new Generator().nextNino.nino
  lazy val testSafeId = "XE0001234567890"
  lazy val testMtditId = "mtditId001"
  lazy val testSourceId = "sourceId0001"
  lazy val testErrorReason = "Error Reason"

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

  val fePropertyRequest = FERequest(
    nino = testNino,
    incomeSource = Property,
    isAgent = false
  )

  val feBusinessRequest = FERequest(
    nino = testNino,
    incomeSource = Business,
    isAgent = false,
    accountingPeriodStart = DateModel("01", "05", "2017"),
    accountingPeriodEnd = DateModel("30", "04", "2018"),
    tradingName = "Test Business",
    cashOrAccruals = "cash"
  )

  val feBothRequest = FERequest(
    nino = testNino,
    incomeSource = Both,
    isAgent = false,
    accountingPeriodStart = DateModel("01", "05", "2017"),
    accountingPeriodEnd = DateModel("30", "04", "2018"),
    tradingName = "Test Business",
    cashOrAccruals = "cash"
  )

  val businessSubscriptionRequestPayload = BusinessSubscriptionRequestModel(
    List(BusinessDetailsModel(
      accountingPeriodStartDate = "2017-05-01",
      accountingPeriodEndDate = "2018-04-30",
      tradingName = "Test Business",
      cashOrAccruals = "cash"
    ))
  )

  val registerRequestPayload = RegistrationRequestModel(isAnAgent = false)


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

  object GG {

    val knowFactsRequest = KnownFactsRequest(
      List(
        TypeValuePair(TypeValuePairExamples.testType1, TypeValuePairExamples.testValue1),
        TypeValuePair(TypeValuePairExamples.testType2, TypeValuePairExamples.testValue2)
      )
    )

    object KnownFactsResponse {

      def successResponse(line: Int): JsValue =
        s"""{
           | "linesUpdated" : $line
           | }""".stripMargin

      def failureResponse(statusCode: Int, message: String): JsValue =
        s"""{
           | "statusCode" : $statusCode,
           | "message" : "$message"
           | }""".stripMargin


      val SERVICE_DOES_NOT_EXISTS_MODEL = ErrorModel(BAD_REQUEST, "The service specified does not exist")
      val GATEWAY_ERROR_MODEL = ErrorModel(INTERNAL_SERVER_ERROR, "Authentication successful, but error accessing user information with Gateway token")

      val SERVICE_DOES_NOT_EXISTS = (BAD_REQUEST, failureResponse(BAD_REQUEST, SERVICE_DOES_NOT_EXISTS_MODEL.reason))
      val GATEWAY_ERROR= (INTERNAL_SERVER_ERROR, failureResponse(INTERNAL_SERVER_ERROR, GATEWAY_ERROR_MODEL.reason))

    }

    object TypeValuePairExamples {
      val testType1 = "MOSW2Number"
      val testValue1 = "10"
      val testType2 = "MOSW2ID"
      val testValue2 = "A"

      def jsonTypeValuePair(testType: String, testValue: String): JsValue =
        s"""{"type" : "$testType",
           | "value" : "$testValue"
           | }""".stripMargin
    }

    object EnrolRequestExamples {
      val portalId = "MOSW"
      val serviceName = "MOSW5"
      val friendlyName = "Main Enrolment"
      val knownFact1 = "DV200L"
      val knownFact2 = "13 66GH"

      def jsonEnrolRequest(portalId: String, serviceName: String, friendlyName: String, knownFacts: List[String]): JsValue =
        s"""{
           |     "portalId": "$portalId",
           |     "serviceName": "$serviceName",
           |     "friendlyName": "$friendlyName",
           |     "knownFacts": [
           |        ${knownFacts.map(x =>s""" "$x" """).mkString(",")}
           |      ]
           |}""".stripMargin
    }

    object EnrolResponseExamples {
      val serviceName = "MOSW5"
      val state = "NotYetActivated"
      val friendlyName = ""
      val testType1 = "MOSW5PostCode"
      val testValue1 = "13 9DF"
      val testType2 = "MOSW5Reference"
      val testValue2 = "DV200L"

      def jsonEnrolResponse(serviceName: String, state: String, friendlyName: String, identifier: List[TypeValuePair]): JsValue =
        s"""{
           |     "serviceName": "$serviceName",
           |     "state": "$state",
           |     "friendlyName": "$friendlyName",
           |     "identifiers": [
           |        ${identifier.map(x => s"""{ "type" : "${x.`type`}", "value" : "${x.value}"}""").mkString(",")}
           |      ]
           |}""".stripMargin
    }

  }

  object Authenticator {

    val refreshFailure: (Int, Option[JsValue]) = (BAD_REQUEST, """{ "reason" : "Bearer token missing or invalid, or GG-token has expired" }""": JsValue)

  }

}
