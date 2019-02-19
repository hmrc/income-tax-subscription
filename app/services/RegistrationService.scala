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

import audit.{Logging, LoggingConfig}
import connectors.RegistrationConnector
import models.ErrorModel
import models.registration.{RegistrationRequestModel, RegistrationSuccessResponseModel}
import play.api.http.Status.CONFLICT
import utils.Implicits._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier

@Singleton
class RegistrationService @Inject()(registrationConnector: RegistrationConnector,
                                    logging: Logging) {

  def register(isAgent: Boolean, nino: String)(implicit hc: HeaderCarrier): Future[Either[ErrorModel, RegistrationSuccessResponseModel]] = {
    implicit val registerLoggingConfig: Option[LoggingConfig] = RegistrationService.registerLoggingConfig
    logging.debug(s"Request: NINO=$nino, isAgent=$isAgent")
    registrationConnector.register(nino, RegistrationRequestModel(isAgent)).flatMap {
      case Left(ErrorModel(CONFLICT, _, _)) => lookupRegister(nino)
      case r => r
    }
  }

  @inline private[services] def lookupRegister(nino: String)(implicit hc: HeaderCarrier) = {
    implicit val lookupRegisterConfig: Option[LoggingConfig] = RegistrationService.lookupRegisterLoggingConfig
    logging.debug(s"Request: NINO=$nino")
    registrationConnector.getRegistration(nino)
  }

}

object RegistrationService {
  val registerLoggingConfig: Option[LoggingConfig] = LoggingConfig(heading = "RegistrationService.register")
  val lookupRegisterLoggingConfig: Option[LoggingConfig] = LoggingConfig(heading = "RegistrationService.lookupRegister")
}
