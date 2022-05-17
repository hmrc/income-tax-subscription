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
import config.featureswitch.FeatureSwitching
import play.api.libs.json.{Format, JsObject, JsValue, Json}
import play.modules.reactivemongo.ReactiveMongoComponent
import reactivemongo.api.commands.FindAndModifyCommand
import reactivemongo.api.indexes.{Index, IndexType}
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import reactivemongo.play.json.ImplicitBSONHandlers._
import uk.gov.hmrc.mongo.ReactiveRepository

import java.time.Instant
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class InstantProvider @Inject()() {

  def getInstantNowMilli: Long = Instant.now.toEpochMilli

}

@Singleton
class ThrottlingRepository @Inject()(mongo: ReactiveMongoComponent,
                                     val appConfig: MicroserviceAppConfig,
                                     instantProvider: InstantProvider)(implicit ec: ExecutionContext)

  extends ReactiveRepository[JsObject, BSONObjectID](
    "throttling",
    mongo.mongoConnector.db,
    implicitly[Format[JsObject]],
    implicitly[Format[BSONObjectID]]
  ) with FeatureSwitching {

  val throttleIdKey = "throttleId"
  val timecodeKey = "timecode"
  val countKey = "count"
  val lastUpdatedTimestampKey = "lastUpdatedTimestamp"

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

  private def resultToThrottleCount: FindAndModifyCommand.Result[collection.pack.type] => Int = {
    _.result[JsObject] match {
      case Some(json) => (json \ countKey).as[Int]
      case None => 0
    }
  }

  def checkThrottle(id: String): Future[Int] = {
    val time: Long = instantProvider.getInstantNowMilli

    findAndUpdate(
      query = query(id, time),
      update = update(id, time),
      fetchNewObject = true,
      upsert = true
    ) map resultToThrottleCount
  }


  val idTimecodeIndex: Index =
    Index(
      key = Seq(
        throttleIdKey -> IndexType.Ascending,
        timecodeKey -> IndexType.Ascending
      ),
      name = Some("idTimecodeIndex"),
      unique = true
    )

  lazy val ttlIndex: Index = Index(
    Seq((lastUpdatedTimestampKey, IndexType.Ascending)),
    name = Some("throttleExpiryIndex"),
    options = BSONDocument("expireAfterSeconds" -> appConfig.timeToLiveSeconds)
  )

  collection.indexesManager.ensure(idTimecodeIndex)
  collection.indexesManager.ensure(ttlIndex)

}


