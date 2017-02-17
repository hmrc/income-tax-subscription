package models.gg

import play.api.libs.json.Json

case class KnownFact(`type`: String, value: String)

object KnownFact {
  implicit val format = Json.format[KnownFact]
}

