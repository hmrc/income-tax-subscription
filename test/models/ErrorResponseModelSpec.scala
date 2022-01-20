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
import org.scalatest.Matchers
import play.api.libs.json.Json

class ErrorResponseModelSpec extends CommonSpec with Matchers {

  "errorResponse" should {
    "translate ErrorUnauthorized to error Json with only the required fields" in {
      Json.toJson {
        ErrorUnauthorized
      }.toString() shouldBe
        """{"code":"UNAUTHORIZED","message":"Bearer token is missing or not authorized"}"""
    }

    "translate ErrorNotFound to error Json with only the required fields" in {
      Json.toJson {
        ErrorNotFound
      }.toString() shouldBe
        """{"code":"NOT_FOUND","message":"Resource was not found"}"""
    }

    "translate ErrorGenericBadRequest to error Json with only the required fields" in {
      Json.toJson {
        ErrorGenericBadRequest
      }.toString() shouldBe
        """{"code":"BAD_REQUEST","message":"Bad Request"}"""
    }

    "translate ErrorAcceptHeaderInvalid to error Json with only the required fields" in {
      Json.toJson {
        ErrorAcceptHeaderInvalid
      }.toString() shouldBe
        """{"code":"ACCEPT_HEADER_INVALID","message":"The accept header is missing or invalid"}"""
    }

    "translate ErrorInternalServerError to error Json with only the required fields" in {
      Json.toJson {
        ErrorInternalServerError
      }.toString() shouldBe
        """{"code":"INTERNAL_SERVER_ERROR","message":"Internal server error"}"""
    }
  }
}
