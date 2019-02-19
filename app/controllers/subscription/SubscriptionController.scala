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

package controllers.subscription

import javax.inject.Inject
import audit.{Logging, LoggingConfig}
import controllers.ITSASessionKeys
import models.frontend.{FEFailureResponse, FERequest}
import play.api.libs.json.{JsError, JsSuccess}
import play.api.mvc._
import services.{AuthService, RosmAndEnrolManagerService}
import uk.gov.hmrc.play.bootstrap.controller.BaseController
import utils.JsonUtils._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier

class SubscriptionController @Inject()(logging: Logging,
                                       subManService: RosmAndEnrolManagerService,
                                       authService: AuthService,
                                       cc: ControllerComponents
                                      ) extends BaseController(cc) {
  import authService._

  def subscribe(nino: String): Action[AnyContent] = Action.async { implicit request =>
    val path = request.headers.get(ITSASessionKeys.RequestURI) match {
      case Some(value) => value
      case None =>
        val noAuditPath = "-"
        logging.err(s"Expected ${ITSASessionKeys.RequestURI} in HeaderCarrier, using '$noAuditPath' as audit path")
        noAuditPath
    }

    implicit val loggingConfig = SubscriptionController.subscribeLoggingConfig
    logging.debug(s"Request received for $nino")

    authorised() {
      parseRequest(request) match {
        case Some(feRequest) => createSubscription(feRequest, path)
        case None => BadRequest(toJsValue(FEFailureResponse("Request is invalid")))
      }
    }
  }

  private def parseRequest(request: Request[AnyContent]): Option[FERequest] = for {
    jsonBody <- request.body.asJson
    parsedBody <- jsonBody.validate[FERequest] match {
      case JsSuccess(body, _) => Some(body)
      case JsError(errors) =>
        logging.err(s"Request is invalid:\n${errors.toString}\n${request.body.toString}")
        None
    }
  } yield parsedBody

  private def createSubscription(feRequest: FERequest, path: String)(implicit hc: HeaderCarrier): Future[Result] =
    subManService.rosmAndEnrol(feRequest, path).map {
    case Right(r) =>
      logging.debug(s"Subscription successful, responding with\n$r")
      Ok(toJsValue(r))
    case Left(l) =>
      logging.warn(s"Subscription failed, responding with\nstatus=${l.status}\nreason=${l.reason}")
      Status(l.status)(toJsValue(FEFailureResponse(l.reason)))
  }

}

object SubscriptionController {
  val subscribeLoggingConfig: Option[LoggingConfig] = LoggingConfig(heading = "SubscriptionController.subscribe")
  val parseRequestLoggingConfig: Option[LoggingConfig] = LoggingConfig(heading = "SubscriptionController.parseRequest")
}
