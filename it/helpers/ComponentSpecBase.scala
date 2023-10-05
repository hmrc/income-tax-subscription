/*
 * Copyright 2018 HM Revenue & Customs
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

package helpers

import controllers.ITSASessionKeys
import helpers.servicemocks.AuditStub.stubAuditing
import helpers.servicemocks.WireMockMethods
import models.lockout.LockoutRequest
import org.scalatest._
import org.scalatest.concurrent.{Eventually, IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json, Writes}
import play.api.libs.ws.WSResponse
import play.api.{Application, Environment, Mode}
import uk.gov.hmrc.http.HeaderCarrier

trait ComponentSpecBase extends AnyWordSpecLike
  with OptionValues
  with GivenWhenThen with TestSuite
  with GuiceOneServerPerSuite with ScalaFutures with IntegrationPatience with Matchers with Assertions
  with WiremockHelper with BeforeAndAfterEach with BeforeAndAfterAll with Eventually
  with CustomMatchers with WireMockMethods {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .in(Environment.simple(mode = Mode.Dev))
    .configure(config)
    .build()

  val mockHost: String = WiremockHelper.wiremockHost
  val mockPort: String = WiremockHelper.wiremockPort.toString
  val mockUrl = s"http://$mockHost:$mockPort"

  def config: Map[String, String] = Map(
    "auditing.consumer.baseUri.host" -> mockHost,
    "auditing.consumer.baseUri.port" -> mockPort,
    "microservice.services.auth.host" -> mockHost,
    "microservice.services.auth.port" -> mockPort,
    "microservice.services.des.url" -> mockUrl,
    "microservice.services.gg-admin.host" -> mockHost,
    "microservice.services.gg-admin.port" -> mockPort,
    "microservice.services.government-gateway.host" -> mockHost,
    "microservice.services.government-gateway.port" -> mockPort,
    "microservice.services.gg-authentication.host" -> mockHost,
    "microservice.services.gg-authentication.port" -> mockPort,
    "microservice.services.throttle-threshold" -> "2",
    "throttle.testThrottle.max"-> "10",
    "throttle.zeroTestThrottle.max"-> "0",
    "throttle.oneTestThrottle.max" -> "1",
    "microservice.services.status-determination-service.host" -> mockHost,
    "microservice.services.status-determination-service.port" -> mockPort,
    "microservice.services.signup-tax-year-service.host" -> mockHost,
    "microservice.services.signup-tax-year-service.port" -> mockPort
  ) ++ overriddenConfig()

  def overriddenConfig(): Map[String, String] = Map.empty

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    startWiremock()
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    resetWiremock()
    stubAuditing()
  }

  override protected def afterAll(): Unit = {
    stopWiremock()
    super.afterAll()
  }

  implicit val hc: HeaderCarrier = HeaderCarrier()

  object IncomeTaxSubscription {
    def getSubscriptionStatus(nino: String): WSResponse = get(s"/subscription/$nino")

    def get(uri: String): WSResponse = authorisedClient(uri).get().futureValue

    def signUp(nino: String, taxYear: String): WSResponse = post(s"/mis/sign-up/$nino/$taxYear", Json.parse("{}"))

    def throttled(throttleId: String): WSResponse = post(s"/throttled?throttleId=$throttleId", Json.parse("{}"))

    def mandationStatus(body: JsValue): WSResponse = post("/itsa-status", body)

    def businessIncomeSource(mtdbsaRef: String, body: JsValue): WSResponse = post(s"/mis/create/$mtdbsaRef", body)

    def checkLockoutStatus(arn: String): WSResponse = authorisedClient(s"/client-matching/lock/$arn")
      .get()
      .futureValue

    def lockoutAgent(arn: String, body: LockoutRequest): WSResponse = post(s"/client-matching/lock/$arn", body)

    def storeNino(token: String, nino: String): WSResponse = post(s"/identifier-mapping/$token", Json.obj("nino" -> nino))

    def getNino(token: String): WSResponse = authorisedClient(s"/identifier-mapping/$token").get().futureValue

    def postUnauthorisedRetrieveReference(utr: String): WSResponse =
      buildClient(s"/subscription-data")
        .post(Json.obj("utr" -> utr))
        .futureValue

    def postRetrieveReference(utr: String): WSResponse =
      authorisedClient(s"/subscription-data")
        .post(Json.obj("utr" -> utr))
        .futureValue

    def getRetrieveSelfEmployments(reference: String, dataId: String): WSResponse =
      authorisedClient(s"/subscription-data/$reference/id/$dataId")
        .get()
        .futureValue

    def postInsertSelfEmployments(reference: String, dataId: String, body: JsValue): WSResponse =
      authorisedClient(s"/subscription-data/$reference/id/$dataId", "Content-Type" -> "application/json")
        .post(body.toString())
        .futureValue

    def deleteDeleteAllSessionData(reference: String): WSResponse =
      authorisedClient(s"/subscription-data/$reference/all")
        .delete()
        .futureValue

    def deleteSubscriptionData(reference: String, id: String): WSResponse =
      authorisedClient(s"/subscription-data/$reference/id/$id")
        .delete()
        .futureValue

    def getAllSelfEmployments(reference: String): WSResponse =
      authorisedClient(s"/subscription-data/$reference/all")
        .get()
        .futureValue

    def post[T](uri: String, body: T)(implicit writes: Writes[T]): WSResponse =
      buildClient(uri)
        .withHttpHeaders(
          "Content-Type" -> "application/json",
          "Authorization" -> "Bearer 123",
          ITSASessionKeys.RequestURI -> IntegrationTestConstants.requestUri
        )
        .post(writes.writes(body).toString())
        .futureValue

  }

  private def authorisedClient(path: String, extraHeaders: (String, String)*) = {
    val sessionId = "X-Session-ID" -> "testSessionId"
    val authorisation = "Authorization" -> "Bearer 123"
    val headers = extraHeaders.toList :+ sessionId :+ authorisation
    buildClient(path)
      .withHttpHeaders(headers:_*)
  }
}
