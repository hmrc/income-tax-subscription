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

import org.openqa.selenium.support.ui.{ExpectedCondition, WebDriverWait}
import org.openqa.selenium.{By, WebDriver, WebElement}
import org.scalatest._
import org.scalatest.selenium.{Page, WebBrowser}


case class Link(href: String, text: String)

trait WebPage extends Page with WebBrowser with ShouldMatchers {

  implicit val webDriver: WebDriver = Env.driver

  def isCurrentPage: Boolean

  def heading = tagName("h1").element.text

  def bodyText = tagName("body").element.text

  def pageType = id("pageType").element.text

  def at() = {
    loadPage()
    isCurrentPage shouldBe true
  }

  override def toString = this.getClass.getSimpleName

  private def loadPage() = {
    val wait = new WebDriverWait(webDriver, 10)
    wait.until(
      new ExpectedCondition[WebElement] {
        override def apply(d: WebDriver) = d.findElement(By.tagName("body"))
      }
    )
  }

  def elementToLink(element: Option[WebElement]): Option[Link] =
    element match {
      case Some(e) => Some(Link(e.getAttribute("href"), e.getText))
      case None => None
    }


  def findLink(id: String): Option[Link] = elementToLink(Some(webDriver.findElement(By.id(id))))

}
