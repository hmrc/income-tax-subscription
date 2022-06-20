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

import play.api.Logging
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.{AuthService, SubscriptionDataService}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubscriptionDataController @Inject()(authService: AuthService,
                                           subscriptionDataService: SubscriptionDataService,
                                           cc: ControllerComponents)
                                          (implicit ec: ExecutionContext) extends BackendController(cc) with Logging {

  def retrieveReference: Action[JsValue] = Action.async(parse.json) { implicit request =>
    authService.authorised().retrieve(Retrievals.credentials) {
      case Some(credentials) =>
        (request.body \ "utr").validate[String] match {
          case JsSuccess(utr, _) =>
            subscriptionDataService.retrieveReference(utr, credentials.providerId) map{
              case SubscriptionDataService.Existing(reference) => Ok(Json.obj("reference" -> reference))
              case SubscriptionDataService.Created(reference) => Created(Json.obj("reference" -> reference))
            }
          case JsError(_) =>
            logger.error("[SubscriptionDataController][retrieveReference] - Could not parse json request.")
            Future.successful(InternalServerError(
              s"[SubscriptionDataController][retrieveReference] - Could not parse json request."
            ))
        }
      case None =>
        logger.error("[SubscriptionDataController][retrieveReference] - Could not retrieve users credentials.")
        Future.successful(InternalServerError(
          "[SubscriptionDataController][retrieveReference] - Could not retrieve users credentials."
        ))
    }
  }

  def getAllSubscriptionData(reference: String): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      subscriptionDataService.getAllSubscriptionData(reference).map {
        case Some(data) => Ok(data)
        case None => NoContent
      }
    }
  }

  def retrieveSubscriptionData(reference: String, id: String): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      subscriptionDataService.retrieveSubscriptionData(reference, id).map {
        case Some(data) => Ok(data)
        case None => NoContent
      }
    }
  }

  def insertSubscriptionData(reference: String, id: String): Action[JsValue] = Action.async(parse.json) { implicit request =>
    authService.authorised() {
      subscriptionDataService.insertSubscriptionData(
        reference = reference,
        dataId = id,
        request.body
      ).map(_ => Ok)
    }
  }

  def deleteSubscriptionData(reference: String, id: String): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      subscriptionDataService.deleteSubscriptionData(
        reference = reference,
        dataId = id
      ).map(_ => Ok)
    }
  }

  def deleteAllSubscriptionData(reference: String): Action[AnyContent] = Action.async { implicit request =>
    authService.authorised() {
      subscriptionDataService.deleteAllSubscriptionData(reference).map { result =>
        if (result.wasAcknowledged()) {
          Ok
        } else {
          throw new RuntimeException(
            "[SubscriptionDataController][deleteAllSessionData] - delete session data failed"
          )
        }
      }
    }
  }

}
