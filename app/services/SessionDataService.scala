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

package services

import config.AppConfig
import config.featureswitch.FeatureSwitching
import play.api.libs.json.JsValue
import repositories.SessionDataRepository
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class SessionDataService @Inject()(sessionDataRepository: SessionDataRepository, val appConfig: AppConfig) extends FeatureSwitching {


  def getAllSessionData(implicit hc: HeaderCarrier): Future[Option[JsValue]] =
    sessionDataRepository.getSessionData(sessionId = sessionIdFromHC)

  def getSessionData(dataId: String)(implicit hc: HeaderCarrier): Future[Option[JsValue]] =
    sessionDataRepository.getDataFromSession(sessionId = sessionIdFromHC, dataId = dataId)

  def insertSessionData(dataId: String, data: JsValue)(implicit hc: HeaderCarrier): Future[Option[JsValue]] =
    sessionDataRepository.insertDataWithSession(
      sessionId = sessionIdFromHC,
      dataId = dataId,
      data = data
    )

  def deleteSessionData(dataId: String)(implicit hc: HeaderCarrier): Future[Option[JsValue]] =
    sessionDataRepository.deleteDataWithSession(
      sessionId = sessionIdFromHC,
      dataId = dataId
    )

  private[services] def sessionIdFromHC(implicit hc: HeaderCarrier): String = {
    hc.sessionId.fold(
      throw new InternalServerException("[SessionDataService][sessionIdFromHC] - No session id in header carrier")
    )(_.value)
  }
}


