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

import com.typesafe.config.Config
import models.ErrorModel
import org.apache.pekko.actor.ActorSystem
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.play.PlaySpec

import java.time.Duration
import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ConnectorRetriesSpec extends PlaySpec
  with ScalaFutures
  with BeforeAndAfterEach {

  private val config = mock[Config]

  val counts: mutable.Map[Level, Int] = mutable.Map()

  class TestLogCounter extends LogCounter {
    override def count(level: Level): Unit = {
      counts.put(level, counts.getOrElse(level, 0) + 1)
    }
  }

  private val testLogCounter = new TestLogCounter

  private val connectorRetries: ConnectorRetries = new ConnectorRetries {
    override protected val configuration: Config = config
    override protected val actorSystem: ActorSystem = ActorSystem("test-actor-system")
    override protected val logCounter = Some(testLogCounter)
  }

  private val SUCCESSFUL = Right("SUCCESSFUL")
  private val FAILURE = Left(ErrorModel(0, "FAILURE"))

  private val apiOne = 0
  private val apiTwo = 1

  private val oneMilliSec: Duration = Duration.ofMillis(1)

  private val specific: java.util.List[Duration] = java.util.List.of(
    oneMilliSec,
    oneMilliSec,
    oneMilliSec
  )

  private val fallback: java.util.List[Duration] = java.util.List.of(
    oneMilliSec
  )

  private val root = "retries.intervals"

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(config)
    when(config.getDurationList(ArgumentMatchers.eq(s"$root.$apiOne"))).thenReturn(specific)
    when(config.getDurationList(ArgumentMatchers.eq(root))).thenReturn(fallback)
    counts.clear()
  }

  private def checkConfigUsed(apiNumber: Int, fallbackUsed: Boolean) = {
    verify(config, times(if (apiNumber == apiOne) 1 else 0)).getDurationList(ArgumentMatchers.eq(s"$root.$apiOne"))
    verify(config, times(if (apiNumber == apiTwo) 1 else 0)).getDurationList(ArgumentMatchers.eq(s"$root.$apiTwo"))
    verify(config, times(if (fallbackUsed) 1 else 0)).getDurationList(ArgumentMatchers.eq(root))
  }

  private def checkSingleLogFor(level: Level) = {
    Thread.sleep(100)
    counts.size mustBe 1
    counts.get(level) mustBe Some(1)
    Seq(Off, Info, Warn, Error).filterNot(_ == level).foreach { l =>
      counts.get(l) mustBe None
    }
  }

  "ConnectorRetries.retryFor" should {
    "retry the body" when {
      "the body returned a match for something to retry" in {
        var counter = 0

        val result = connectorRetries.retryFor[String](apiOne, "Test API failure retry") {
          case FAILURE => true
        } {
          counter += 1
          Future.successful(
            if (counter < 2) FAILURE else SUCCESSFUL
          )
        }

        // Function is called once and succeeds on first retry.
        result.futureValue mustBe SUCCESSFUL
        counter mustBe 2

        checkConfigUsed(
          apiNumber = apiOne,
          fallbackUsed = false
        )
      }

      "the body always returns a match for something to retry" in {
        var counter = 0

        val result = connectorRetries.retryFor[String](apiOne, "Test API failure retry") {
          case FAILURE => true
        } {
          counter += 1
          Future.successful(FAILURE)
        }

        // Function is called once and retried three times.
        result.futureValue mustBe FAILURE
        counter mustBe 4

        checkConfigUsed(
          apiNumber = apiOne,
          fallbackUsed = false
        )
      }
    }

    "not retry the body" when {
      "the body returned a value which was not a match for something to retry" in {
        var counter = 0

        val result = connectorRetries.retryFor[String](apiOne, "Test API failure retry") {
          case FAILURE => true
        } {
          counter += 1
          Future.successful(SUCCESSFUL)
        }

        // Function succeeds first time it is called
        result.futureValue mustBe SUCCESSFUL
        counter mustBe 1

        checkConfigUsed(
          apiNumber = apiOne,
          fallbackUsed = false
        )
      }
    }

    "uses fallback retry list" in {
      var counter = 0;

      val result = connectorRetries.retryFor[String](apiTwo, "Test API failure retry") {
        case FAILURE => true
      } {
        counter += 1
        Future.successful(FAILURE)
      }

      // Function is called once and retried once
      result.futureValue mustBe FAILURE
      counter mustBe 2

      checkConfigUsed(
        apiNumber = apiTwo,
        fallbackUsed = true
      )
    }

    Seq(Off, Info, Warn, Error).foreach { level =>
      s"use logging level of ${level.name} for errors" in {
        connectorRetries.retryFor[String](apiOne, "Test API failure retry", _ => level) {
          case FAILURE => true
        } {
          Future.successful(FAILURE)
        }

        checkSingleLogFor(level)
      }
    }
  }
}
