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

package controllers

import common.Extractors
import connectors.ItsaIncomeSourceConnector
import models.subscription.CreateIncomeSourcesModel
import play.api.Logging
import play.api.libs.json.JsValue
import play.api.mvc._
import services.AuthService
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class BusinessIncomeSourcesController @Inject()(authService: AuthService,
                                                itsaIncomeSourceConnector: ItsaIncomeSourceConnector,
                                                cc: ControllerComponents)
                                               (implicit ec: ExecutionContext)
  extends BackendController(cc) with Extractors with Logging {

  def createIncomeSource(mtdbsaRef: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    authService.authorised().retrieve(Retrievals.allEnrolments) { enrolments =>
      withJsonBody[CreateIncomeSourcesModel] { incomeSources =>
        itsaIncomeSourceConnector.createIncomeSources(
          agentReferenceNumber = getArnFromEnrolments(enrolments),
          mtdbsaRef = mtdbsaRef,
          createIncomeSources = incomeSources
        ) map {
          case Right(_) =>
            NoContent
          case Left(error) =>
            logger.error(s"Error processing Create Income Sources with status ${error.status} and message ${error.reason}")
            InternalServerError("Create Income Sources Failure")
        }
      }
    }
  }
}
