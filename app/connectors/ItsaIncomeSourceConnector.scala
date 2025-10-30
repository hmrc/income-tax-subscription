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
import models.monitoring.CompletedSignUpAudit
import models.subscription.CreateIncomeSourcesModel
import org.apache.pekko.actor.ActorSystem
import parsers.ITSAIncomeSourceParser._
import play.api.libs.json.Json
import play.api.mvc.Request
import services.monitoring.AuditService
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, HeaderNames, Retries, StringContextOps}

import java.net.URL
import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId}
import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ItsaIncomeSourceConnector @Inject()(http: HttpClientV2,
                                          appConfig: AppConfig,
                                          auditService: AuditService,
                                          val configuration: Config,
                                          val actorSystem: ActorSystem
                                         )(implicit ec: ExecutionContext) extends Retries {

  private def itsaIncomeSourceUrl: URL =
    url"${appConfig.itsaIncomeSourceURL}/etmp/RESTAdapter/itsa/taxpayer/income-source"

  private val isoDatePattern: DateTimeFormatter = DateTimeFormatter
    .ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
    .withZone(ZoneId.of("UTC"))


  def createIncomeSources(agentReferenceNumber: Option[String],
                          mtdbsaRef: String,
                          createIncomeSources: CreateIncomeSourcesModel)
                         (implicit hc: HeaderCarrier, request: Request[_]): Future[PostITSAIncomeSourceResponse] = {

    val headerCarrier: HeaderCarrier = hc
      .copy(authorization = Some(Authorization(appConfig.itsaIncomeSourceAuthorisationToken)))

    retryFor("# API #5265 - ITSA Income Source") {
      case ITSAIncomeSourceForbiddenException => true
      case _ => false
    } {
      val hipHeaders: Map[String, String] = Map(
        HeaderNames.authorisation -> appConfig.itsaIncomeSourceAuthorisationToken,
        "correlationid" -> UUID.randomUUID().toString,
        "X-Message-Type" -> "CreateIncomeSource",
        "X-Originating-System" -> "MDTP",
        "X-Receipt-Date" -> isoDatePattern.format(Instant.now()),
        "X-Regime" -> "ITSA",
        "X-Transmitting-System" -> "HIP"
      )

      val call = http.post(itsaIncomeSourceUrl)(headerCarrier)
        .withBody(Json.toJson(createIncomeSources)(CreateIncomeSourcesModel.hipWrites(mtdbsaRef)))
      hipHeaders.foldLeft(call)((a, b) => a.setHeader(b)).execute flatMap {
        case Left(error) =>
          Future.successful(Left(error))
        case Right(value) =>
          auditService.extendedAudit(CompletedSignUpAudit(agentReferenceNumber, createIncomeSources, appConfig.itsaIncomeSourceAuthorisationToken)) map {
            _ => Right(value)
          }
      }
    }
  }
}