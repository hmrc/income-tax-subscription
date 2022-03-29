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

package models

import common.CommonSpec
import org.scalatest.matchers.should.Matchers
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NOT_FOUND}
import play.api.libs.json.{JsValue, Json}

class ErrorModelSpec extends CommonSpec with Matchers {
  "Error model" should {
    "serialize an error without an error code to String" in {
      ErrorModel(NOT_FOUND, "NOT_FOUND").toString shouldBe "ErrorModel(404,NOT_FOUND)"
    }

    "serialize an error with an error code to String" in {
      ErrorModel(INTERNAL_SERVER_ERROR, "PARSE_ERROR", "{}").toString shouldBe "ErrorModel(500,PARSE_ERROR,{})"
    }

    "serialize an error with a parsing failure" in {
      val corruptResponse: JsValue = Json.obj("a" -> "not valid")
      ErrorModel(INTERNAL_SERVER_ERROR, ErrorModel.parseFailure(corruptResponse)).toString shouldBe "ErrorModel(500,PARSE_ERROR,{\"a\":\"not valid\"})"
    }
  }
}
