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

import config.featureswitch.FeatureSwitch.prefix
import testonly.controllers.featureswitch.FeatureSwitchSetting

sealed trait FeatureSwitch {
  val name: String
  val displayName: String
}

object FeatureSwitch {
  val prefix = "feature-switch"

  val switches: Set[FeatureSwitch] = Set(
    StubDESFeature,
    SubmitUtrToSignUp,
    UseHIPSignUpTaxYearAPI,
    NewGetITSAStatusAPI,
    GetNewITSABusinessDetails
  )

  def apply(str: String): FeatureSwitch =
    switches find (_.name == str) match {
      case Some(switch) => switch
      case None => throw new IllegalArgumentException("Invalid feature switch: " + str)
    }

  def apply(setting: FeatureSwitchSetting): FeatureSwitch =
    switches find (_.displayName == setting.feature) match {
      case Some(switch) => switch
      case None => throw new IllegalArgumentException("Invalid feature switch: " + setting.feature)
    }
}

object StubDESFeature extends FeatureSwitch {
  val displayName = s"Use stub for DES connection"
  val name = s"$prefix.stub-des"
}

object SubmitUtrToSignUp extends FeatureSwitch {
  val displayName = "Submit UTR to API 1565"
  val name = s"$prefix.submit-utr-to-api-1565"
}

object UseHIPSignUpTaxYearAPI extends FeatureSwitch {
  val displayName = s"Use HIP SignUp Tax Year API "
  val name = s"$prefix.use-hip-signup-tax-year-api"
}

object NewGetITSAStatusAPI extends FeatureSwitch {
  val displayName = "Use HIP Get ITSA Status API"
  val name = s"$prefix.replace-get-itsa-status"
}

object GetNewITSABusinessDetails extends FeatureSwitch {
  val displayName = s"Get Business Details from API 5266"
  val name = s"$prefix.get-business-details-api-5266"
}