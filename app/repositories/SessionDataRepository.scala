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

package repositories

import com.mongodb.client.model.{FindOneAndUpdateOptions, IndexOptions}
import com.mongodb.client.result.DeleteResult
import config.AppConfig
import org.bson.Document
import org.bson.conversions.Bson
import org.mongodb.scala.model.IndexModel
import org.mongodb.scala.result.InsertOneResult
import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json._
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import utils.JsonUtils.JsObjectUtil

import java.time.Instant
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions


@Singleton
class SessionDataRepositoryConfig @Inject()(val appConfig: AppConfig) {

  import SessionDataRepository._

  private val ttlLengthSeconds = appConfig.sessionTimeToLiveSeconds

  def mongoComponent: MongoComponent = MongoComponent(appConfig.mongoUri)

  def indexes: Seq[IndexModel] =
    Seq(ttlIndex(ttlLengthSeconds), sessionIdIndex)
}

@Singleton
class SessionDataRepository @Inject()(config: SessionDataRepositoryConfig)(implicit ec: ExecutionContext)
  extends PlayMongoRepository[JsObject](
    collectionName = "sessionData",
    mongoComponent = config.mongoComponent,
    domainFormat = implicitly[Format[JsObject]],
    indexes = config.indexes,
    replaceIndexes = false
  ) {

  def findOneAndUpdateOptions(upsert: Boolean): FindOneAndUpdateOptions = new FindOneAndUpdateOptions().upsert(upsert)

  import SessionDataRepository._

  private val removeIdProjection = toBson(Json.obj(_Id -> 0))

  def find(selector: JsObject, projection: Option[JsObject]): Future[Seq[JsValue]] = {
    collection
      .find(selector)
      .projection(projection.map(toBson).getOrElse(removeIdProjection))
      .toFuture()
  }

  def getSessionData(sessionId: String): Future[Option[JsValue]] = {
    val selector = Json.obj("session-id" -> sessionId)
    val projection = Json.obj(_Id -> 0)
    find(selector, Some(projection)) map (_.headOption)
  }

  def getDataFromSession(sessionId: String, dataId: String): Future[Option[JsValue]] = {
    getSessionData(sessionId) map { optData =>
      optData.flatMap { json =>
        (json \ dataId).asOpt[JsValue]
      }
    }
  }

  def insertDataWithSession(sessionId: String, dataId: String, data: JsValue): Future[Option[JsValue]] = {
    val selector: JsObject = Json.obj("session-id" -> sessionId)
    val set: JsValue = selector ++ Json.obj(dataId -> data) ++ Json.obj(
      "lastUpdatedTimestamp" -> Json.obj(
        "$date" -> Instant.now.toEpochMilli
      ).as[JsValue]
    )
    val update: JsObject = Json.obj(f"$$set" -> set)
    findAndUpdate(selector, update, fetchNewObject = true, upsert = true)
  }

  def deleteDataWithSession(sessionId: String, dataId: String): Future[Option[JsValue]] = {
    val selector: JsObject = Json.obj("session-id" -> sessionId)
    val unset: JsValue = Json.obj(dataId -> "")
    val update: JsObject = Json.obj(f"$$unset" -> unset)
    findAndUpdate(selector, update)
  }


  private def findAndUpdate(selector: JsObject, update: JsObject, fetchNewObject: Boolean = false, upsert: Boolean = false) = {
    collection
      .findOneAndUpdate(selector, update, findOneAndUpdateOptions(upsert))
      .toFuture()
      .map(asOption)
  }

  def insert(document: JsObject): Future[InsertOneResult] = {
    collection
      .insertOne(document)
      .toFuture()
  }

  def drop(): Future[Void] = {
    collection
      .drop()
      .toFuture()
  }

}


object SessionDataRepository {

  object IndexType {
    def ascending: Int = 1

    def descending: Int = -1
  }

  def asOption(o: JsObject): Option[JsValue] = o.result.toOption.flatMap(Option(_))

  implicit def toBson(doc: JsObject): Bson = Document.parse(doc.toString())

  val lastUpdatedTimestampKey = "lastUpdatedTimestamp"

  val sessionIdIndex: IndexModel = IndexModel(
    Json.obj("session-id" -> IndexType.ascending),
    new IndexOptions()
      .name("sessionIdIndex")
      .unique(true)
  )

  def ttlIndex(ttlLengthSeconds: Long): IndexModel = new IndexModel(
    Json.obj(lastUpdatedTimestampKey -> IndexType.ascending),
    new IndexOptions()
      .name("sessionDataExpires")
      .unique(false)
      .expireAfter(ttlLengthSeconds, TimeUnit.SECONDS)
  )

  val _Id = "_id"

}
