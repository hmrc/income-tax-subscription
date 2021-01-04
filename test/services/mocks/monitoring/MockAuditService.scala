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

/*
 * Copyright 2019 HM Revenue & Customs
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

package services.mocks.monitoring

import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, Suite}
import play.api.mvc.Request
import services.monitoring.{AuditModel, AuditService, ExtendedAuditModel}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext

trait MockAuditService extends MockitoSugar with BeforeAndAfterEach {
  self: Suite =>
  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockAuditService)
  }

  val mockAuditService = mock[AuditService]

  def verifyAudit(model: AuditModel): Unit =
    verify(mockAuditService).audit(
      ArgumentMatchers.eq(model)
    )(
      ArgumentMatchers.any[HeaderCarrier],
      ArgumentMatchers.any[ExecutionContext],
      ArgumentMatchers.any[Request[_]]
    )

  def verifyExtendedAudit(model: ExtendedAuditModel): Unit =
    verify(mockAuditService).extendedAudit(
      ArgumentMatchers.eq(model)
    )(
      ArgumentMatchers.any[HeaderCarrier],
      ArgumentMatchers.any[ExecutionContext],
      ArgumentMatchers.any[Request[_]]
    )
}