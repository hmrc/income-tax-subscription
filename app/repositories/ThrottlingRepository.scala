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

package repositories

import com.mongodb.client.model.{FindOneAndUpdateOptions, IndexOptions, ReturnDocument}
import config.AppConfig
import org.bson.Document
import org.bson.conversions.Bson
import org.mongodb.scala.model.IndexModel
import org.mongodb.scala.result.InsertOneResult
import org.mongodb.scala.{Observable, SingleObservable}
import play.api.libs.json.{Format, JsObject, JsValue, Json}
import repositories.ThrottlingRepository._
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions

@Singleton
class InstantProvider @Inject()() {

  def getInstantNowMilli: Long = Instant.now.toEpochMilli

}

@Singleton
class ThrottlingRepositoryConfig @Inject()(val appConfig: AppConfig) {

  private val ttlLengthSeconds = appConfig.throttleTimeToLiveSeconds

  def mongoComponent: MongoComponent = MongoComponent(appConfig.mongoUri)

  def indexes: Seq[IndexModel] = Seq(
    ttlIndex(ttlLengthSeconds),
    idTimecodeIndex
  )
  
}

@Singleton
class ThrottlingRepository @Inject()(config: ThrottlingRepositoryConfig, instantProvider: InstantProvider)(implicit ec: ExecutionContext)
  extends PlayMongoRepository[JsObject](
    collectionName = "throttling",
    mongoComponent = config.mongoComponent,
    domainFormat = implicitly[Format[JsObject]],
    indexes = config.indexes,
    replaceIndexes = false
  ) {

  private def timecode(time: Long) = time / 60000

  private def query(id: String, time: Long): JsObject = {
    Json.obj(
      throttleIdKey -> id,
      timecodeKey -> timecode(time)
    )
  }

  private def update(id: String, time: Long): JsObject = {
    val timestampUpdated: JsValue = Json.obj(f"$$date" -> time)
    val set: JsValue = query(id, time) ++ Json.obj(
      lastUpdatedTimestampKey -> timestampUpdated
    )
    val inc: JsValue = Json.obj(
      countKey -> 1
    )
    Json.obj(f"$$set" -> set, f"$$inc" -> inc)
  }

  private def resultToThrottleCount: Option[JsValue] => Int = {
    case Some(json) => (json \ countKey).as[Int]
    case None => 0
  }

  def checkThrottle(id: String): Future[Int] = {
    val time: Long = instantProvider.getInstantNowMilli
    val eventualMaybeValue = findAndUpdate(query(id, time), update(id, time))
    eventualMaybeValue map resultToThrottleCount
  }

  def stateOfThrottle(id: String): Future[(Int, Long)] = {
    val time: Long = instantProvider.getInstantNowMilli
    val eventualMaybeValue = find(query(id, time), None).map(l => l.headOption)
    eventualMaybeValue map resultToThrottleCount map (c => (c, timecode(time)))
  }

  private def findAndUpdate(selector: JsObject, update: JsObject): Future[Option[JsValue]] =
    collection.findOneAndUpdate(selector, update, findOneAndUpdateOptions).toFuture().map(asOption)

  def insert(document: JsObject): Future[InsertOneResult] = collection.insertOne(document).toFuture

  def drop(): Future[Void] = collection.drop().toFuture

  private val findOneAndUpdateOptions: FindOneAndUpdateOptions = new FindOneAndUpdateOptions()
    .upsert(true)
    .returnDocument(ReturnDocument.AFTER)

  def find(selector: JsObject, projection: Option[JsObject]): Future[List[JsObject]] = {
    collection
      .find(selector)
      .projection(projection.map(toBson).getOrElse(removeIdProjection))
      .toFuture()
      .map(_.toList)
  }

  val _Id = "_id"
  private val removeIdProjection = toBson(Json.obj(_Id -> 0))
}


object ThrottlingRepository {

  object IndexType {
    def ascending: Int = 1

    def descending: Int = -1
  }

  implicit def asOption(o: JsObject): Option[JsValue] = o.result.toOption.flatMap(Option(_))

  implicit def toBson(doc: JsObject): Bson = Document.parse(doc.toString())

  implicit def toFuture[T](observable: SingleObservable[T]): Future[T] = observable.toFuture()

  implicit def toFuture[T](observable: Observable[T]): Future[Seq[T]] = observable.toFuture()

  val throttleIdKey = "throttleId"
  val timecodeKey = "timecode"
  val countKey = "count"
  val lastUpdatedTimestampKey = "lastUpdatedTimestamp"


  val idTimecodeIndex: IndexModel = IndexModel(
    Json.obj(
      throttleIdKey -> IndexType.ascending,
      timecodeKey -> IndexType.ascending
    ),
    new IndexOptions()
      .name("idTimecodeIndex")
      .unique(true)
      .sparse(true)
  )

  def ttlIndex(ttlLengthSeconds: Long): IndexModel = new IndexModel(
    Json.obj(lastUpdatedTimestampKey -> IndexType.ascending),
    new IndexOptions()
      .name("throttleDataExpires")
      .unique(false)
      .sparse(false)
      .expireAfter(ttlLengthSeconds, TimeUnit.SECONDS)
  )

}
