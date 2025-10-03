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

package config.featureswitch

import common.CommonSpec
import config.MicroserviceAppConfig
import config.featureswitch.FeatureSwitching.{FEATURE_SWITCH_OFF, FEATURE_SWITCH_ON}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

class FeatureSwitchingSpec extends CommonSpec with FeatureSwitching with MockitoSugar with BeforeAndAfterEach {

  val mockServicesConfig: ServicesConfig = mock[ServicesConfig]
  val mockConfig: Configuration = mock[Configuration]

  when(mockConfig.getOptional(any())(any())).thenReturn(None)

  override val appConfig = new MicroserviceAppConfig(mockServicesConfig, mockConfig)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockConfig)
    FeatureSwitch.switches foreach { switch =>
      sys.props -= switch.name
    }
  }

  "FeatureSwitching constants" should {
    "be true" in {
      FEATURE_SWITCH_ON shouldBe "true"
    }

    "be false" in {
      FEATURE_SWITCH_OFF shouldBe "false"
    }
  }
  
}
