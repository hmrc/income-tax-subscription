/*
 * Copyright 2021 HM Revenue & Customs
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
import play.api.libs.json.{Format, JsObject, JsValue, Json}
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.Cursor.FailOnError
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import reactivemongo.play.json.ImplicitBSONHandlers._
import uk.gov.hmrc.mongo.ReactiveRepository

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubscriptionDataRepository @Inject()(mongo: ReactiveMongoComponent,
                                           appConfig: MicroserviceAppConfig)(implicit ec: ExecutionContext)
  extends ReactiveRepository[JsObject, BSONObjectID](
    "selfEmploymentsData",
    mongo.mongoConnector.db,
    implicitly[Format[JsObject]],
    implicitly[Format[BSONObjectID]]
  ) {

  private def find(selector: JsObject, projection: Option[JsObject] = None): Future[List[JsValue]] = {
    collection.find(selector, projection).cursor[JsObject]().collect(maxDocs = -1, FailOnError[List[JsObject]]())
  }

  def getSessionIdData(sessionId: String): Future[Option[JsValue]] = {
    val selector = Json.obj("sessionId" -> sessionId)
    val projection = Json.obj(_Id -> 0)
    find(selector, Some(projection)) map (_.headOption)
  }

  def getDataFromSession(sessionId: String, dataId: String): Future[Option[JsValue]] = {
    getSessionIdData(sessionId) map { optData =>
      optData.flatMap { json =>
        (json \ dataId).asOpt[JsValue]
      }
    }
  }

  def insertDataWithSession(sessionId: String, dataId: String, data: JsValue): Future[Option[JsValue]] = {
    val selector: JsObject = Json.obj("sessionId" -> sessionId)
    val set: JsValue = selector ++ Json.obj(dataId -> data)
    val update: JsObject = Json.obj("$set" -> set)
    findAndUpdate(selector, update, fetchNewObject = true, upsert = true).map(_.result[JsValue])
  }

  def deleteDataFromSessionId(sessionId: String): Future[WriteResult] = {
    val selector = Json.obj("sessionId" -> sessionId)
    remove("sessionId" -> Json.toJson(sessionId))
  }

  val creationTimestampKey = "creationTimestamp"

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
    name = Some("selfEmploymentsDataExpires"),
    unique = false,
    background = false,
    dropDups = false,
    sparse = false,
    version = None,
    options = BSONDocument("expireAfterSeconds" -> appConfig.timeToLiveSeconds)
  )

  collection.indexesManager.ensure(ttlIndex)
  collection.indexesManager.ensure(sessionIdIndex)
}
