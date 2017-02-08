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

package apiPlatformIntegration

import akka.stream.Materializer
import com.github.tomakehurst.wiremock.client.WireMock._
import controllers.DocumentationController
import org.scalatest.{BeforeAndAfter, TestData}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneAppPerSuite, OneAppPerTest}
import play.api.Application
import play.api.http.HttpErrorHandler
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.UnitSpec
import utils.{MicroserviceLocalRunSugar, WiremockServiceLocatorSugar}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Testcase to verify the capability of integration with the API platform.
  *
  * 1, To integrate with API platform the service needs to register itself to the service locator by calling the /registration endpoint and providing
  * - application name
  * - application url
  *
  * 2a, To expose API's to Third Party Developers, the service needs to define the APIs in a definition.json and make it available under api/definition GET endpoint
  * 2b, For all of the endpoints defined in the definition.json a documentation.xml needs to be provided and be available under api/documentation/[version]/[endpoint name] GET endpoint
  * Example: api/documentation/1.0/Fetch-Some-Data
  *
  * See: https://confluence.tools.tax.service.gov.uk/display/ApiPlatform/API+Platform+Architecture+with+Flows
  */
class PlatformIntegrationSpec extends UnitSpec with MockitoSugar with ScalaFutures with WiremockServiceLocatorSugar with BeforeAndAfter with OneAppPerTest {

  before {
    startMockServer()
    stubRegisterEndpoint(204)
  }

  after {
    stopMockServer()
  }

  val additionalConfiguration: Map[String, Any] = Map(
    "microservice.services.service-locator.host" -> stubHost,
    "microservice.services.service-locator.port" -> stubPort
  )

  implicit override def newAppForTest(testData: TestData): Application =
    new GuiceApplicationBuilder().configure(additionalConfiguration).build()

  trait Setup extends MicroserviceLocalRunSugar {
    val request = FakeRequest()

    implicit lazy val mat = app.injector.instanceOf[Materializer]
    lazy val httpErrorHandler = app.injector.instanceOf[HttpErrorHandler]
    lazy val documentationController = new DocumentationController(app, httpErrorHandler, mat)
  }

  "microservice" should {

    "register itelf to service-locator" in new Setup {
      run {
        () => {
          app.map { _ =>
            verify(1, postRequestedFor(urlMatching("/registration"))
              .withHeader("Content-Type", equalTo("application/json"))
              .withRequestBody(equalTo(regPayloadStringFor("income-tax-subscription", "http://income-tax-subscription.service")))
            )
          }
        }
      }
    }

    "provide definition endpoint and documentation endpoint for each api" in new Setup {
      run {
        () => {
          def normalizeEndpointName(endpointName: String): String = endpointName.replaceAll(" ", "-")

          def verifyDocumentationPresent(version: String, endpointName: String) {
            withClue(s"Getting documentation version '$version' of endpoint '$endpointName'") {
              val documentationResult = documentationController.documentation(version, endpointName)(request)
              status(documentationResult) shouldBe 200
            }
          }

          val result = documentationController.definition()(request)
          status(result) shouldBe 200
          val r = await(result)

          val jsonResponse = jsonBodyOf(result)(mat).futureValue

          val versions: Seq[String] = (jsonResponse \\ "version") map (_.as[String])
          val endpointNames: Seq[Seq[String]] = (jsonResponse \\ "endpoints").map(_ \\ "endpointName").map(_.map(_.as[String]))

          versions.zip(endpointNames).flatMap { case (version, endpoint) => {
            endpoint.map(endpointName => (version, endpointName))
          }
          }.foreach { case (version, endpointName) => verifyDocumentationPresent(version, endpointName) }
        }
      }
    }

    "provide raml documentation" in new Setup {
      run {
        () => {
          val result = documentationController.raml("1.0", "application.raml")(request)

          status(result) shouldBe 200
          bodyOf(result).futureValue should startWith("#%RAML 1.0")
        }
      }
    }
  }
}
