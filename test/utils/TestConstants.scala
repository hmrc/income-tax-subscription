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

import play.api.libs.json.JsValue
import uk.gov.hmrc.domain.Generator

import JsonUtils._

object TestConstants {
  lazy val testNino = new Generator().nextNino.nino
  lazy val testSafeId = "XE0001234567890"
  lazy val testMtditId = "mtditId001"
  lazy val testSourceId = "sourceId0001"
  lazy val testErrorReason = "Error Reason"

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

    def failureResponse(code: String, reason: String): JsValue =
      s"""
         |{
         |  "code":"$code",
         |  "reason":"$reason"
         |}
      """.stripMargin
  }

}
