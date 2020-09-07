
package controllers

import config.MicroserviceAppConfig
import helpers.ComponentSpecBase
import helpers.IntegrationTestConstants._
import helpers.servicemocks.{AuthStub, SignUpStub}
import play.api.http.Status._

class SignUpControllerISpec extends ComponentSpecBase {

  val appConfig: MicroserviceAppConfig = app.injector.instanceOf[MicroserviceAppConfig]
  val signUpController: SignUpController = app.injector.instanceOf[SignUpController]


  "signUp" should {
    "call sign up connector successfully when auth succeeds for a sign up submission 200" in {
      AuthStub.stubAuthSuccess()
      SignUpStub.stubSignUp(testNino, testSignUpSubmission(testNino), appConfig.desAuthorisationToken, appConfig.desEnvironment)(
        OK, testSignUpSuccessBody
      )

      val res = IncomeTaxSubscription.signUp(testNino)

      res should have(
        httpStatus(OK)
      )
    }

    "return a Json parse failure when invalid json is found" in {
      AuthStub.stubAuthSuccess()
      SignUpStub.stubSignUp(testNino, testSignUpSubmission(testNino), appConfig.desAuthorisationToken, appConfig.desEnvironment)(
        OK, testSignUpInvalidBody
      )

      val res = IncomeTaxSubscription.signUp(testNino)

      res should have(
        httpStatus(INTERNAL_SERVER_ERROR)
      )
    }

    "Show error processing Sign up request with status Internal Server Error" in {
      AuthStub.stubAuthSuccess()
      SignUpStub.stubSignUp(testNino, testSignUpSubmission(testNino), appConfig.desAuthorisationToken, appConfig.desEnvironment)(
        INTERNAL_SERVER_ERROR, failureResponse("code", "reason")
      )

      val res = IncomeTaxSubscription.signUp(testNino)

      res should have(
        httpStatus(INTERNAL_SERVER_ERROR)
      )
    }
  }
}