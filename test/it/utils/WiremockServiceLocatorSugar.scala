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

package utils

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import domain.Registration
import play.api.libs.json.Json

trait WiremockServiceLocatorSugar {
  lazy val wireMockUrl = s"http://$stubHost:$stubPort"
  lazy val wireMockServer = new WireMockServer(wireMockConfig().port(stubPort))
  val stubPort = 9602
  val stubHost = "localhost"

  def regPayloadStringFor(serviceName: String, serviceUrl: String): String =
    Json.toJson(Registration(serviceName, serviceUrl, Some(Map("third-party-api" -> "true")))).toString

  def startMockServer() = {
    wireMockServer.start()
    WireMock.configureFor(stubHost, stubPort)
  }

  def stopMockServer() = {
    wireMockServer.stop()
    wireMockServer.resetMappings()
  }

  def stubRegisterEndpoint(status: Int) = stubFor(post(urlMatching("/registration")).willReturn(aResponse().withStatus(status)))
}
