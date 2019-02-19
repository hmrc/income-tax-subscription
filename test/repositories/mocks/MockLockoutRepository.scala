/*
 * Copyright 2019 HM Revenue & Customs
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

package repositories.mocks

import models.matching.LockoutResponse
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import repositories.LockoutMongoRepository
import utils.TestConstants.{testException, testLockoutResponse}

import scala.concurrent.Future

trait MockLockoutRepository extends MockitoSugar {
  val mockLockoutMongoRepository = mock[LockoutMongoRepository]

  private def mockLockoutAgent(arn: String)(response: Future[Option[LockoutResponse]]) =
    when(mockLockoutMongoRepository.lockoutAgent(ArgumentMatchers.eq(arn), ArgumentMatchers.any()))
      .thenReturn(response)

  def mockLockCreated(arn: String): Unit =
    mockLockoutAgent(arn)(Future.successful(Some(testLockoutResponse)))

  def mockLockCreationFailed(arn: String): Unit =
    mockLockoutAgent(arn)(Future.failed(testException))

  private def mockGetLockoutStatus(arn: String)(response: Future[Option[LockoutResponse]]) =
    when(mockLockoutMongoRepository.getLockoutStatus(arn))
      .thenReturn(response)

  def mockLockedOut(arn: String): Unit =
    mockGetLockoutStatus(arn)(Future.successful(Some(testLockoutResponse)))

  def mockNotLockedOut(arn: String): Unit =
    mockGetLockoutStatus(arn)(Future.successful(None))

  def mockLockedOutFailure(arn: String): Unit =
    mockGetLockoutStatus(arn)(Future.failed(testException))
}
