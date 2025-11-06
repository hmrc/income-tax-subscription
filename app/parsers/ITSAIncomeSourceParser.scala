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

package parsers

import models.subscription.business.{CreateIncomeSourceErrorModel, CreateIncomeSourceSuccessModel}
import parsers.hip.Parser
import play.api.Logging
import play.api.http.Status.CREATED
import play.api.http.Status.FORBIDDEN
import uk.gov.hmrc.http.{HttpReads, HttpResponse, InternalServerException}

object ITSAIncomeSourceParser extends Logging {
  type PostITSAIncomeSourceResponse = Either[CreateIncomeSourceErrorModel, CreateIncomeSourceSuccessModel]

    object itsaIncomeSourceResponseHttpReads extends Parser[PostITSAIncomeSourceResponse] {
      override def read(response: HttpResponse): PostITSAIncomeSourceResponse = {
        response.status match {
          case CREATED =>
            logger.debug("[ItsaIncomeSourcesResponseHttpReads][read]: Status Created")
            Right(CreateIncomeSourceSuccessModel())
          case FORBIDDEN =>
            logger.warn(s"[ItsaIncomeSourcesResponseHttpReads][read]: Unexpected response, status $FORBIDDEN returned. Body: ${response.body}")
            throw  ITSAIncomeSourceForbiddenException
          case status =>
            logger.warn(s"[ItsaIncomeSourcesResponseHttpReads][read]: Unexpected response, status $status returned. Body: ${response.body}")
            Left(CreateIncomeSourceErrorModel(status, response.body))
        }
      }
  }

  case object ITSAIncomeSourceForbiddenException extends InternalServerException(
    "[ITSAIncomeSourceParserException- Forbidden status received] "
  )
}
