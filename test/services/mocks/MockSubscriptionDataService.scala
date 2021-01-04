/*
 * Copyright 2021 HM Revenue & Customs
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

package services.mocks

import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, Suite}
import play.api.libs.json.JsObject
import reactivemongo.api.commands.WriteResult
import services.SubscriptionDataService

import scala.concurrent.Future

trait MockSubscriptionDataService extends BeforeAndAfterEach with MockitoSugar {
  self: Suite =>

  val mockSubscriptionDataService = mock[SubscriptionDataService]

  val mockDataId: String = "DataId"

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSubscriptionDataService)
  }

  def mockGetAllSelfEmployments(result: Option[JsObject]): Unit = {
    when(mockSubscriptionDataService.getAllSelfEmployments(ArgumentMatchers.any()))
      .thenReturn(Future.successful(result))
  }

  def mockRetrieveSelfEmployments(result: Option[JsObject]): Unit = {
    when(mockSubscriptionDataService.retrieveSelfEmployments(ArgumentMatchers.eq(mockDataId))(ArgumentMatchers.any()))
      .thenReturn(Future.successful(result))
  }

  def mockInsertSelfEmployments(data: JsObject)(result: Option[JsObject]): Unit = {
    when(mockSubscriptionDataService.insertSelfEmployments(ArgumentMatchers.eq(mockDataId), ArgumentMatchers.eq(data))(ArgumentMatchers.any()))
      .thenReturn(Future.successful(result))
  }

  def mockDeleteSessionData(result: WriteResult): Unit = {
    when(mockSubscriptionDataService.deleteSessionData(ArgumentMatchers.any()))
      .thenReturn(Future.successful(result))
  }

}
