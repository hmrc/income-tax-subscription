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

import com.mongodb.client.result.DeleteResult
import config.AppConfig
import config.featureswitch.FeatureSwitching
import play.api.libs.json.JsValue
import repositories.SubscriptionDataRepository
import services.SubscriptionDataService.{Created, Existence, Existing}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubscriptionDataService @Inject()(subscriptionDataRepository: SubscriptionDataRepository, val appConfig: AppConfig)
                                       (implicit ec: ExecutionContext) extends FeatureSwitching {

  def retrieveReference(utr: String, arn: Option[String]): Future[Existence] = {
    subscriptionDataRepository.retrieveReference(utr, arn) flatMap {
      case Some(value) => Future.successful(Existing(value))
      case None => subscriptionDataRepository.createReference(utr, arn) map (reference => Created(reference))
    }
  }

  def retrieveSubscriptionData(reference: String, dataId: String): Future[Option[JsValue]] =
    subscriptionDataRepository.getDataFromReference(
      reference = reference,
      dataId = dataId
    )

  def getAllSubscriptionData(reference: String): Future[Option[JsValue]] =
    subscriptionDataRepository.getReferenceData(reference = reference)

  def insertSubscriptionData(reference: String, dataId: String, data: JsValue): Future[Option[JsValue]] =
    subscriptionDataRepository.insertDataWithReference(
      reference = reference,
      dataId = dataId,
      data = data
    )

  def deleteSubscriptionData(reference: String, dataId: String): Future[Option[JsValue]] =
    subscriptionDataRepository.deleteDataWithReference(
      reference = reference,
      dataId = dataId
    )

  def deleteAllSubscriptionData(reference: String): Future[DeleteResult] =
    subscriptionDataRepository.deleteDataFromReference(reference)

}

object SubscriptionDataService {
  sealed trait Existence

  case class Existing(reference: String) extends Existence

  case class Created(reference: String) extends Existence
}
