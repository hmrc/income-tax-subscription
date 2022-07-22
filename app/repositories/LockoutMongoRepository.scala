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

import com.mongodb.client.model.IndexOptions
import config.AppConfig
import models.lockout.CheckLockout
import models.matching.LockoutResponse
import org.bson.Document
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.IndexModel
import org.mongodb.scala.result.InsertOneResult
import org.mongodb.scala.{Observable, SingleObservable}
import play.api.libs.json.{Format, JsObject, JsValue, Json}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import java.time._
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions

@Singleton
class LockoutMongoRepositoryConfig @Inject()(val appConfig: AppConfig) {

  def mongoComponent: MongoComponent = MongoComponent(appConfig.mongoUri)

  import repositories.LockoutMongoRepository.lockIndex

  def indexes: Seq[IndexModel] = Seq(lockIndex)

}

@Singleton
class LockoutMongoRepository @Inject()(val config: LockoutMongoRepositoryConfig)(implicit ec: ExecutionContext)
  extends PlayMongoRepository[JsObject](
    collectionName = "selfEmploymentsData",
    mongoComponent = config.mongoComponent,
    domainFormat = implicitly[Format[JsObject]],
    indexes = config.indexes,
    replaceIndexes = true
  ) {

  def insert(document: JsObject): Future[InsertOneResult] = collection.insertOne(document).toFuture

  def lockoutAgent(arn: String, timeoutSeconds: Int): Future[Option[LockoutResponse]] = {
    val ttl: Duration = Duration.ofSeconds(timeoutSeconds)

    // mongo uses millis, so we need to get an instant with millis
    val instant = Instant.ofEpochMilli(Instant.now.plusSeconds(ttl.getSeconds).toEpochMilli)
    val expiryTimestamp = OffsetDateTime.ofInstant(instant, ZoneId.systemDefault())

    val model = LockoutResponse(arn, expiryTimestamp)
    insert(Json.toJson(model).as[JsObject]).map {
      case result if result.wasAcknowledged() => Some(model)
      case _ => None
    }
  }

  // If collection.drop() is executed without dropIndexes() first, a race condition appears to occur.
  def drop(implicit ec: ExecutionContext): Future[Any] = Future.sequence(Seq(collection.dropIndexes().toFuture(), collection.drop().toFuture()))

  def recreate: Future[Any] = collection.createIndexes(config.indexes).toFuture()

  implicit def toBson(doc: JsObject): Bson = Document.parse(doc.toString())

  def find(selector: JsObject, projection: Option[JsObject]): Future[List[JsValue]] = {
    collection
      .find(selector)
      .projection(projection.map(toBson).getOrElse(Json.obj()))
      .toFuture()
      .map(_.toList)
  }

  def getLockoutStatus(arn: String): Future[Option[LockoutResponse]] = {
    val selector: JsObject = Json.obj(LockoutResponse.arn -> arn)
    val projection: Option[JsObject] = None
    find(selector, projection).map(l => l.headOption.map(lr => lr.as[LockoutResponse]))
  }

  def dropDb: Future[Any] = Future.sequence(Seq(
    collection.drop().toFuture(),
    collection.createIndexes(config.indexes).toFuture()
  ))
}

object LockoutMongoRepository {

  val lockIndex: IndexModel = IndexModel(
    Json.obj(CheckLockout.expiry -> IndexType.ascending),
    new IndexOptions()
      .name("lockExpires")
      .unique(false)
      .sparse(false)
      .expireAfter(0, TimeUnit.SECONDS)
  )

  object IndexType {
    def ascending: Int = 1

    def descending: Int = -1
  }

  implicit def asOption(o: JsObject): Option[JsValue] = o.result.toOption.flatMap(Option(_))

  implicit def toBson(doc: JsObject): Bson = Document.parse(doc.toString())

  implicit def toFuture[T](observable: SingleObservable[T]): Future[T] = observable.toFuture()

  implicit def toFuture[T](observable: Observable[T]): Future[Seq[T]] = observable.toFuture()


}
