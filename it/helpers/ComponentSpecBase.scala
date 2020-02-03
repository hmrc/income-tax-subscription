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
import helpers.servicemocks.WireMockMethods
import models.lockout.LockoutRequest
import models.subscription.incomesource.SignUpRequest
import org.scalatest._
import org.scalatest.concurrent.{Eventually, IntegrationPatience, ScalaFutures}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{Json, Writes}
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
  }

  override def afterAll(): Unit = {
    stopWiremock()
    super.afterAll()
  }

  implicit val hc = HeaderCarrier()

  object IncomeTaxSubscription {
    def getSubscriptionStatus(nino: String): WSResponse = get(s"/subscription/$nino")

    def get(uri: String): WSResponse = await(buildClient(uri).get())

    def createSubscription(body: SignUpRequest): WSResponse = post(s"/subscription-v2/${body.nino}", body)

    def checkLockoutStatus(arn: String): WSResponse = get(s"/client-matching/lock/$arn")

    def lockoutAgent(arn: String, body: LockoutRequest): WSResponse = post(s"/client-matching/lock/$arn", body)

    def storeNino(token: String, nino: String): WSResponse = post(s"/identifier-mapping/$token", Json.obj("nino" -> nino))

    def getNino(token: String): WSResponse = get(s"/identifier-mapping/$token")

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
