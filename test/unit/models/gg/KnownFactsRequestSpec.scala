package unit.models.gg

import models.gg.{KnownFact, KnownFactsRequest}
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.play.test.UnitSpec
import utils.JsonUtils._

class KnownFactsRequestSpec extends UnitSpec {

  val testType1 = "MOSW2Number"
  val testValue1 = "10"
  val testType2 = "MOSW2ID"
  val testValue2 = "A"

  "KnownFact" should {
    "Provide the correct writer for KnownFact" in {
      val knownFact = KnownFact(
        `type` = testType1,
        value = testValue1
      )

      val request: JsValue = Json.toJson(knownFact)
      val expected = Json.fromJson[KnownFact](
        s"""{"type" : "$testType1",
           | "value" : "$testValue1"
           | }""".stripMargin).get
      val actual = Json.fromJson[KnownFact](request).get

      actual shouldBe expected
    }
  }

  "KnownFactsRequest" should {
    "Provide the correct writer for KnownFactsRequest" in {
      val knownFact1 = KnownFact(
        `type` = testType1,
        value = testValue1
      )
      val knownFact2 = KnownFact(
        `type` = testType2,
        value = testValue2
      )
      val knownFactsRequest = KnownFactsRequest(List(knownFact1, knownFact2))

      val request: JsValue = Json.toJson(knownFactsRequest)
      val expected = Json.fromJson[KnownFactsRequest](
        s"""{
           | "facts": [
           |        { "type" : "MOSW2Number", "value": "10" },
           |        { "type" : "MOSW2ID", "value": "A" }]
           | }""".stripMargin).get
      val actual = Json.fromJson[KnownFactsRequest](request).get

      actual shouldBe expected
    }
  }

}
