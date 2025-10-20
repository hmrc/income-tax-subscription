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

package connectors

import helpers.{ComponentSpecBase, WiremockHelper}
import helpers.IntegrationTestConstants.{testArn, testCreateIncomeFailureBody, testCreateIncomeSources, testMtdbsaRef}
import helpers.WiremockHelper.StubResponse
import helpers.servicemocks.{AuditStub, CreateIncomeSourceStub}
import models.subscription._
import models.subscription.business.{CreateIncomeSourceErrorModel, CreateIncomeSourceSuccessModel}
import play.api.http.Status.{CREATED, FORBIDDEN, INTERNAL_SERVER_ERROR}
import play.api.libs.json.Json
import play.api.mvc.Request
import play.api.test.FakeRequest
import utils.TestConstants.testCreateIncomeSuccessBody

class ItsaIncomeSourceConnectorISpec extends ComponentSpecBase{

  private lazy val connector: ItsaIncomeSourceConnector = app.injector.instanceOf[ItsaIncomeSourceConnector]
  implicit val request: Request[_] = FakeRequest()

  override def overriddenConfig(): Map[String, String] = Map(
    "auditing.enabled" -> "true"
  )

  "createIncomeSources" must {
    s"return a successful response when receiving a $CREATED response" in {

      CreateIncomeSourceStub.stubItsaIncomeSource(
        expectedBody = Json.toJson(testCreateIncomeSources)(CreateIncomeSourcesModel.hipWrites(testMtdbsaRef))
      )(status = CREATED, body = testCreateIncomeSuccessBody)

      val result = connector.createIncomeSources(
        agentReferenceNumber = Some(testArn),
        mtdbsaRef = testMtdbsaRef,
        createIncomeSources = testCreateIncomeSources
      )

      result.futureValue shouldBe Right(CreateIncomeSourceSuccessModel())
      AuditStub.verifyAudit()
    }

    s"should retry 2 times and return a successful response when receiving a $FORBIDDEN status" in {
      WiremockHelper.stubPostSequence(s"/etmp/RESTAdapter/itsa/taxpayer/income-source")(
        StubResponse(FORBIDDEN),
        StubResponse(FORBIDDEN),
        StubResponse(CREATED)
      )

      val result = connector.createIncomeSources(
        agentReferenceNumber = Some(testArn),
        mtdbsaRef = testMtdbsaRef,
        createIncomeSources = testCreateIncomeSources
      )

      result.futureValue shouldBe Right(CreateIncomeSourceSuccessModel())

      WiremockHelper.verifyPost(
        uri = s"/etmp/RESTAdapter/itsa/taxpayer/income-source",
        times = 3
      )
    }

    s"return a failure response when receiving a non-$CREATED response" in {
      CreateIncomeSourceStub.stubItsaIncomeSource(
        expectedBody = Json.toJson(testCreateIncomeSources)(CreateIncomeSourcesModel.hipWrites(testMtdbsaRef))
      )(status = INTERNAL_SERVER_ERROR, body = testCreateIncomeFailureBody)

      val result = connector.createIncomeSources(
        agentReferenceNumber = Some(testArn),
        mtdbsaRef = testMtdbsaRef,
        createIncomeSources = testCreateIncomeSources
      )

      result.futureValue shouldBe Left(CreateIncomeSourceErrorModel(
        status = INTERNAL_SERVER_ERROR,
        reason = testCreateIncomeFailureBody.toString()
      ))
    }
  }

}
