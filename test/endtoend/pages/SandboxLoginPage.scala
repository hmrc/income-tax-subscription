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

import utils.{Env, WebPage}

object SandboxLoginPage extends WebPage {
  override def isCurrentPage: Boolean = find(className("button")).fold(false)(_.text == "Sign in")

  override val url: String = s"${Env.oauthFrontendHost}/oauth/sandbox-login?continue=http://localhost:9560/oauth/grantscope"

  def signInBtn = find(className("button")).get

  def userField = textField("userId")

  def passwordField = pwdField("password")

  def signIn(): Unit = {
    click on signInBtn
  }

  def signIn(username: String, password: String) = {
    userField.value = username
    passwordField.value = password
    click on signInBtn
  }
}
