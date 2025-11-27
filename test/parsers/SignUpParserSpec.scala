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

package parsers

import common.CommonSpec
import models.ErrorModel
import models.SignUpResponse.{AlreadySignedUp, SignUpSuccess}
import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpResponse

class SignUpParserSpec extends CommonSpec {

  "The HIP sign up parser" when {

    "supplied with a CREATED response" should {

      "return a SignUpSuccess when the response has valid json" in {
        val response = HttpResponse(CREATED, body =
          """
            |{
            | "success" : {
            |   "processingDate": "2022-01-31T09:26:17Z",
            |   "mtdbsa": "XAIT000000"
            | }
            |}
          """.stripMargin)

        SignUpParser.HipSignUpResponseHttpReads.read("", response) shouldBe Right(SignUpSuccess("XAIT000000"))
      }

      "return a SignUpFailure when the response has invalid json" in {
        val response = HttpResponse(CREATED, body =
          """
            |{
            | "invalid" : "json"
            |}
          """.stripMargin)

        SignUpParser.HipSignUpResponseHttpReads.read("", response) shouldBe Left(ErrorModel(CREATED, "Failed to read Json for MTD Sign Up Response"))
      }
    }

    "supplied with a Unprocessable entity response" should {
      "return a already signed up when the response has a customer already signed up code" in {
        val response = HttpResponse(
          status = UNPROCESSABLE_ENTITY,
          body = Json.obj("errors" ->
            Json.obj("code" -> "820", "processingDate" -> "2022-01-31T09:26:17Z", "text" -> "CUSTOMER ALREADY SIGNED UP")
          ).toString()
        )

        SignUpParser.HipSignUpResponseHttpReads.read("", response) shouldBe Right(AlreadySignedUp)
      }

      "return a sign up failure when the response hs not got a customer already signed up code" in {
        val response = HttpResponse(
          status = UNPROCESSABLE_ENTITY,
          body = Json.obj("errors" ->
            Json.obj("code" -> "002", "processingDate" -> "2022-01-31T09:26:17Z", "text" -> "ID not found")
          ).toString()
        )

        SignUpParser.HipSignUpResponseHttpReads.read("", response) shouldBe
          Left(ErrorModel(UNPROCESSABLE_ENTITY, "002"))
      }

      "return a sign up failure when the response json has no code" in {
        val response = HttpResponse(UNPROCESSABLE_ENTITY, body = Json.obj().toString())

        SignUpParser.HipSignUpResponseHttpReads.read("", response) shouldBe
          Left(ErrorModel(UNPROCESSABLE_ENTITY, s"Failed to read Json for MTD Sign Up Response"))
      }
    }

    "supplied with a non OK or Unprocessable entity response" should {

      "return a SignUpFailure with the response message" in {
        val response = HttpResponse(INTERNAL_SERVER_ERROR, body = "Error body")

        SignUpParser.HipSignUpResponseHttpReads.read("", response) shouldBe Left(ErrorModel(INTERNAL_SERVER_ERROR, "Error body"))
      }
    }
  }
}
