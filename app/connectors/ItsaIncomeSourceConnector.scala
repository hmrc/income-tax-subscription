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
import config.featureswitch.FeatureSwitch.SubmissionAuditUpdate
import config.featureswitch.FeatureSwitching
import connectors.hip.BaseHIPConnector
import models.monitoring.{CreateIncomeSourcesAudit, SignUpAudit}
import models.subscription.CreateIncomeSourcesModel
import models.subscription.business.CreateIncomeSourceErrorModel
import org.apache.pekko.actor.ActorSystem
import parsers.ITSAIncomeSourceParser.*
import play.api.http.Status.TOO_MANY_REQUESTS
import play.api.libs.json.Json
import play.api.mvc.Request
import services.monitoring.AuditService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ItsaIncomeSourceConnector @Inject()(val httpClient: HttpClientV2,
                                          val appConfig: AppConfig,
                                          val configuration: Config,
                                          val actorSystem: ActorSystem,
                                          auditService: AuditService)
                                         (implicit val ec: ExecutionContext) extends BaseHIPConnector with ConnectorRetries with FeatureSwitching {

  private def itsaIncomeSourceUrl =
    "/etmp/RESTAdapter/itsa/taxpayer/income-source"

  def createIncomeSources(agentReferenceNumber: Option[String],
                          mtdbsaRef: String,
                          createIncomeSources: CreateIncomeSourcesModel)
                         (implicit hc: HeaderCarrier, request: Request[_]): Future[PostITSAIncomeSourceResponse] = {
    auditService.extendedAudit(SignUpAudit(agentReferenceNumber, createIncomeSources, appConfig.getHipAuthToken))

    updateETMP(mtdbsaRef, createIncomeSources) flatMap { result =>
      if (isEnabled(SubmissionAuditUpdate)) {
        auditService.extendedAudit(CreateIncomeSourcesAudit(
          agentReferenceNumber   = agentReferenceNumber,
          nino                   = createIncomeSources.nino,
          mtdItsaReferenceNumber = mtdbsaRef,
          isSuccess              = result.isRight,
          errorReason            = result.left.toOption.map(_.reason)
        )) map { _ => result }
      } else {
        Future.successful(result)
      }
    }
  }

  private def updateETMP(mtdbsaRef: String,
                         createIncomeSources: CreateIncomeSourcesModel)
                        (implicit hc: HeaderCarrier): Future[PostITSAIncomeSourceResponse] = {
    retryFor[PostITSAIncomeSourceResponse](ITSAIncomeSourceResponseHttpReads.apiNumber, ITSAIncomeSourceResponseHttpReads.apiName) {
      case Left(CreateIncomeSourceErrorModel(TOO_MANY_REQUESTS, _)) => true
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
      )
    }
  }
}