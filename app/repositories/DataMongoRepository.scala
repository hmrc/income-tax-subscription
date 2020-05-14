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

package repositories

import config.MicroserviceAppConfig
import javax.inject.{Inject, Singleton}
import models.DataModel
import play.api.libs.json.Format
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import reactivemongo.play.json.ImplicitBSONHandlers._
import uk.gov.hmrc.mongo.ReactiveRepository

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DataMongoRepository @Inject()(mongo: ReactiveMongoComponent,
                                    appConfig: MicroserviceAppConfig)(implicit ec: ExecutionContext)
  extends ReactiveRepository[DataModel, BSONObjectID](
    "ItsData",
    mongo.mongoConnector.db,
    implicitly[Format[DataModel]],
    implicitly[Format[BSONObjectID]]
  ) {

  def findAllBySessionId(sessionId: String): Future[List[DataModel]] = {
    find("sessionId" -> sessionId)
  }

  def findByDataId(dataId: String): Future[Option[DataModel]] = {
    find("dataId" -> dataId) map (_.headOption)
  }

  val creationTimestampKey = "creationTimestamp"

  private val dataIdIndex =
    Index(Seq(("dataId", IndexType.Ascending)),
      name = Some("dataIdIndex"),
      unique = true,
      background = true,
      dropDups = false,
      sparse = false,
      version = None,
      options = BSONDocument())

  private val sessionIdIndex =
    Index(Seq(("sessionId", IndexType.Ascending)),
      name = Some("sessionIdIndex"),
      unique = false,
      background = true,
      dropDups = false,
      sparse = false,
      version = None,
      options = BSONDocument())

  private lazy val ttlIndex = Index(
    Seq((creationTimestampKey, IndexType.Ascending)),
    name = Some("itsDataExpires"),
    unique = false,
    background = false,
    dropDups = false,
    sparse = false,
    version = None,
    options = BSONDocument("expireAfterSeconds" -> appConfig.timeToLiveSeconds)
  )

  collection.indexesManager.ensure(ttlIndex)
  collection.indexesManager.ensure(dataIdIndex)
  collection.indexesManager.ensure(sessionIdIndex)
}
