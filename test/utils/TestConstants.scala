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

package utils

import common.Constants
import models.ErrorModel
import models.frontend._
import models.lockout.LockoutRequest
import models.matching.LockoutResponse
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.domain.Generator

import java.time.{Instant, LocalDate}
import java.util.UUID

object TestConstants extends JsonUtils {

  lazy val testNino: String = new Generator().nextNino.nino
  lazy val testUtr: String = new Generator().nextAtedUtr.utr
  lazy val testTaxYear: String = "2023-24"
  // for the purpose of unit tests we only need a random string for the ARN
  lazy val testArn: String = new Generator().nextNino.nino
  lazy val testSafeId = "XE0001234567890"
  lazy val testMtditId = "mtditId001"
  lazy val testSourceId = "sourceId0001"
  lazy val testErrorReason = "Error Reason"
  lazy val testPreferencesToken: String = s"${UUID.randomUUID()}"
  lazy val now: LocalDate = LocalDate.now

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
  val corruptResponse: JsValue = """{"a": "not valid"}"""
  val CORRUPT = (BAD_REQUEST, corruptResponse)
  val CONFLICT_ERROR = (CONFLICT, failureResponse(CONFLICT_ERROR_MODEL.code.get, CONFLICT_ERROR_MODEL.reason))

  lazy val testLockoutRequest = LockoutRequest(timeoutSeconds = 10)
  lazy val testLockoutResponse = LockoutResponse(testArn, Instant.now)

  lazy val testLockoutSuccess = Right(Some(testLockoutResponse))
  lazy val testLockoutFailure = Left(ErrorModel(BAD_REQUEST, ""))
  lazy val testLockoutNone = Right(None)

  lazy val testException = new Exception("an error")

  def failureResponse(code: String, reason: String): JsValue =
    s"""
       |{
       |  "code":"$code",
       |  "reason":"$reason"
       |}
    """.stripMargin


  val hmrcAsAgent = Constants.hmrcAsAgent

  val feSuccessResponse = FESuccessResponse(Some(testMtditId))

  val testStartDate = LocalDate.now()

  val testEndDate = LocalDate.now().plusDays(1)

  def testTaxYearSignUpSubmission(nino: String, utr: String, taxYear: String): JsValue = Json.obj(
    "nino" -> nino,
    "utr" -> utr,
    "taxYear" -> taxYear
  )


  val testCreateIncomeSuccessBody: JsValue = Json.parse(
    """
      | {
      |   "incomeSourceId":"AAIS12345678901"
      | }
    """.stripMargin
  )

  val testCreateIncomeFailureBody: JsValue = Json.parse(
    """
      |{
      |  "mtdbsa": "XQIT00000000001"
      |}
    """.stripMargin
  )

}
