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

import config.MicroserviceAppConfig
import config.featureswitch.{FeatureSwitching, SaveAndRetrieve}
import play.api.libs.json.{Format, JsObject, JsValue, Json}
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.Cursor.FailOnError
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import reactivemongo.play.json.ImplicitBSONHandlers._
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.mongo.ReactiveRepository

import java.time.Instant
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubscriptionDataRepository @Inject()(mongo: ReactiveMongoComponent,
                                           appConfig: MicroserviceAppConfig)(implicit ec: ExecutionContext)
  extends ReactiveRepository[JsObject, BSONObjectID](
    "selfEmploymentsData",
    mongo.mongoConnector.db,
    implicitly[Format[JsObject]],
    implicitly[Format[BSONObjectID]]
  ) with FeatureSwitching {

  private def find(selector: JsObject, projection: Option[JsObject]): Future[List[JsValue]] = {
    collection.find(selector, projection).cursor[JsObject]().collect(maxDocs = -1, FailOnError[List[JsObject]]())
  }

  def getSessionIdData(reference: String, sessionId: String): Future[Option[JsValue]] = {
    val selector = Json.obj("reference" -> reference, "sessionId" -> sessionId)
    val projection = Json.obj(_Id -> 0)
    find(selector, Some(projection)) map (_.headOption)
  }

  def getReferenceData(reference: String): Future[Option[JsValue]] = {
    val selector = Json.obj("reference" -> reference)
    val projection = Json.obj(_Id -> 0)
    find(selector, Some(projection)) map (_.headOption)
  }

  def getDataFromSession(reference: String, sessionId: String, dataId: String): Future[Option[JsValue]] = {
    getSessionIdData(reference, sessionId) map { optData =>
      optData.flatMap { json =>
        (json \ dataId).asOpt[JsValue]
      }
    }
  }

  def getDataFromReference(reference: String, dataId: String): Future[Option[JsValue]] = {
    getReferenceData(reference) map { optData =>
      optData.flatMap { json =>
        (json \ dataId).asOpt[JsValue]
      }
    }
  }

  def insertDataWithSession(reference: String, sessionId: String, dataId: String, data: JsValue): Future[Option[JsValue]] = {
    val selector: JsObject = Json.obj("reference" -> reference, "sessionId" -> sessionId)
    val set: JsValue = selector ++ Json.obj(dataId -> data) ++ Json.obj(
      "lastUpdatedTimestamp" -> Json.obj(
        "$date" -> Instant.now.toEpochMilli
      ).as[JsValue]
    )
    val update: JsObject = Json.obj(f"$$set" -> set)
    findAndUpdate(selector, update, fetchNewObject = true, upsert = true).map(_.result[JsValue])
  }

  def insertDataWithReference(reference: String, dataId: String, data: JsValue): Future[Option[JsValue]] = {
    val selector: JsObject = Json.obj("reference" -> reference)
    val set: JsValue = selector ++ Json.obj(dataId -> data) ++ Json.obj(
      "lastUpdatedTimestamp" -> Json.obj(
        "$date" -> Instant.now.toEpochMilli
      ).as[JsValue]
    )
    val update: JsObject = Json.obj(f"$$set" -> set)
    findAndUpdate(selector, update, fetchNewObject = true, upsert = true) map (_.result[JsValue])
  }

  def deleteDataWithReference(reference: String, dataId: String): Future[Option[JsValue]] = {
    val selector: JsObject = Json.obj("reference" -> reference)
    val unset: JsValue = Json.obj(dataId -> "")
    val update: JsObject = Json.obj(f"$$unset" -> unset)
    findAndUpdate(selector, update) map (_.result[JsValue])
  }

  def deleteDataWithReferenceAndSessionId(reference: String, sessionId: String, dataId: String): Future[Option[JsValue]] = {
    val selector: JsObject = Json.obj("reference" -> reference, "sessionId" -> sessionId)
    val unset: JsValue = Json.obj(dataId -> "")
    val update: JsObject = Json.obj(f"$$unset" -> unset)
    findAndUpdate(selector, update) map (_.result[JsValue])
  }

  def deleteDataFromSessionId(reference: String, sessionId: String): Future[WriteResult] = {
    remove("reference" -> reference, "sessionId" -> Json.toJson(sessionId))
  }

  def deleteDataFromReference(reference: String): Future[WriteResult] = {
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
      if (result.ok) reference
      else throw new InternalServerException("[SubscriptionDataRepository][createReference] - Unable to create document reference")
    }
  }

  val lastUpdatedTimestampKey = "lastUpdatedTimestamp"

  val sessionIdIndex: Index =
    Index(Seq(("sessionId", IndexType.Ascending)),
      name = Some("sessionIdIndex"),
      unique = false,
      dropDups = false,
      sparse = true,
      version = None,
      options = BSONDocument())

  private val ttlLength = if (isEnabled(SaveAndRetrieve)) {
    appConfig.timeToLiveSecondsSaveAndRetrieve
  } else {
    appConfig.timeToLiveSeconds
  }

  val utrCredIndex: Index =
    Index(
      key = Seq(
        "utr" -> IndexType.Ascending,
        "credId" -> IndexType.Ascending
      ),
      name = Some("utrCredIndex"),
      unique = true,
      dropDups = false,
      sparse = true,
      version = None,
      options = BSONDocument()
    )

  val referenceIndex: Index =
    Index(
      key = Seq(
        "reference" -> IndexType.Ascending
      ),
      name = Some("referenceIndex"),
      unique = true,
      dropDups = false,
      sparse = true,
      version = None,
      options = BSONDocument()
    )

  lazy val ttlIndex: Index = Index(
    Seq((lastUpdatedTimestampKey, IndexType.Ascending)),
    name = Some("selfEmploymentsDataExpires"),
    unique = false,
    dropDups = false,
    sparse = false,
    version = None,
    options = BSONDocument("expireAfterSeconds" -> ttlLength)
  )

  collection.indexesManager.ensure(sessionIdIndex)

  collection.indexesManager.drop("selfEmploymentsDataExpires")
  collection.indexesManager.ensure(ttlIndex)

  collection.indexesManager.drop("utrCredIndex")
  collection.indexesManager.ensure(utrCredIndex)

  collection.indexesManager.drop("referenceIndex")

  if(isEnabled(SaveAndRetrieve)) {
    collection.indexesManager.ensure(referenceIndex)
  }

}


