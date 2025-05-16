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
import parsers.GetITSABusinessDetailsParser.{AlreadySignedUp, NotSignedUp, getITSABusinessDetailsResponseHttpReads}
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK, UNPROCESSABLE_ENTITY}
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.{HttpResponse, InternalServerException}

class ITSABusinessDetailsParserSpec extends CommonSpec {

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
      getITSABusinessDetailsResponseHttpReads.read("GET", "test-url", response) shouldBe AlreadySignedUp("XNIT00000068707")
    }
  }

  "return NotSignedUp when status is UNPROCESSABLE_ENTITY and has a code of 008" in {
    val json = Json.obj("errors" -> Json.obj("code" -> "008"))
    val response = HttpResponse(UNPROCESSABLE_ENTITY, json, Map.empty)
    getITSABusinessDetailsResponseHttpReads.read("GET", "test-url", response) shouldBe NotSignedUp
  }

  "throw an InternalServerException" when {
    "mtd id is missing from the OK response json" in {
      val badJson = Json.obj("taxPayerDisplayResponse" -> Json.obj())
      val response = HttpResponse(OK, badJson, Map.empty)

      intercept[InternalServerException] {
        getITSABusinessDetailsResponseHttpReads.read("GET", "test-url", response)
      }.getMessage shouldBe s"[GetITSABusinessDetailsParser] - Failure parsing json - $OK"
    }
    "unable to retrieve code from the UNPROCESSABLE_ENTITY response json" in {
      val json = Json.obj("errors" -> Json.obj())
      val response = HttpResponse(UNPROCESSABLE_ENTITY, json, Map.empty)

      intercept[InternalServerException] {
        getITSABusinessDetailsResponseHttpReads.read("GET", "/test-url", response)
      }.getMessage shouldBe s"[GetITSABusinessDetailsParser] - Failure parsing json - $UNPROCESSABLE_ENTITY"
    }
    "code in the UNPROCESSABLE_ENTITY response json is not 008" in {
      val json = Json.obj("errors" -> Json.obj("code" -> "006"))
      val response = HttpResponse(UNPROCESSABLE_ENTITY, json, Map.empty)

      intercept[InternalServerException] {
        getITSABusinessDetailsResponseHttpReads.read("GET", "/test-url", response)
      }.getMessage shouldBe s"[GetITSABusinessDetailsParser] - Unsupported error code returned: 006 - $UNPROCESSABLE_ENTITY"
    }
    "an unsupported status is returned" in {
      val response = HttpResponse(INTERNAL_SERVER_ERROR, Json.obj(), Map.empty)

      intercept[InternalServerException] {
        getITSABusinessDetailsResponseHttpReads.read("GET", "test-url", response)
      }.getMessage shouldBe s"[GetITSABusinessDetailsParser] - Unsupported status received - $INTERNAL_SERVER_ERROR"
    }
  }
}
