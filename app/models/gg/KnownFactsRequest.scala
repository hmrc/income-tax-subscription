package models.gg

import play.api.libs.json.Json


case class KnownFactsRequest(facts: List[KnownFact])

object KnownFactsRequest {
  implicit val formats = Json.format[KnownFactsRequest]
}
