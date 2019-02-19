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

package repositories.digitalcontact

import javax.inject.{Inject, Singleton}

import config.AppConfig
import models.digitalcontact.PaperlessPreferenceKey
import models.lockout.CheckLockout
import play.api.libs.json.OFormat
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.indexes.IndexType.Ascending
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson._
import repositories.converters.PaperlessPreferenceKeyWrites._
import repositories.converters.{PaperlessPreferenceKeyReads, PaperlessPreferenceKeyWrites}
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PaperlessPreferenceMongoRepository @Inject()(config: AppConfig)(implicit mongo: ReactiveMongoComponent, ec: ExecutionContext)
  extends ReactiveRepository[PaperlessPreferenceKey, BSONObjectID](
    "paperlessPreference",
    mongo.mongoConnector.db,
    OFormat(PaperlessPreferenceKeyReads, PaperlessPreferenceKeyWrites),
    ReactiveMongoFormats.objectIdFormats
  ) {

  def storeNino(key: PaperlessPreferenceKey): Future[PaperlessPreferenceKey] = {
    insert(key) map (_ => key)
  }

  def retrieveNino(token: String): Future[Option[PaperlessPreferenceKey]] = {
    find(tokenKey -> token) map (_.headOption)
  }

  private lazy val ttlIndex = Index(
    Seq((timestampKey, IndexType(Ascending.value))),
    name = Some("tokenExpires"),
    unique = false,
    background = false,
    dropDups = false,
    sparse = false,
    version = None,
    options = BSONDocument("expireAfterSeconds" -> config.paperlessPreferencesExpirySeconds)
  )

  private def setIndex(): Unit = {
    collection.indexesManager.drop(ttlIndex.name.get) onComplete {
      _ => collection.indexesManager.ensure(ttlIndex)
    }
  }

  setIndex()
}
