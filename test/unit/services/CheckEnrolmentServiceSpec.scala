package unit.services

import models.frontend.FESuccessResponse
import play.api.http.Status._
import uk.gov.hmrc.play.http.HeaderCarrier
import unit.services.mocks.MockCheckEnrolmentService
import utils.TestConstants._

import scala.concurrent.ExecutionContext.Implicits.global

class CheckEnrolmentServiceSpec extends MockCheckEnrolmentService {

  implicit val hc = HeaderCarrier()

  "CheckEnrolmentService.checkAlreadyEnrolled" should {

    def call = await(TestCheckEnrolmentService.checkAlreadyEnrolled(testNino))

    "return the Right(NONE) when the person does not have a mtditsa enrolment" in {
      mockBusinessDetails(getBusinessDetailsNotFound)
      call.right.get shouldBe None
    }

    "return the Right(Some(FESuccessResponse)) when the person already have a mtditsa enrolment" in {
      mockBusinessDetails(getBusinessDetailsSuccess)
      // testMtditId must be the same value defined in getBusinessDetailsSuccess
      call.right.get shouldBe Some(FESuccessResponse(testMtditId))
    }

    "return the error for other error type" in {
      mockBusinessDetails(getBusinessDetailsServerError)
      call.left.get.status shouldBe INTERNAL_SERVER_ERROR
    }

  }

}
