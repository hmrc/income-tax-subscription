/*
 * Copyright 2020 HM Revenue & Customs
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

package utils

import javax.inject.{Inject, Singleton}
import play.api.{Configuration, Logger}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.Audit

case class LoggingConfig(heading: String)

object LoggingConfig {

  implicit class LoggingConfigUtil(config: Option[LoggingConfig]) {
    def addHeading(message: String): String = config.fold(message)(x => x.heading + ": " + message)
  }

}

@Singleton
class Logging @Inject()(configuration: Configuration,
                        auditConnector: AuditConnector) {


  lazy val appName: String = configuration.get[String]("appName")

  lazy val debugToWarn: Boolean = configuration.getOptional[String]("feature-switching.debugToWarn").fold(false)(x => x.toBoolean)

  lazy val audit: Audit = new Audit(appName, auditConnector)
  

  @inline def trace(msg: String)(implicit config: Option[LoggingConfig] = None): Unit = Logger.trace(config.addHeading(msg))

  @inline def debug(msg: String)(implicit config: Option[LoggingConfig] = None): Unit = Logger.debug(config.addHeading(msg))

  @inline def info(msg: String)(implicit config: Option[LoggingConfig] = None): Unit = Logger.info(config.addHeading(msg))

  @inline def warn(msg: String)(implicit config: Option[LoggingConfig] = None): Unit = Logger.warn(config.addHeading(msg))

  @inline def err(msg: String)(implicit config: Option[LoggingConfig] = None): Unit = Logger.error(config.addHeading(msg))

}

object Logging {

  val splunkString = "SPLUNK AUDIT:\n"


  val eventTypeRequest: String = "Request"
  val eventTypeSuccess: String = "Success"
  val eventTypeFailure: String = "Failure"
  val eventTypeBadRequest: String = "BadRequest"
  val eventTypeConflict: String = "Conflict"
  val eventTypeNotFound: String = "NotFound"
  val eventTypeInternalServerError: String = "InternalServerError"
  val eventTypeServerUnavailable: String = "ServerUnavailable"
  val eventTypeUnexpectedError: String = "UnexpectedError"
}

