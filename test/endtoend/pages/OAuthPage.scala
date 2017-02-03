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

package pages

import java.net.URL

import play.api.libs.json.Json
import utils.{Env, WebPage}

import scalaj.http.Http

class OAuthPage (authType: String) extends WebPage{

  override def isCurrentPage: Boolean = ???

  override val url: String = authoriseUrl()


  private def authoriseUrl(): String = {
    val clientId = authType match {
      case "PRODUCTION" => "bgFz03beasjfC95S9fUjvhbHdRca"
      case "SANDBOX" => "NtvvzzLCpvetD6Fv_xmJBCUGoWIa"
    }

    s"""${Env.oauthFrontendHost}/oauth/authorize""" +
      s"""?client_id=${clientId}""" +
      s"""&scope=write:income-tax""" +
      s"""&response_type=code""" +
      s"""&redirect_uri=http://localhost:22222/oauth""" +
      s"""&state=12345"""
  }

  def accessToken(oauthCode: Option[String]) = {
    val clientDetails = authType match {
      case "PRODUCTION" => ("bgFz03beasjfC95S9fUjvhbHdRca", "d82e866b-acce-4d7d-b1f2-fdb969f52b6b")
      case "SANDBOX" => ("NtvvzzLCpvetD6Fv_xmJBCUGoWIa", "5442ea09-4733-4e70-9003-b4bcb4410579")
    }

    val tokenResponse = Http(s"${Env.oauthFrontendHost}/oauth/token")
      .postForm(Seq(
        "client_id" -> s"${clientDetails._1}",
        "client_secret" -> s"${clientDetails._2}",
        "grant_type" -> "authorization_code",
        "code" -> oauthCode.get,
        "redirect_uri" -> "http://localhost:22222/oauth"))
      .asString
    withClue("Token request has failed with error:"){
      tokenResponse.code shouldBe 200
    }

    (Json.parse(tokenResponse.body) \ "access_token").asOpt[String]
  }

  def urlParameter(url: String, parameterName: String): Option[String] = {
    val parameters: Array[String] = new URL(url).getQuery.split("&")
    parameters find (_ startsWith s"$parameterName=") map {
      s => s.split("=")(1)
    }
  }
}
