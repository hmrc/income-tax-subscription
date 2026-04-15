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

import helpers.IntegrationTestConstants.{testArn, testCreateIncomeFailureBody, testCreateIncomeSources, testMtdbsaRef}
import helpers.WiremockHelper.StubResponse
import helpers.servicemocks.{AuditStub, CreateIncomeSourceStub}
import helpers.{ComponentSpecBase, WiremockHelper}
import models.DateModel
import models.subscription.*
import models.subscription.business.{CreateIncomeSourceErrorModel, CreateIncomeSourceSuccessModel}
import play.api.http.Status.{CREATED, FORBIDDEN, INTERNAL_SERVER_ERROR, UNPROCESSABLE_ENTITY}
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Request
import play.api.test.FakeRequest
import utils.TestConstants.testCreateIncomeSuccessBody

class ItsaIncomeSourceConnectorISpec extends ComponentSpecBase {

  private lazy val connector: ItsaIncomeSourceConnector = app.injector.instanceOf[ItsaIncomeSourceConnector]
  implicit val request: Request[_] = FakeRequest()

  override def overriddenConfig(): Map[String, String] = Map(
    "auditing.enabled" -> "true"
  )

  "createIncomeSources" must {
    "submit the country code if specified and GB if not" in {
      Map(
        None       -> "GB",
        Some("FR") -> "FR"
      ).foreach { entry =>
        val country = entry._1.map(Country(_, ""))
        val date = DateModel("1", "1", "2001")
        val data = CreateIncomeSourcesModel(
          nino = "",
          soleTraderBusinesses = Some(SoleTraderBusinesses(
            AccountingPeriodModel(date, date),
            Seq(SelfEmploymentData(
              id = "",
              businessName = Some(BusinessNameModel("")),
              businessTradeName = Some(BusinessTradeNameModel("")),
              startDateBeforeLimit = true,
              businessStartDate = Some(BusinessStartDate(date)),
              businessAddress = Some(BusinessAddressModel(Address(
                lines = Seq(""),
                postcode = None,
                country = country
              )))
            ))
          ))
        )
        val json = Json.toJson(data)(CreateIncomeSourcesModel.hipWrites("")).toString.replace(" ", "")
        json.contains(s"""countryCode":"${entry._2}""") shouldBe true
      }
    }

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
      AuditStub.verifyAudit()
    }

    s"return a failure response" when {
      s"receiving a $UNPROCESSABLE_ENTITY status response" which {
        "has a valid error json" in {
          CreateIncomeSourceStub.stubItsaIncomeSource(
            expectedBody = Json.toJson(testCreateIncomeSources)(CreateIncomeSourcesModel.hipWrites(testMtdbsaRef))
          )(status = UNPROCESSABLE_ENTITY, body = errorsJson)

          val result = connector.createIncomeSources(
            agentReferenceNumber = Some(testArn),
            mtdbsaRef = testMtdbsaRef,
            createIncomeSources = testCreateIncomeSources
          )

          result.futureValue shouldBe Left(CreateIncomeSourceErrorModel(
            status = UNPROCESSABLE_ENTITY,
            reason = s"API #5265: Create income sources, Status: $UNPROCESSABLE_ENTITY, Code: 000, Reason: error text"
          ))
          AuditStub.verifyAudit()
        }
        "has an invalid error json" in {
          CreateIncomeSourceStub.stubItsaIncomeSource(
            expectedBody = Json.toJson(testCreateIncomeSources)(CreateIncomeSourcesModel.hipWrites(testMtdbsaRef))
          )(status = UNPROCESSABLE_ENTITY, body = Json.obj())

          val result = connector.createIncomeSources(
            agentReferenceNumber = Some(testArn),
            mtdbsaRef = testMtdbsaRef,
            createIncomeSources = testCreateIncomeSources
          )

          result.futureValue shouldBe Left(CreateIncomeSourceErrorModel(
            status = UNPROCESSABLE_ENTITY,
            reason = s"API #5265: Create income sources, Status: $UNPROCESSABLE_ENTITY, Message: Failure parsing json response"
          ))
          AuditStub.verifyAudit()
        }
      }
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
        reason = s"API #5265: Create income sources, Status: $INTERNAL_SERVER_ERROR, Message: Unexpected status received"
      ))
      AuditStub.verifyAudit()
    }
  }

  lazy val errorsJson: JsObject = Json.obj(
    "errors" -> Json.obj("code" -> "000", "text" -> "error text", "processingDate" -> "")
  )

}
