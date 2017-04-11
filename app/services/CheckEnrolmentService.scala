package services

import javax.inject.{Inject, Singleton}

import audit.{Logging, LoggingConfig}
import connectors.BusinessDetailsConnector
import models.ErrorModel
import models.frontend.FESuccessResponse
import play.api.http.Status._
import uk.gov.hmrc.play.http.HeaderCarrier
import utils.Implicits._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CheckEnrolmentService @Inject()(businessDetailsConnector: BusinessDetailsConnector,
                                      logging: Logging) {

  def checkAlreadyEnrolled(nino: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Either[ErrorModel, Option[FESuccessResponse]]] = {
    logging.debug(s"Request: NINO=$nino")
    implicit val checkAlreadyEnrolledLoggingConfig = CheckEnrolmentService.checkAlreadyEnrolledLoggingConfig
    businessDetailsConnector.getBusinessDetails(nino).flatMap {
      case Left(error: ErrorModel) if error.status == NOT_FOUND =>
        logging.debug(s"No mtditsa enrolment for nino=$nino")
        Right(None)
      case Right(x) =>
        logging.debug(s"Client is already enrolled with mtditsa, ref=${x.mtdbsa}")
        Right(Some(FESuccessResponse(x.mtdbsa)))
      case Left(x) => Left(x)
    }
  }

}

object CheckEnrolmentService {
  val checkAlreadyEnrolledLoggingConfig: Option[LoggingConfig] = LoggingConfig(heading = "CheckEnrolmentService.checkAlreadyEnrolled")
}

