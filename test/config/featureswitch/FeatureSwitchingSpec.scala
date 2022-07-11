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

package config.featureswitch

import common.CommonSpec
import config.MicroserviceAppConfig
import config.featureswitch.FeatureSwitching.{FEATURE_SWITCH_OFF, FEATURE_SWITCH_ON}
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

class FeatureSwitchingSpec extends CommonSpec with FeatureSwitching with MockitoSugar with BeforeAndAfterEach {

  val mockServicesConfig: ServicesConfig = mock[ServicesConfig]
  val mockConfig: Configuration = mock[Configuration]
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

  "StubDESFeature" should {
    "return true if StubDESFeature feature switch is enabled in sys.props" in {
      enable(StubDESFeature)
      isEnabled(StubDESFeature) shouldBe true
    }
    "return false if StubDESFeature feature switch is disabled in sys.props" in {
      disable(StubDESFeature)
      isEnabled(StubDESFeature) shouldBe false
    }

    "return false if StubDESFeature feature switch does not exist" in {
      when(mockConfig.getOptional[String]("feature-switch.stub-des")).thenReturn(None)
      isEnabled(StubDESFeature) shouldBe false
    }

    "return false if StubDESFeature feature switch is not in sys.props but is set to 'off' in config" in {
      when(mockConfig.getOptional[String]("feature-switch.stub-des")).thenReturn(Some(FEATURE_SWITCH_OFF))
      isEnabled(StubDESFeature) shouldBe false
    }

    "return true if StubDESFeature feature switch is not in sys.props but is set to 'on' in config" in {
      when(mockConfig.getOptional[String]("feature-switch.stub-des")).thenReturn(Some(FEATURE_SWITCH_ON))
      isEnabled(StubDESFeature) shouldBe true
    }
  }

}
