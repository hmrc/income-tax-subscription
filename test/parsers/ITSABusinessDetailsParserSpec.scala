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
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NOT_FOUND, OK}
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

  "return NotSignedUp when status is NOT_FOUND" in {
    val response = HttpResponse(NOT_FOUND, Json.obj(), Map.empty)
    getITSABusinessDetailsResponseHttpReads.read("GET", "test-url", response) shouldBe NotSignedUp
  }

  "throw InternalServerException when mtdId is missing from JSON" in {
    val badJson = Json.obj("taxPayerDisplayResponse" -> Json.obj())
    val response = HttpResponse(OK, badJson, Map.empty)

    intercept[InternalServerException] {
      getITSABusinessDetailsResponseHttpReads.read("GET", "test-url", response)
    }.getMessage should include("Failure parsing json")
  }

  "throw InternalServerException when unsupported status is returned" in {
    val response = HttpResponse(INTERNAL_SERVER_ERROR, Json.obj(), Map.empty)

    intercept[InternalServerException] {
      getITSABusinessDetailsResponseHttpReads.read("GET", "test-url", response)
    }.getMessage should include("Unsupported status received")
  }

}
