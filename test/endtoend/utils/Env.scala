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

package utils

import com.typesafe.config.ConfigFactory
import org.openqa.selenium.firefox.{FirefoxDriver, FirefoxProfile}
import org.openqa.selenium.{HasCapabilities, WebDriver}
import play.api.Configuration

import scala.util.Try

object Env {


  val environment = System.getProperty("environment", "local")

  val oauthFrontendHost = "https://www-qa.tax.service.gov.uk"
  val apiGatewayHost = "https://qa-api.tax.service.gov.uk"

  private val config = Configuration(ConfigFactory.load())

  private def getConfigString(key: String): String = {
    val property: String = s"$environment.$key"
    config.getString(property).getOrElse(throw new RuntimeException(s"Config not found for: $property"))
  }

  def sleep(millis: Int = -1) = {
    if(millis == -1)
      Thread.sleep(getConfigString("sleep.default").toInt)
    else
      Thread.sleep(millis)
  }

  val incomeTaxSubscription = getConfigString("services.income-tax-subscription")

  lazy val driver: WebDriver with HasCapabilities = FirefoxDriver

  private lazy val FirefoxDriver = {
    val profile = new FirefoxProfile
    profile.setPreference("javascript.enabled", true)
    profile.setAcceptUntrustedCertificates(true)
    val driver = new FirefoxDriver(profile)
    driver.manage().window().maximize()
    driver
  }

  Runtime.getRuntime addShutdownHook new Thread {
    override def run {
      shutdown()
    }
  }

  def shutdown() = {
    Try(driver.quit())
  }

}
