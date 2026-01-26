/*
 * Copyright 2025 HM Revenue & Customs
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

package connectors

import com.typesafe.config.Config
import config.AppConfig
import connectors.hip.BaseHIPConnector
import models.monitoring.CompletedSignUpAudit
import models.subscription.CreateIncomeSourcesModel
import org.apache.pekko.actor.ActorSystem
import parsers.ITSAIncomeSourceParser.*
import play.api.libs.json.Json
import play.api.mvc.Request
import services.monitoring.AuditService
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, Retries}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ItsaIncomeSourceConnector @Inject()(val httpClient: HttpClientV2,
                                          val appConfig: AppConfig,
                                          val configuration: Config,
                                          val actorSystem: ActorSystem,
                                          auditService: AuditService)
                                         (implicit val ec: ExecutionContext) extends BaseHIPConnector with Retries {

  private def itsaIncomeSourceUrl =
    "/etmp/RESTAdapter/itsa/taxpayer/income-source"

  def createIncomeSources(agentReferenceNumber: Option[String],
                          mtdbsaRef: String,
                          createIncomeSources: CreateIncomeSourcesModel)
                         (implicit hc: HeaderCarrier, request: Request[_]): Future[PostITSAIncomeSourceResponse] = {
    retryFor("# API #5265 - ITSA Income Source") {
      case ITSAIncomeSourceForbiddenException(_) => true
      case _ => false
    } {
      val headers: Map[String, String] = Map(
        "X-Message-Type" -> "CreateIncomeSource"
      )

      val body = Json.toJson(createIncomeSources)(CreateIncomeSourcesModel.hipWrites(mtdbsaRef))

      super.post(
        uri = itsaIncomeSourceUrl,
        body = body,
        parser = ITSAIncomeSourceResponseHttpReads,
        additionalHeaders = headers
      ) flatMap {
        case Left(error) =>
          Future.successful(Left(error))
        case Right(value) =>
          auditService.extendedAudit(CompletedSignUpAudit(agentReferenceNumber, createIncomeSources, appConfig.getHipAuthToken)) map {
            _ => Right(value)
          }
      }
    }
  }
}