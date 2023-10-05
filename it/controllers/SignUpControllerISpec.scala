
package controllers

import config.MicroserviceAppConfig
import config.featureswitch.{FeatureSwitching, TaxYearSignup}
import helpers.ComponentSpecBase
import helpers.IntegrationTestConstants._
import helpers.servicemocks.{AuthStub, SignUpStub, SignUpTaxYearStub}
import models.SignUpResponse
import play.api.Configuration
import play.api.http.Status._
import play.api.libs.json.Json

class SignUpControllerISpec extends ComponentSpecBase with FeatureSwitching {

  val appConfig: MicroserviceAppConfig = app.injector.instanceOf[MicroserviceAppConfig]
  val signUpController: SignUpController = app.injector.instanceOf[SignUpController]
  val configuration: Configuration = app.injector.instanceOf[Configuration]

  override def beforeEach(): Unit = {
    super.beforeEach()
    disable(TaxYearSignup)
  }

  "signUp" should {
    "call sign up connector successfully when auth succeeds for a sign up submission 200" in {
      AuthStub.stubAuth(OK)
      SignUpStub.stubSignUp(testNino, testSignUpSubmission(testNino), appConfig.desAuthorisationToken, appConfig.desEnvironment)(
        OK, testSignUpSuccessBody
      )

      val res = IncomeTaxSubscription.signUp(testNino, testTaxYear)

      res should have(
        httpStatus(OK)
      )
      res should have(
        jsonBodyAs[SignUpResponse](SignUpResponse("XQIT00000000001"))
      )
    }

    "feature switch is enabled call sign up connector successfully when auth succeeds for a sign up submission 200" in {
      enable(TaxYearSignup)
      AuthStub.stubAuth(OK)
      SignUpTaxYearStub.stubSignUp(
        testTaxYearSignUpSubmission(testNino, testTaxYear),
        appConfig.signUpServiceAuthorisationToken,
        appConfig.signUpServiceEnvironment
      )(OK, testSignUpSuccessBody)

      val res = IncomeTaxSubscription.signUp(testNino, testTaxYear)

      res should have(
        httpStatus(OK)
      )
      res should have(
        jsonBodyAs[SignUpResponse](SignUpResponse("XQIT00000000001"))
      )
    }

    "return a Json parse failure when invalid json is found" in {
      AuthStub.stubAuthSuccess()
      SignUpStub.stubSignUp(testNino, testSignUpSubmission(testNino), appConfig.desAuthorisationToken, appConfig.desEnvironment)(
        OK, testSignUpInvalidBody
      )

      val res = IncomeTaxSubscription.signUp(testNino, testTaxYear)

      res should have(
        httpStatus(INTERNAL_SERVER_ERROR)
      )
    }

    "Show error processing Sign up request with status Internal Server Error" in {
      AuthStub.stubAuthSuccess()
      SignUpStub.stubSignUp(testNino, testSignUpSubmission(testNino), appConfig.desAuthorisationToken, appConfig.desEnvironment)(
        INTERNAL_SERVER_ERROR, failureResponse("code", "reason")
      )

      val res = IncomeTaxSubscription.signUp(testNino, testTaxYear)

      res should have(
        httpStatus(INTERNAL_SERVER_ERROR)
      )
    }
  }
}