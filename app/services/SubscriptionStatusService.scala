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

package services

import config.AppConfig
import config.featureswitch.{FeatureSwitching, GetNewITSABusinessDetails}
import connectors.{BusinessDetailsConnector, GetITSABusinessDetailsConnector}
import models.ErrorModel
import models.frontend.FESuccessResponse
import parsers.GetITSABusinessDetailsParser
import play.api.Logging
import play.api.http.Status._
import play.api.mvc.Request
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SubscriptionStatusService @Inject()(val appConfig: AppConfig,
                                          businessDetailsConnector: BusinessDetailsConnector,
                                          itsaBusinessDetailsConnector: GetITSABusinessDetailsConnector)
  extends Logging with FeatureSwitching {

  /*
  * This method will check to see if a user with the supplied nino has an MTD IT SA subscription
  * if will return OK with the reference if it is found, or OK with {} if it is not found
  * it will return all other errors as they were
  **/

  def checkMtditsaSubscription(nino: String)
                              (implicit hc: HeaderCarrier, ec: ExecutionContext, request: Request[_]): Future[Either[ErrorModel, Option[FESuccessResponse]]] = {
    if (isEnabled(GetNewITSABusinessDetails)) {
      itsaBusinessDetailsConnector.getHIPBusinessDetails(nino).map {
        case GetITSABusinessDetailsParser.AlreadySignedUp(mtdId) => Right(Some(FESuccessResponse(Some(mtdId))))
        case GetITSABusinessDetailsParser.NotSignedUp => Right(None)
      }
    } else {
      businessDetailsConnector.getBusinessDetails(nino) map {
        case Right(response) =>
          Right(Some(FESuccessResponse(Some(response.mtdId))))
        case Left(error: ErrorModel) =>
          if (error.status == NOT_FOUND) {
            Right(None)
          } else {
            Left(error)
          }
      }
    }
  }
}

