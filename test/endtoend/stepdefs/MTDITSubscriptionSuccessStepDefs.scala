/*
 * Copyright 2016 HM Revenue & Customs
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

package stepdefs

import cucumber.api.scala.{EN, ScalaDsl}
import models.SubscriptionRequestModel
import org.scalatest.OptionValues
import pages._
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers._
import play.mvc.Http.Status
import utils.{BaseSpec, Env}

import scalaj.http.{Http, HttpOptions, HttpResponse}

class MTDITSubscriptionSuccessStepDefs extends ScalaDsl with EN with OptionValues with BaseSpec {

  val validHeader: String = "application/vnd.hmrc.1.0+json"
  val validJsonRequest: String = Json.toJson(SubscriptionRequestModel(
    email = "test@test.com",
    acceptsTermsAndConditions = true
  )).toString

  object world {
    var declaration: Boolean = _
    var email: String = _
    var accessToken: Option[String] = None
    var requestBody: JsValue = _
    var response: HttpResponse[String] = _
  }


  Given("^The user has a valid access token$") {
    world.accessToken match {
      case None =>
        val oAuthPage = new OAuthPage("SANDBOX")
        goTo(oAuthPage)

        on(SandboxLoginPage)
        SandboxLoginPage.signIn("user1", "password1")

        on(GrantPage)
        GrantPage.authorise()

        val oauthCode = oAuthPage.urlParameter(currentUrl, "code")
        world.accessToken = oAuthPage.accessToken(oauthCode)
        webDriver.close()
      case _ =>
    }
  }

  Given("^Agreement to Terms & Conditions is '(.*)'$") { (agreement: Boolean) ⇒ world.declaration = agreement}

  And("^A valid email address has been submitted as '(.*)'$") { (emailAddress: String) ⇒ world.email = emailAddress}

  When("^I access the MTD IT Subscription API endpoint$") {
    world.response = Http(s"${Env.apiGatewayHost}/income-tax/subscription")
      .header(ACCEPT, validHeader)
      .header(AUTHORIZATION, s"Bearer ${world.accessToken.get}")
      .header(CONTENT_TYPE, "application/json")
      .option(HttpOptions.allowUnsafeSSL)
      .postData(Json.toJson(SubscriptionRequestModel(world.email, world.declaration)).toString())
      .asString
  }

  Then("^The response code is status 201$") {
    world.response.code shouldBe Status.CREATED
  }
}
