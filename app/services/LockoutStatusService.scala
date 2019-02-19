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

package services

import javax.inject.{Inject, Singleton}

import models.ErrorModel
import models.lockout.LockoutRequest
import models.matching.LockoutResponse
import repositories.LockoutMongoRepository

import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.http.HeaderCarrier

@Singleton
class LockoutStatusService @Inject()(lockoutRepository: LockoutMongoRepository) {

  def lockoutAgent(arn: String, request: LockoutRequest)(implicit hc: HeaderCarrier, ec: ExecutionContext)
  : Future[Either[ErrorModel, Option[LockoutResponse]]] = {
    lockoutRepository.lockoutAgent(arn, request.timeoutSeconds).map(response => Right(response))
  }

  def checkLockoutStatus(arn: String)(implicit hc: HeaderCarrier, ec: ExecutionContext)
  : Future[Either[ErrorModel, Option[LockoutResponse]]] = {
    lockoutRepository.getLockoutStatus(arn).map(response => Right(response))
  }

}
