/*
 * Copyright 2022 HM Revenue & Customs
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
import config.featureswitch.{FeatureSwitching, SaveAndRetrieve}
import play.api.libs.json.JsValue
import reactivemongo.api.commands.WriteResult
import repositories.SubscriptionDataRepository
import services.SubscriptionDataService.{Created, Existence, Existing}
import uk.gov.hmrc.http.{HeaderCarrier, InternalServerException}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubscriptionDataService @Inject()(subscriptionDataRepository: SubscriptionDataRepository, val appConfig: AppConfig)
                                       (implicit ec: ExecutionContext) extends FeatureSwitching {


  private[services] def sessionIdFromHC(implicit hc: HeaderCarrier): String = {
    hc.sessionId.fold(
      throw new InternalServerException("[SubscriptionDataService][retrieveSelfEmployments] - No session id in header carrier")
    )(_.value)
  }

  def retrieveReference(utr: String, credId: String)(implicit hc: HeaderCarrier): Future[Existence] = {
    subscriptionDataRepository.retrieveReference(utr, credId) flatMap {
      case Some(value) => Future.successful(Existing(value))
      case None => subscriptionDataRepository.createReference(utr, credId, sessionIdFromHC) map(reference => Created(reference))
    }
  }

  def retrieveSubscriptionData(reference: String, dataId: String)(implicit hc: HeaderCarrier): Future[Option[JsValue]] = {
    if (isEnabled(SaveAndRetrieve)) {
      subscriptionDataRepository.getDataFromReference(
        reference = reference,
        dataId = dataId
      )
    } else {
      subscriptionDataRepository.getDataFromSession(
        reference = reference,
        sessionId = sessionIdFromHC,
        dataId = dataId
      )
    }
  }

  def getAllSubscriptionData(reference: String)(implicit hc: HeaderCarrier): Future[Option[JsValue]] = {
    if (isEnabled(SaveAndRetrieve)) {
      subscriptionDataRepository.getReferenceData(reference = reference)
    } else {
      subscriptionDataRepository.getSessionIdData(reference = reference, sessionId = sessionIdFromHC)
    }
  }

  def insertSubscriptionData(reference: String, dataId: String, data: JsValue)(implicit hc: HeaderCarrier): Future[Option[JsValue]] = {
    if (isEnabled(SaveAndRetrieve)) {
      subscriptionDataRepository.insertDataWithReference(
        reference = reference,
        dataId = dataId,
        data = data
      )
    } else {
      subscriptionDataRepository.insertDataWithSession(
        reference = reference,
        sessionId = sessionIdFromHC,
        dataId = dataId,
        data = data
      )
    }
  }

  def deleteSubscriptionData(reference: String, dataId: String)(implicit hc: HeaderCarrier): Future[Option[JsValue]] = {
    if (isEnabled(SaveAndRetrieve)) {
      subscriptionDataRepository.deleteDataWithReference(
        reference = reference,
        dataId = dataId
      )
    } else {
      subscriptionDataRepository.deleteDataWithReferenceAndSessionId(
        reference = reference,
        sessionId = sessionIdFromHC,
        dataId = dataId
      )
    }
  }

  def deleteAllSubscriptionData(reference: String)(implicit hc: HeaderCarrier): Future[WriteResult] = {
    if (isEnabled(SaveAndRetrieve)) {
      subscriptionDataRepository.deleteDataFromReference(reference)
    } else {
      subscriptionDataRepository.deleteDataFromSessionId(reference, sessionIdFromHC)
    }
  }

}

object SubscriptionDataService {
  sealed trait Existence

  case class Existing(reference:String) extends Existence
  case class Created(reference:String) extends Existence
}
