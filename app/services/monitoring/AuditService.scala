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

package services.monitoring

import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.libs.json.JsValue
import play.api.mvc.Request
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.AuditExtensions
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.{DataEvent, ExtendedDataEvent}
import utils.Implicits.eitherUtilLeft

import scala.concurrent.ExecutionContext

@Singleton
class AuditService @Inject()(configuration: Configuration,
                             auditConnector: AuditConnector) {

  private lazy val appName: String = configuration.get[String]("appName")

  def audit(dataSource: AuditModel)(implicit hc: HeaderCarrier, ec: ExecutionContext, request: Request[_]): Unit =
    auditConnector.sendEvent(toDataEvent(appName, dataSource, request.path))

  def extendedAudit(dataSource: ExtendedAuditModel)(implicit hc: HeaderCarrier, ec: ExecutionContext, request: Request[_]): Unit =
    auditConnector.sendExtendedEvent(toExtendedDataEvent(appName, dataSource, request.path))

  def toDataEvent(appName: String, auditModel: AuditModel, path: String)(implicit hc: HeaderCarrier): DataEvent = {
    val auditType: String = auditModel.auditType
    val transactionName: String = auditModel.transactionName
    val detail: Map[String, String] = auditModel.detail
    val tags: Map[String, String] = Map.empty[String, String]

    DataEvent(
      auditSource = appName,
      auditType = auditType,
      tags = AuditExtensions.auditHeaderCarrier(hc).toAuditTags(transactionName, path) ++ tags,
      detail = AuditExtensions.auditHeaderCarrier(hc).toAuditDetails(detail.toSeq: _*)
    )
  }

  def toExtendedDataEvent(appName: String, extendedAuditModel: ExtendedAuditModel, path: String)(implicit hc: HeaderCarrier): ExtendedDataEvent = {
    val auditType: String = extendedAuditModel.auditType
    val transactionName: String = extendedAuditModel.transactionName
    val detail: JsValue = extendedAuditModel.detail
    val tags: Map[String, String] = Map.empty[String, String]

    ExtendedDataEvent(
      auditSource = appName,
      auditType = auditType,
      tags = AuditExtensions.auditHeaderCarrier(hc).toAuditTags(transactionName, path) ++ tags,
      detail = detail
    )
  }
}

trait AuditModel {
  val auditType: String
  val transactionName: String
  val detail: Map[String, String]
}

trait ExtendedAuditModel {
  val auditType: String
  val transactionName: String
  val detail: JsValue
}
