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

package controllers

import config.AppConfig
import config.featureswitch.{FeatureSwitching, SaveAndRetrieve}
import connectors.CreateIncomeSourcesConnector
import models.subscription.{BusinessSubscriptionDetailsModel, CreateIncomeSourcesModel}
import play.api.Logger
import play.api.libs.json.JsValue
import play.api.mvc._
import services.AuthService
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier, Enrolments}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.collection.immutable.::
import scala.concurrent.ExecutionContext

@Singleton
class BusinessIncomeSourcesController @Inject()(authService: AuthService,
                                                createIncomeSourcesConnector: CreateIncomeSourcesConnector,
                                                cc: ControllerComponents,
                                                val appConfig: AppConfig
                                               )
                                               (implicit ec: ExecutionContext) extends BackendController(cc) with FeatureSwitching {

  val logger: Logger = Logger(this.getClass)

  def createIncomeSource(mtdbsaRef: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    authService.authorised().retrieve(Retrievals.allEnrolments) { enrolments =>
      val agentReferenceNumber: Option[String] = getArnFromEnrolments(enrolments)
      if (isEnabled(SaveAndRetrieve)) {
        withJsonBody[CreateIncomeSourcesModel] { incomeSources =>
          createIncomeSourcesConnector.createBusinessIncomeSources(agentReferenceNumber, mtdbsaRef, incomeSources).map {
            case Right(_) => NoContent
            case Left(error) => logger.error(s"Error processing Business Income Source with status ${error.status} and message ${error.reason}")
              InternalServerError("Business Income Source Failure")
          }
        }
      } else {
        withJsonBody[BusinessSubscriptionDetailsModel] { incomeSourceRequest =>
          createIncomeSourcesConnector.createBusinessIncome(agentReferenceNumber, mtdbsaRef, incomeSourceRequest).map {
            case Right(_) => NoContent
            case Left(error) => logger.error(s"Error processing Business Income Source with status ${error.status} and message ${error.reason}")
              InternalServerError("Business Income Source Failure")
          }
        }
      }
    }
  }

  private def getArnFromEnrolments(enrolments: Enrolments): Option[String] = enrolments.enrolments.collectFirst {
    case Enrolment("HMRC-AS-AGENT", EnrolmentIdentifier(_, value) :: _, _, _) => value
  }

}
