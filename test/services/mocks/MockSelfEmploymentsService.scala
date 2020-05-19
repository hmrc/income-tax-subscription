/*
 * Copyright 2020 HM Revenue & Customs
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

import org.mockito.{ArgumentMatcher, ArgumentMatchers}
import org.mockito.Mockito._
import org.mockito.stubbing.OngoingStubbing
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, Suite}
import play.api.libs.json.JsObject
import services.{AuthService, SelfEmploymentsService}
import uk.gov.hmrc.auth.core.authorise.EmptyPredicate
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

trait MockSelfEmploymentsService extends BeforeAndAfterEach with MockitoSugar {
  self: Suite =>

  val mockSelfEmploymentsService = mock[SelfEmploymentsService]

  val mockDataId: String = "DataId"

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSelfEmploymentsService)
  }

  def mockGetAllSelfEmployments(result: Option[JsObject]): Unit = {
    when(mockSelfEmploymentsService.getAllSelfEmployments(ArgumentMatchers.any()))
      .thenReturn(Future.successful(result))
  }

  def mockRetrieveSelfEmployments(result: Option[JsObject]): Unit = {
    when(mockSelfEmploymentsService.retrieveSelfEmployments(ArgumentMatchers.eq(mockDataId))(ArgumentMatchers.any()))
      .thenReturn(Future.successful(result))
  }

  def mockInsertSelfEmployments(data: JsObject)(result: Option[JsObject]): Unit = {
    when(mockSelfEmploymentsService.insertSelfEmployments(ArgumentMatchers.eq(mockDataId), ArgumentMatchers.eq(data))(ArgumentMatchers.any()))
      .thenReturn(Future.successful(result))
  }

}
