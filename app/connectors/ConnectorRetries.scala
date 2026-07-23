/*
 * Copyright 2026 HM Revenue & Customs
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
import org.apache.pekko.event.LoggingReceive
import org.apache.pekko.pattern.after
import play.api.Logging
import uk.gov.hmrc.mdc.Mdc

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.*
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters.*
import scala.util.{Failure, Success, Try}

trait ConnectorRetries extends Logging {

  protected def actorSystem: ActorSystem

  protected def configuration: Config

  protected def logCounter: Option[LogCounter] = None
  
  def retryFor[A](apiNumber: Int, desc: String, level: PartialFunction[ErrorModel, Level] = _ => Error)
                 (condition: PartialFunction[Either[ErrorModel, A], Boolean])
                 (block: => Future[Either[ErrorModel, A]])
                 (implicit ec: ExecutionContext): Future[Either[ErrorModel, A]] = {

    def loop(remainingIntervals: Seq[FiniteDuration]): Future[Either[ErrorModel, A]] = {
      // scheduling will loose MDC data. Here we explicitly ensure it is available on block.
      block.flatMap { result =>
        val mustRetry = condition.lift(result).getOrElse(false)
        if (mustRetry && remainingIntervals.nonEmpty) {
          val delay = remainingIntervals.head
          logger.warn(s"Retrying [API #$apiNumber - $desc] in $delay due to error")
          val mdcData = Mdc.mdcData
          after(delay, actorSystem.scheduler) {
            Mdc.putMdc(mdcData)
            loop(remainingIntervals.tail)
          }
        } else {
          Future.successful(result)
        }
      }
    }

    val result = loop(intervals(apiNumber))
    result.onComplete {
      case Success(Left(error)) => logError(error, level)
      case Success(Right(_)) => {}
      case Failure(exception) => throw exception
    }
    result
  }

  private def intervals(apiNumber: Int): Seq[FiniteDuration] = {
    val root = "retries.intervals"
    val durations = Try {
      configuration.getDurationList(s"$root.$apiNumber").asScala.toSeq
    } match {
      case Success(list) if list.nonEmpty => list
      case _ => configuration.getDurationList(root).asScala.toSeq
    }
    durations.map { d =>
      FiniteDuration(d.toMillis, TimeUnit.MILLISECONDS)
    }
  }
  
  private def logError(error: ErrorModel, level: PartialFunction[ErrorModel, Level]): Unit = {
    val value = level.lift(error).getOrElse(Error)
    logCounter.foreach(_.count(value))
    value match {
      case Info => logger.info(error.reason)
      case Warn => logger.warn(error.reason)
      case Error => logger.error(error.reason)
      case Off => {}
    }
  }
}

sealed trait Level {
  val name: String
}

object Off extends Level {
  override val name: String = "OFF"
}

object Info extends Level{
  override val name: String = "INFO"
}

object Warn extends Level{
  override val name: String = "WARN"
}

object Error extends Level{
  override val name: String = "ERROR"
}

trait LogCounter {
  def count(level: Level): Unit
}
