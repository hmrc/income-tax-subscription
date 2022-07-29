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

import com.mongodb.client.model.{FindOneAndUpdateOptions, IndexOptions}
import com.mongodb.client.result.DeleteResult
import config.AppConfig
import org.bson.Document
import org.bson.conversions.Bson
import org.mongodb.scala.model.IndexModel
import org.mongodb.scala.result.InsertOneResult
import org.mongodb.scala.{Observable, SingleObservable}
import play.api.libs.json.Json.JsValueWrapper
import play.api.libs.json.{Format, JsObject, JsValue, Json}
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import java.time.Instant
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions

@Singleton
class SubscriptionDataRepositoryConfig @Inject()(val appConfig: AppConfig) {

  import SubscriptionDataRepository._

  private val ttlLengthSeconds = appConfig.timeToLiveSeconds

  def mongoComponent: MongoComponent = MongoComponent(appConfig.mongoUri)

  def indexes: Seq[IndexModel] =
    Seq(ttlIndex(ttlLengthSeconds), utrCredIndex, referenceIndex)

}

@Singleton
class SubscriptionDataRepository @Inject()(config: SubscriptionDataRepositoryConfig)(implicit ec: ExecutionContext)
  extends PlayMongoRepository[JsObject](
    collectionName = "selfEmploymentsData",
    mongoComponent = config.mongoComponent,
    domainFormat = implicitly[Format[JsObject]],
    indexes = config.indexes,
    replaceIndexes = false
  ) {

  private val findOneAndUpdateOptions: FindOneAndUpdateOptions = new FindOneAndUpdateOptions().upsert(true)

  import SubscriptionDataRepository._

  private val removeIdProjection = toBson(Json.obj(_Id -> 0))

  private def find(selector: JsObject, projection: Option[JsObject]): Future[Seq[JsValue]] = {
    collection
      .find(selector)
      .projection(projection.map(toBson).getOrElse(removeIdProjection))
  }

  def getReferenceData(reference: String): Future[Option[JsValue]] = {
    val selector = Json.obj("reference" -> reference)
    val projection = Json.obj(_Id -> 0)
    find(selector, Some(projection)) map (_.headOption)
  }

  def getDataFromReference(reference: String, dataId: String): Future[Option[JsValue]] = {
    getReferenceData(reference) map { optData =>
      optData.flatMap { json =>
        (json \ dataId).asOpt[JsValue]
      }
    }
  }

  def insertDataWithReference(reference: String, dataId: String, data: JsValue): Future[Option[JsValue]] = {
    val selector: JsObject = Json.obj("reference" -> reference)
    val set: JsValue = selector ++ Json.obj(dataId -> data) ++ Json.obj(
      "lastUpdatedTimestamp" -> Json.obj(
        "$date" -> Instant.now.toEpochMilli
      ).as[JsValue]
    )
    val update: JsObject = Json.obj(f"$$set" -> set)
    findAndUpdate(selector, update, fetchNewObject = true, upsert = true)
  }

  def deleteDataWithReference(reference: String, dataId: String): Future[Option[JsValue]] = {
    val selector: JsObject = Json.obj("reference" -> reference)
    val unset: JsValue = Json.obj(dataId -> "")
    val update: JsObject = Json.obj(f"$$unset" -> unset)
    findAndUpdate(selector, update)
  }

  def deleteDataFromReference(reference: String): Future[DeleteResult] = {
    remove("reference" -> Json.toJson(reference))
  }

  def retrieveReference(utr: String, credId: String): Future[Option[String]] = {
    val selector: JsObject = Json.obj("utr" -> utr, "credId" -> credId)
    val projection = Json.obj(_Id -> 0)
    find(selector, Some(projection)).map {
      _.headOption map { json =>
        (json \ "reference").asOpt[String] match {
          case Some(reference) =>
            reference
          case None =>
            throw new InternalServerException("[SubscriptionDataRepository][retrieveReference] - Document exists without reference, unable to retrieve")
        }
      }
    }
  }

  def createReference(utr: String, credId: String, sessionId: String): Future[String] = {
    val reference: String = UUID.randomUUID().toString
    val document: JsObject = Json.obj(
      "utr" -> utr,
      "credId" -> credId,
      "reference" -> reference,
      "sessionId" -> sessionId,
      "lastUpdatedTimestamp" -> Json.obj(
        "$date" -> Instant.now.toEpochMilli
      ).as[JsValue]
    )
    insert(document).map { result =>
      if (result.wasAcknowledged()) reference
      else throw new InternalServerException("[SubscriptionDataRepository][createReference] - Unable to create document reference")
    }
  }

  private def findAndUpdate(selector: JsObject, update: JsObject, fetchNewObject: Boolean = false, upsert: Boolean = false) =
    collection.findOneAndUpdate(selector, update, findOneAndUpdateOptions).toFuture().map(asOption)

  private def remove(tuples: (String, JsValueWrapper)*) =
    collection.deleteOne(Json.obj(tuples: _*))

  def insert(document: JsObject): Future[InsertOneResult] = collection.insertOne(document).toFuture

  def drop(): Future[Void] = collection.drop().toFuture

}


object SubscriptionDataRepository {

  object IndexType {
    def ascending: Int = 1

    def descending: Int = -1
  }

  implicit def asOption(o: JsObject): Option[JsValue] = o.result.toOption.flatMap(Option(_))

  implicit def toBson(doc: JsObject): Bson = Document.parse(doc.toString())

  implicit def toFuture[T](observable: SingleObservable[T]): Future[T] = observable.toFuture()

  implicit def toFuture[T](observable: Observable[T]): Future[Seq[T]] = observable.toFuture()

  val lastUpdatedTimestampKey = "lastUpdatedTimestamp"

  val utrCredIndex: IndexModel = new IndexModel(
    Json.obj("utr" -> IndexType.ascending, "credId" -> IndexType.ascending),
    new IndexOptions()
      .name("utrCredIndex")
      .unique(true)
      .sparse(true)
  )

  val referenceIndex: IndexModel = IndexModel(
    Json.obj("reference" -> IndexType.ascending),
    new IndexOptions()
      .name("referenceIndex")
      .unique(true)
      .sparse(true)
  )

  def referenceIndexMaybe(sandr: Boolean): Option[IndexModel] = if (sandr) Some(referenceIndex) else None

  def ttlIndex(ttlLengthSeconds: Long): IndexModel = new IndexModel(
    Json.obj(lastUpdatedTimestampKey -> IndexType.ascending),
    new IndexOptions()
      .name("selfEmploymentsDataExpires")
      .unique(false)
      .sparse(false)
      .expireAfter(ttlLengthSeconds, TimeUnit.SECONDS)
  )

  val _Id = "_id"

}
