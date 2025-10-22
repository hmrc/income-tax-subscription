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

import common.CommonSpec
import models.ErrorModel
import models.matching.LockoutResponse
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import repositories.mocks.MockLockoutRepository
import services.LockoutStatusService
import utils.TestConstants._

import scala.concurrent.{ExecutionContext, Future}

trait MockLockoutStatusService extends CommonSpec with MockitoSugar with BeforeAndAfterEach {

  val mockLockoutStatusService = mock[LockoutStatusService]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockLockoutStatusService)
  }

  private def mockLockoutAgent(arn: String)(result: Future[Either[ErrorModel, Option[LockoutResponse]]]): Unit =
    when(mockLockoutStatusService.lockoutAgent(ArgumentMatchers.eq(arn), ArgumentMatchers.any())).thenReturn(result)

  def mockLockCreated(arn: String): Unit =
    mockLockoutAgent(arn)(Future.successful(testLockoutSuccess))

  def mockLockCreationFailed(arn: String): Unit =
    mockLockoutAgent(arn)(Future.successful(testLockoutFailure))

  private def mockGetLockoutStatus(arn: String)(result: Future[Either[ErrorModel, Option[LockoutResponse]]]): Unit =
    when(mockLockoutStatusService.checkLockoutStatus(ArgumentMatchers.eq(arn))).thenReturn(result)

  def mockLockedOut(arn: String): Unit =
    mockGetLockoutStatus(arn)(Future.successful(testLockoutSuccess))

  def mockNotLockedOut(arn: String): Unit =
    mockGetLockoutStatus(arn)(Future.successful(testLockoutNone))

  def mockLockedOutFailure(arn: String): Unit =
    mockGetLockoutStatus(arn)(Future.successful(testLockoutFailure))

}

trait TestLockoutStatusService extends MockLockoutRepository {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
  object TestLockoutStatusService extends LockoutStatusService(mockLockoutMongoRepository)

}

