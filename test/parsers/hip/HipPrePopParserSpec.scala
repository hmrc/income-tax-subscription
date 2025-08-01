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

package parsers.hip

import common.CommonSpec
import models.hip.{SelfEmp, SelfEmpHolder}
import models.subscription.Address
import models.subscription.business.Cash
import models.{DateModel, ErrorModel, PrePopData, PrePopSelfEmployment}
import parsers.hip.HipPrePopParser.GetHipPrePopResponse
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NOT_FOUND, OK, SERVICE_UNAVAILABLE}
import play.api.libs.json.{JsObject, JsValue, Json}
import uk.gov.hmrc.http.HttpResponse

class HipPrePopParserSpec extends CommonSpec {

  "HipPrePopParser" when {
    "an OK (200) status is returned" should {
      "parse pre-pop data successfully" when {
        "valid json is returned" in {
          read(OK, selfEmpJson) shouldBe Right(
            Seq(SelfEmpHolder(SelfEmp(
              Some("ABC"),
              Some("Plumbing"),
              Some("123 Street"),
              Some("ZZ1 1ZZ"),
              Some("1900-01-01")
            )),
          ))
        }
      }

      "fail to parse the pre-pop data" when {
        "invalid json is returned" in {
          read(OK, Json.obj("selfEmployment" -> Json.arr(Json.obj()))) shouldBe Left(ErrorModel(OK, "Failure parsing json response from prepop api"))
        }
      }
    }

    "a NOT_FOUND (404) status is returned" should {
      "return an empty prepop data set" in {
        read(NOT_FOUND) shouldBe Right(Seq.empty)
      }
    }

    "a SERVICE_UNAVAILABLE (503) status is returned" should {
      "return an empty prepop data set" in {
        read(SERVICE_UNAVAILABLE) shouldBe Right(Seq.empty)
      }
    }

    "a different status is returned" should {
      "return an unexpected status error model" in {
        read(INTERNAL_SERVER_ERROR) shouldBe Left(ErrorModel(INTERNAL_SERVER_ERROR, "Unexpected status returned from pre-pop api"))
      }
    }
  }

  def read(status: Int, json: JsValue = Json.obj()): GetHipPrePopResponse = {
    HipPrePopParser.GetHipPrePopResponseHttpReads.read("", "", HttpResponse(status, json.toString()))
  }

  lazy val selfEmpJson = Json.arr(Json.obj(
    "selfEmp" -> Json.obj(
      "businessName" -> "ABC",
      "businessDescription" -> "Plumbing",
      "businessAddressFirstLine" -> "123 Street",
      "businessAddressPostcode" -> "ZZ1 1ZZ",
      "dateBusinessStarted" -> "1900-01-01"
    )
  ))

}
