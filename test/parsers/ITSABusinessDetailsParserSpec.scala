/*
 * Copyright 2025 HM Revenue & Customs
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

package parsers

import common.CommonSpec
import models.ErrorModel
import parsers.GetITSABusinessDetailsParser.{AlreadySignedUp, GetITSABusinessDetailsResponseHttpReads, NotSignedUp}
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK, UNPROCESSABLE_ENTITY}
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HttpResponse

import java.util.UUID

class ITSABusinessDetailsParserSpec extends CommonSpec {

  val testCorrelationId: String = UUID.randomUUID().toString

  val validJson: JsValue = Json.parse(
    """
      |{
      |   "success": {
      |     "taxPayerDisplayResponse" : {
      |       "mtdId": "XNIT00000068707"
      |       }
      |     }
      |}
          """.stripMargin)

  "GetITSABusinessDetailsParser" should {
    "return AlreadySignedUp when valid mtdId is present" in {
      val response = HttpResponse(OK, validJson, Map.empty)
      GetITSABusinessDetailsResponseHttpReads.httpReads(testCorrelationId).read("", "", response) shouldBe Right(AlreadySignedUp("XNIT00000068707"))
    }
  }

  "return NotSignedUp" when {
    "status is UNPROCESSABLE_ENTITY and has a code of 006" in {
      val json = Json.obj("errors" -> Json.obj("code" -> "006", "text" -> "006 error", "processingDate" -> ""))
      val response = HttpResponse(UNPROCESSABLE_ENTITY, json, Map.empty)

      GetITSABusinessDetailsResponseHttpReads.httpReads(testCorrelationId).read("", "", response) shouldBe Right(NotSignedUp)
    }

    "status is UNPROCESSABLE_ENTITY and has a code of 008" in {
      val json = Json.obj("errors" -> Json.obj("code" -> "008", "text" -> "", "processingDate" -> ""))
      val response = HttpResponse(UNPROCESSABLE_ENTITY, json, Map.empty)

      GetITSABusinessDetailsResponseHttpReads.httpReads(testCorrelationId).read("", "", response) shouldBe Right(NotSignedUp)
    }
  }

  "Return an error" when {
    "mtd id is missing from the OK response json" in {
      val badJson = Json.obj("taxPayerDisplayResponse" -> Json.obj())
      val response = HttpResponse(OK, badJson, Map.empty)

      GetITSABusinessDetailsResponseHttpReads.httpReads(testCorrelationId).read("", "", response) shouldBe Left(ErrorModel(
        OK, s"API #5266: Get Business Details, Status: $OK, Message: Failure parsing json response"
      ))
    }
    "unable to retrieve code from the UNPROCESSABLE_ENTITY response json" in {
      val json = Json.obj("errors" -> Json.obj())
      val response = HttpResponse(UNPROCESSABLE_ENTITY, json, Map.empty)

      GetITSABusinessDetailsResponseHttpReads.httpReads(testCorrelationId).read("", "", response) shouldBe Left(ErrorModel(
        UNPROCESSABLE_ENTITY, s"API #5266: Get Business Details, Status: $UNPROCESSABLE_ENTITY, Message: Failure parsing json response"
      ))

    }
    "code in the UNPROCESSABLE_ENTITY response json is not 006 or 008" in {
      val json = Json.obj("errors" -> Json.obj("code" -> "100", "text" -> "test", "processingDate" -> ""))
      val response = HttpResponse(UNPROCESSABLE_ENTITY, json, Map.empty)

      GetITSABusinessDetailsResponseHttpReads.httpReads(testCorrelationId).read("", "", response) shouldBe Left(ErrorModel(
        UNPROCESSABLE_ENTITY, s"API #5266: Get Business Details, Status: $UNPROCESSABLE_ENTITY, Code: 100, Reason: test"
      ))

    }
    "an unsupported status is returned" in {
      val response = HttpResponse(INTERNAL_SERVER_ERROR, Json.obj(), Map.empty)

      GetITSABusinessDetailsResponseHttpReads.httpReads(testCorrelationId).read("", "", response) shouldBe Left(ErrorModel(
        INTERNAL_SERVER_ERROR, s"API #5266: Get Business Details, Status: $INTERNAL_SERVER_ERROR, Message: Unexpected status returned"
      ))

    }
  }
}
