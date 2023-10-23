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

import config.AppConfig
import play.api.libs.json.{Format, JsObject}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OldThrottleRepositoryConfig @Inject()(val appConfig: AppConfig) {

  def mongoComponent: MongoComponent = MongoComponent(appConfig.mongoUri)

}

@Singleton
class OldThrottleRepository @Inject()(config: OldThrottleRepositoryConfig)(implicit ec: ExecutionContext)
  extends PlayMongoRepository[JsObject](
    collectionName = "throttle",
    mongoComponent = config.mongoComponent,
    domainFormat = implicitly[Format[JsObject]],
    indexes = Seq.empty
  ) {

  def drop(): Future[Void] = {
    collection.drop().toFuture()
  }

}
