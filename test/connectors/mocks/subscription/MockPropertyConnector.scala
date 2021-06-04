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

package connectors.mocks.subscription

import connectors.PropertyConnector
import models.subscription.incomesource.PropertyIncomeModel
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Request
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

trait MockPropertyConnector extends MockitoSugar {

  val mockPropertyConnector: PropertyConnector = mock[PropertyConnector]

  def mockPropertySubscribe(nino: String, propertyIncomeModel: PropertyIncomeModel, arn: Option[String])
                           (response: Future[String])
                           (implicit hc: HeaderCarrier): Unit = {

    when(mockPropertyConnector.propertySubscribe(
      ArgumentMatchers.eq(nino),
      ArgumentMatchers.eq(propertyIncomeModel),
      ArgumentMatchers.eq(arn)
    )(ArgumentMatchers.any[HeaderCarrier], ArgumentMatchers.any[Request[_]])).thenReturn(response)

  }

}
