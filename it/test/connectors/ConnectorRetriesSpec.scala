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

package connectors

import com.typesafe.config.{Config, ConfigFactory}
import org.apache.pekko.actor.ActorSystem
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime
import org.scalatestplus.play.PlaySpec

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

class ConnectorRetriesSpec extends PlaySpec
  with ScalaFutures {

  val connectorRetries: ConnectorRetries = new ConnectorRetries {
    override protected val configuration: Config = ConfigFactory.load()
    override val actorSystem: ActorSystem = ActorSystem("test-actor-system")

    override lazy val intervals: Seq[FiniteDuration] = List.fill(3)(1.milliseconds)
  }

  val SUCCESSFUL = "SUCCESSFUL"
  val FAILURE = "FAILURE"

  "ConnectorRetries.retryFor" should {
    "retry the body" when {
      "the body returned a match for something to retry" in {
        var counter = 0

        val result = connectorRetries.retryFor[String]("Test API failure retry") {
          case FAILURE => true
        } {
          if (counter < 2) {
            counter += 1
            Future.successful(FAILURE)
          } else {
            Future.successful(SUCCESSFUL)
          }
        }

        result.futureValue mustBe SUCCESSFUL
        counter mustBe 2
      }
    }
    "not retry the body" when {
      "the body returned a value which was not a match for something to retry" in {
        val result = connectorRetries.retryFor[String]("Test API failure retry") {
          case FAILURE => true
        } {
          Future.successful(SUCCESSFUL)
        }

        result.futureValue mustBe SUCCESSFUL
      }
    }
  }


}