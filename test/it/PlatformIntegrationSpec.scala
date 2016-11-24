package it

import com.github.tomakehurst.wiremock.client.WireMock._
import controllers.Documentation
import it.utils.{MicroserviceLocalRunSugar, WiremockServiceLocatorSugar}
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import play.api.test.FakeRequest
import uk.gov.hmrc.play.test.UnitSpec


/**
  * Testcase to verify the capability of integration with the API platform.
  *
  * 1, To integrate with API platform the service needs to register itself to the service locator by calling the /registration endpoint and providing
  * - application name
  * - application url
  *
  * 2a, To expose API's to Third Party Developers, the service needs to define the APIs in a definition.json and make it available under api/definition GET endpoint
  * 2b, For all of the endpoints defined in the definition.json a documentation.xml needs to be provided and be available under api/documentation/[version]/[endpoint name] GET endpoint
  *     Example: api/documentation/1.0/Fetch-Some-Data
  *
  * See: https://confluence.tools.tax.service.gov.uk/display/ApiPlatform/API+Platform+Architecture+with+Flows
  */
class PlatformIntegrationSpec extends UnitSpec with MockitoSugar with ScalaFutures with WiremockServiceLocatorSugar with BeforeAndAfter {

   before {
     startMockServer()
     stubRegisterEndpoint(204)
   }

   after {
     stopMockServer()
   }

   trait Setup {
     val documentationController = new Documentation {}
     val request = FakeRequest()
   }

   "microservice" should {

     "register itelf to service-locator" in new MicroserviceLocalRunSugar with Setup {
       override val additionalConfiguration: Map[String, Any] = Map(
         "appName" -> "application-name",
         "appUrl" -> "http://microservice-name.service",
         "microservice.services.service-locator.host" -> stubHost,
         "microservice.services.service-locator.port" -> stubPort
       )
       run {
         () => {
           verify(1,postRequestedFor(urlMatching("/registration")).
             withHeader("content-type", equalTo("application/json")).
             withRequestBody(equalTo(regPayloadStringFor("application-name", "http://microservice-name.service"))))
         }
       }
     }

     "provide definition endpoint and documentation endpoint for each api" in new MicroserviceLocalRunSugar with Setup {
       override val additionalConfiguration: Map[String, Any] = Map(
         "microservice.services.service-locator.host" -> stubHost,
         "microservice.services.service-locator.port" -> stubPort
       )
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

           val jsonResponse = jsonBodyOf(result).futureValue

           val versions: Seq[String] = (jsonResponse \\ "version") map (_.as[String])
           val endpointNames: Seq[Seq[String]] = (jsonResponse \\ "endpoints").map(_ \\ "endpointName").map(_.map(_.as[String]))

           versions.zip(endpointNames).flatMap { case (version, endpoint) => {
             endpoint.map(endpointName => (version, endpointName))
           }
           }.foreach { case (version, endpointName) => verifyDocumentationPresent(version, endpointName) }
         }
       }
     }

     "provide raml documentation" in new MicroserviceLocalRunSugar with Setup {
       override val additionalConfiguration: Map[String, Any] = Map(
         "microservice.services.service-locator.host" -> stubHost,
         "microservice.services.service-locator.port" -> stubPort
       )
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
