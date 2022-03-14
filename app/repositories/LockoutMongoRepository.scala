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

import java.time._
import javax.inject.Inject
import models.lockout.CheckLockout
import models.matching.LockoutResponse
import play.api.libs.json.{JsObject, Json}
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson._
import reactivemongo.play.json.ImplicitBSONHandlers._
import uk.gov.hmrc.mongo.ReactiveRepository
import uk.gov.hmrc.mongo.json.ReactiveMongoFormats

import java.time.temporal.{ChronoField, TemporalField}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class LockoutMongoRepository @Inject()(implicit mongo: ReactiveMongoComponent)
  extends ReactiveRepository[LockoutResponse, BSONObjectID](
    "lockout",
    mongo.mongoConnector.db,
    LockoutResponse.format,
    ReactiveMongoFormats.objectIdFormats
  ) {

  def lockoutAgent(arn: String, timeoutSeconds: Int): Future[Option[LockoutResponse]] = {
    val ttl: Duration = Duration.ofSeconds(timeoutSeconds)

    // mongo uses millis, so we need to get an instant with millis
    val instant = Instant.ofEpochMilli(Instant.now.plusSeconds(ttl.getSeconds).toEpochMilli)
    val expiryTimestamp = OffsetDateTime.ofInstant(instant, ZoneId.systemDefault())

    val model = LockoutResponse(arn, expiryTimestamp)
    collection.insert(ordered = false).one(model).map {
      case result if result.ok => Some(model)
      case _ => None
    }
  }

  def getLockoutStatus(arn: String): Future[Option[LockoutResponse]] = {
    val selector: JsObject = Json.obj(LockoutResponse.arn -> arn)
    val projection: Option[JsObject] = None
    collection.find(selector, projection).one[LockoutResponse]
  }

  val lockIndex = Index(
    Seq((CheckLockout.expiry, IndexType.Ascending)),
    name = Some("lockExpires"),
    unique = false,
    background = false,
    dropDups = false,
    sparse = false,
    version = None,
    options = BSONDocument("expireAfterSeconds" -> 0)
  )

  private def setIndex() = collection.indexesManager.ensure(lockIndex)

  setIndex()

  def dropDb: Future[Unit] = {
    collection.drop(failIfNotFound = false)
    setIndex().map(_ => Unit)
  }


}
