/*
 * Copyright 2021 HM Revenue & Customs
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

import models.{SignUpFailure, SignUpResponse}
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.play.test.UnitSpec

class SignUpParserSpec extends UnitSpec {

  "The sign up parser" when {

    "supplied with an OK response" should {

      "return a SignUpResponse when the response has valid json" in {
        val response = HttpResponse(OK, Some(Json.toJson(SignUpResponse("XAIT000000"))))

        SignUpParser.signUpResponseHttpReads.read("", "", response) shouldBe Right(SignUpResponse("XAIT000000"))
      }

      "return a SignUpFailure when the response has invalid json" in {
        val response = HttpResponse(OK, Some(Json.parse(
          """
            |{
            | "invalid" : "json"
            |}
          """.stripMargin)))

        SignUpParser.signUpResponseHttpReads.read("", "", response) shouldBe Left(SignUpFailure(OK, "Failed to read Json for MTD Sign Up Response"))
      }
    }

    "supplied with a non-OK response" should {

      "return a SignUpFailure with the response message" in {
        val response = HttpResponse(INTERNAL_SERVER_ERROR, responseString = Some("Error body"))

        SignUpParser.signUpResponseHttpReads.read("", "", response) shouldBe Left(SignUpFailure(INTERNAL_SERVER_ERROR,  "Error body"))
      }
    }
  }
}
