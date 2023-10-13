/*
 * Copyright 2023 HM Revenue & Customs
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

import com.mongodb.client.result.DeleteResult
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.{BeforeAndAfterEach, Suite}
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.JsObject
import services.SubscriptionDataService

import scala.concurrent.Future

trait MockSubscriptionDataService extends BeforeAndAfterEach with MockitoSugar {
  self: Suite =>

  val mockSubscriptionDataService: SubscriptionDataService = mock[SubscriptionDataService]

  val mockDataId: String = "DataId"

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSubscriptionDataService)
  }

  def mockRetrieveReference(utr: String, arn: Option[String], existence: String => SubscriptionDataService.Existence)(result: String): Unit = {
    when(mockSubscriptionDataService.retrieveReference(ArgumentMatchers.eq(utr), ArgumentMatchers.eq(arn)))
      .thenReturn(Future.successful(existence(result)))
  }

  def mockGetAllSelfEmployments(reference: String)(result: Option[JsObject]): Unit = {
    when(mockSubscriptionDataService.getAllSubscriptionData(ArgumentMatchers.eq(reference)))
      .thenReturn(Future.successful(result))
  }

  def mockRetrieveSelfEmployments(reference: String)(result: Option[JsObject]): Unit = {
    when(mockSubscriptionDataService.retrieveSubscriptionData(ArgumentMatchers.eq(reference), ArgumentMatchers.eq(mockDataId)))
      .thenReturn(Future.successful(result))
  }

  def mockInsertSelfEmployments(reference: String, data: JsObject)(result: Option[JsObject]): Unit = {
    when(mockSubscriptionDataService.insertSubscriptionData(
      ArgumentMatchers.eq(reference), ArgumentMatchers.eq(mockDataId), ArgumentMatchers.eq(data)))
      .thenReturn(Future.successful(result))
  }

  def mockDeleteSubscriptionData(reference: String)(result: Option[JsObject]): Unit = {
    when(mockSubscriptionDataService.deleteSubscriptionData(ArgumentMatchers.eq(reference), ArgumentMatchers.eq(mockDataId)))
      .thenReturn(Future.successful(result))
  }

  def mockDeleteSessionData(reference: String)(result: DeleteResult): Unit = {
    when(mockSubscriptionDataService.deleteAllSubscriptionData(ArgumentMatchers.eq(reference)))
      .thenReturn(Future.successful(result))
  }

}
