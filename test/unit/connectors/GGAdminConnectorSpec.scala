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

package unit.connectors

import models.gg.KnownFactsSuccessResponseModel
import uk.gov.hmrc.play.http.HeaderCarrier
import unit.connectors.mocks.MockGGAdminConnector
import utils.TestConstants.GG.KnownFactsResponse._
import utils.TestConstants.GG._

class GGAdminConnectorSpec extends MockGGAdminConnector {

  implicit val hc = HeaderCarrier()

  "GGAdminConnector.addKnownFacts" should {

    "Post to the correct url" in {
      TestGGAdminConnector.addKnownFactsUrl should endWith("service/ITSA/known-facts")
    }

    def result = await(TestGGAdminConnector.addKnownFacts(knowFactsRequest))

    "parse and return the success response correctly" in {
      mockAddKnownFacts(knowFactsRequest)(addKnownFactsSuccess)
      result shouldBe Right(KnownFactsSuccessResponseModel(1))
    }

    "parse and return the Bad request response correctly" in {
      mockAddKnownFacts(knowFactsRequest)(SERVICE_DOES_NOT_EXISTS)
      result shouldBe Left(SERVICE_DOES_NOT_EXISTS_MODEL)
    }

    "parse and return the Internal server error response correctly" in {
      mockAddKnownFacts(knowFactsRequest)(GATEWAY_ERROR)
      result shouldBe Left(GATEWAY_ERROR_MODEL)
    }

  }


}
