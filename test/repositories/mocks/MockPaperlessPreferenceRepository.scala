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

package repositories.mocks

import models.digitalcontact.PaperlessPreferenceKey
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import repositories.digitalcontact.PaperlessPreferenceMongoRepository
import utils.TestConstants._

import scala.concurrent.Future

trait MockPaperlessPreferenceRepository extends MockitoSugar {
  val mockPaperlessPreferenceRepository = mock[PaperlessPreferenceMongoRepository]

  def mockNinoStore(key: PaperlessPreferenceKey): Unit = {
    when(mockPaperlessPreferenceRepository.storeNino(key))
      .thenReturn(Future.successful(key))
  }

  def mockNinoStoreFailed(key: PaperlessPreferenceKey): Unit = {
    when(mockPaperlessPreferenceRepository.storeNino(key))
      .thenReturn(Future.failed(testException))
  }

  def mockNinoRetrieve(token: String): Unit = {
    when(mockPaperlessPreferenceRepository.retrieveNino(token))
      .thenReturn(Future.successful(Some(testPaperlessPreferenceKey)))
  }

  def mockNinoRetrieveNotFound(token: String): Unit = {
    when(mockPaperlessPreferenceRepository.retrieveNino(token))
      .thenReturn(Future.successful(None))
  }

  def mockNinoRetrieveFailed(token: String): Unit = {
    when(mockPaperlessPreferenceRepository.retrieveNino(token))
      .thenReturn(Future.failed(testException))
  }
}
