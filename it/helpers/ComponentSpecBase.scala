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
import helpers.IntegrationTestConstants.testCreateIncomeSubmissionJson
import helpers.servicemocks.AuditStub.stubAuditing
import helpers.servicemocks.WireMockMethods
import models.lockout.LockoutRequest
import models.subscription.BusinessSubscriptionDetailsModel
import models.subscription.incomesource.SignUpRequest
import org.scalatest._
import org.scalatest.concurrent.{Eventually, IntegrationPatience, ScalaFutures}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json, Writes}
import play.api.libs.ws.WSResponse
import play.api.{Application, Environment, Mode}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec

trait ComponentSpecBase extends UnitSpec
  with GivenWhenThen with TestSuite
  with GuiceOneServerPerSuite with ScalaFutures with IntegrationPatience with Matchers with Assertions
  with WiremockHelper with BeforeAndAfterEach with BeforeAndAfterAll with Eventually
  with CustomMatchers with WireMockMethods {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .in(Environment.simple(mode = Mode.Dev))
    .configure(config)
    .build

  val mockHost = WiremockHelper.wiremockHost
  val mockPort = WiremockHelper.wiremockPort.toString
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
    "microservice.services.throttle-threshold" -> "2"
  )

  override def beforeAll(): Unit = {
    super.beforeAll()
    startWiremock()
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    resetWiremock()
    stubAuditing()
  }

  override def afterAll(): Unit = {
    stopWiremock()
    super.afterAll()
  }

  implicit val hc: HeaderCarrier = HeaderCarrier()

  object IncomeTaxSubscription {
    def getSubscriptionStatus(nino: String): WSResponse = get(s"/subscription/$nino")

    def get(uri: String): WSResponse = await(buildClient(uri).get())

    def createSubscription(body: SignUpRequest): WSResponse = post(s"/subscription-v2/${body.nino}", body)

    def signUp(nino: String): WSResponse = post(s"/mis/sign-up/${nino}", Json.parse("{}"))

    def businessIncomeSource(mtdbsaRef: String, body: JsValue): WSResponse = post(s"/mis/create/${mtdbsaRef}", body)

    def checkLockoutStatus(arn: String): WSResponse = get(s"/client-matching/lock/$arn")

    def lockoutAgent(arn: String, body: LockoutRequest): WSResponse = post(s"/client-matching/lock/$arn", body)

    def storeNino(token: String, nino: String): WSResponse = post(s"/identifier-mapping/$token", Json.obj("nino" -> nino))

    def getNino(token: String): WSResponse = get(s"/identifier-mapping/$token")

    def getRetrieveSelfEmployments(dataId: String): WSResponse = await(
      buildClient(s"/self-employments/id/$dataId")
        .withHttpHeaders("X-Session-ID" -> "testSessionId")
        .get()
    )

    def postInsertSelfEmployments(dataId: String, body: JsValue): WSResponse = await(
      buildClient(s"/self-employments/id/$dataId")
        .withHttpHeaders(
          "X-Session-ID" -> "testSessionId",
          "Content-Type" -> "application/json"
        )
        .post(body.toString())
    )

    def deleteDeleteAllSessionData: WSResponse = await(
      buildClient(s"/subscription-data/all")
       .withHttpHeaders("X-Session-ID" -> "testSessionId")
       .delete()
    )

    def getAllSelfEmployments: WSResponse = await(
      buildClient(s"/self-employments/all")
        .withHttpHeaders("X-Session-ID" -> "testSessionId")
        .get()
    )

    def post[T](uri: String, body: T)(implicit writes: Writes[T]): WSResponse = {
      await(
        buildClient(uri)
          .withHttpHeaders(
            "Content-Type" -> "application/json",
            ITSASessionKeys.RequestURI -> IntegrationTestConstants.requestUri
          )
          .post(writes.writes(body).toString())
      )
    }

  }

}
