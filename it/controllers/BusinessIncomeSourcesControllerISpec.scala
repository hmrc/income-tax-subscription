
package controllers

import config.MicroserviceAppConfig
import controllers.Assets.NO_CONTENT
import helpers.ComponentSpecBase
import helpers.IntegrationTestConstants._
import helpers.servicemocks.{AuthStub, CreateIncomeSourceStub}
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.Json


class BusinessIncomeSourcesControllerISpec extends ComponentSpecBase {

  val appConfig: MicroserviceAppConfig = app.injector.instanceOf[MicroserviceAppConfig]
  val businessIncomeSourcesController: BusinessIncomeSourcesController = app.injector.instanceOf[BusinessIncomeSourcesController]


  "Income Source" should {
    "call Income sources connector successfully when auth succeeds for a no content submission" in {
      AuthStub.stubAuthSuccess()
      CreateIncomeSourceStub.stub(testMtdbsaRef, Json.toJson(testCreateIncomeSubmissionModel), appConfig.desAuthorisationToken, appConfig.desEnvironment)(
        OK, testCreateIncomeSuccessBody
      )

      val result = IncomeTaxSubscription.businessIncomeSource(testMtdbsaRef, testCreateIncomeSubmissionJson)

      result should have(
        httpStatus(NO_CONTENT)
      )
    }

    "Show error processing income source request with status Internal Server Error" in {
      AuthStub.stubAuthSuccess()
      CreateIncomeSourceStub.stub(testMtdbsaRef, Json.toJson(testCreateIncomeSubmissionModel), appConfig.desAuthorisationToken, appConfig.desEnvironment)(
        INTERNAL_SERVER_ERROR, testCreateIncomeFailureBody
      )

      val result = IncomeTaxSubscription.businessIncomeSource(testMtdbsaRef, testCreateIncomeSubmissionJson)

      result should have(
        httpStatus(INTERNAL_SERVER_ERROR)
      )

    }

  }
}
