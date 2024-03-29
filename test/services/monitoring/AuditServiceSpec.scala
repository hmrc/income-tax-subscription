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

package services.monitoring

import common.CommonSpec
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.Configuration
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

class AuditServiceSpec extends CommonSpec with MockitoSugar with BeforeAndAfterEach {
  private val mockAuditConnector = mock[AuditConnector]
  private val mockConfiguration = mock[Configuration]
  private val testAppName = "app"

  val testAuditService = new AuditService(mockConfiguration, mockAuditConnector)

  private implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit private val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("POST", "testUrl")

  private val testAuditModel: AuditModel = new AuditModel {
    override val auditType = "testAuditType"
    override val transactionName: Option[String] = Some("testTransactionName")
    override val detail: Map[String, String] = Map[String, String]()
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockAuditConnector)
    reset(mockConfiguration)

    when(mockConfiguration.get[String]("appName")) thenReturn testAppName
  }

  "audit" when {
    "given a auditable data type" should {
      "extract the data and pass it into the AuditConnector" in {

        val expectedData = testAuditService.toDataEvent(testAppName, testAuditModel, "testUrl")

        testAuditService.audit(testAuditModel)

        verify(mockAuditConnector)
          .sendEvent(
            ArgumentMatchers.refEq(expectedData, "eventId", "generatedAt")
          )(
            ArgumentMatchers.any[HeaderCarrier],
            ArgumentMatchers.any[ExecutionContext]
          )
      }
    }
  }
}
