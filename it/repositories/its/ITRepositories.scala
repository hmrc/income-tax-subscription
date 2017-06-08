/*
 * Copyright 2017 HM Revenue & Customs
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

package repositories.its

import repositories.TestThrottleMongoRepository
import services.its.ITTrait
import play.modules.reactivemongo.ReactiveMongoComponent
import repositories.Repositories
import uk.gov.hmrc.lock.LockRepository

trait ITRepositories extends ITTrait {

  object TestRepositories extends Repositories(app.injector.instanceOf[ReactiveMongoComponent]) {
    override lazy val throttleRepository = new TestThrottleMongoRepository
    override lazy val lockRepository = new LockRepository
  }

}


