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
import org.apache.pekko.pattern.after
import play.api.Logging
import play.api.http.Status.FORBIDDEN
import uk.gov.hmrc.mdc.Mdc

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.*
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters.*

trait ConnectorRetries extends Logging {

  protected def actorSystem: ActorSystem

  protected def configuration: Config

  def retryFor[A](label: String)
                 (block: => Future[A])
                 (implicit ec: ExecutionContext): Future[A] =
    retryFor(label)(None)(block)

  def retryFor[A](label: String)
                 (condition: PartialFunction[A, Boolean])
                 (block: => Future[A])
                 (implicit ec: ExecutionContext): Future[A] =
    retryFor(label)(Some(condition))(block)

  private def retryFor[A](label: String)
                 (condition: Option[PartialFunction[A, Boolean]])
                 (block: => Future[A])
                 (implicit ec: ExecutionContext): Future[A] = {

    def loop(remainingIntervals: Seq[FiniteDuration]): Future[A] = {
      // scheduling will lose MDC data. Here we explicitly ensure it is available on block.
      block.flatMap { result =>
        val conditionMet: Boolean = result match {
          case Left(ErrorModel(FORBIDDEN, _, _)) => true
          case _ => condition.exists(_.lift(result).getOrElse(false))
        }
        if (conditionMet && remainingIntervals.nonEmpty) {
          val delay = remainingIntervals.head
          logger.warn(s"Retrying $label in $delay due to error")
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

    loop(intervals)
  }

  lazy val intervals: Seq[FiniteDuration] = {
    configuration.getDurationList("retries.intervals").asScala.toSeq.map { d =>
      FiniteDuration(d.toMillis, TimeUnit.MILLISECONDS)
    }
  }

}


