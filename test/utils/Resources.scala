/*
 * Copyright 2019 HM Revenue & Customs
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

import com.fasterxml.jackson.core.{JsonParseException, JsonProcessingException}
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.github.fge.jsonschema.main.{JsonSchema, JsonSchemaFactory}
import play.api.Logger
import play.api.libs.json.{JsValue, Json}

import scala.io.Source
import scala.util.{Failure, Success, Try}

trait Resources {

  private final lazy val jsonMapper = new ObjectMapper()
  private final lazy val jsonFactory = jsonMapper.getFactory

  lazy val defaultDataMap = Map(
    "$countryCode" -> load(Resources.countryCodeSchema)
  )

  def load(path: String): String = Source.fromURL(getClass.getResource(path)).mkString

  def loadAndParseJsonWithDummyData(path: String): JsValue = Json.parse(loadAndReplace(path, defaultDataMap))

  def loadAndReplace(path: String, replaceMap: Map[String, String] = defaultDataMap): String = {
    val jsonString: String = load(path).mkString
    replaceMap.foldLeft(jsonString)((json, mapEntry) => json.replace(mapEntry._1, mapEntry._2))
  }

  def loadSchema(schemaPath: String): JsonSchema = {
    val schemaMapper = new ObjectMapper()
    val factory = schemaMapper.getFactory
    val schemaParser = factory.createParser(loadAndReplace(schemaPath))
    val schemaJson: JsonNode = schemaMapper.readTree(schemaParser)
    val schemaFactory = JsonSchemaFactory.byDefault()
    schemaFactory.getJsonSchema(schemaJson)
  }

  def validateJson(schemaPath: String, json: JsValue): Boolean = {
    val schemaValidator = loadSchema(schemaPath)
    Try {
      val jsonParser = jsonFactory.createParser(json.toString)
      val jsonJson: JsonNode = jsonMapper.readTree(jsonParser)
      val report = schemaValidator.validate(jsonJson)
      report.isSuccess
    } match {
      case Success(result) => result
      case Failure(e: JsonParseException) =>
        Logger.error(s"getJsonValidationReport: There was an error parsing the Json: ${e.getMessage}")
        false
      case Failure(e: JsonProcessingException) =>
        Logger.error(s"getJsonValidationReport: There was an Json Validator Processing Exception: ${e.getMessage}")
        false
      case Failure(e) =>
        Logger.error(s"getJsonValidationReport: There was an a general exception: ${e.getMessage}")
        false
    }
  }

}


object Resources extends Resources {
  lazy val newRegistrationRequestSchema = "/schemas/new_registration_request.schema"
  lazy val newRegistrationResponseSchema = "/schemas/new_registration_response.schema"
  lazy val getRegistrationRequestSchema = "/schemas/get_registration_request.schema"
  lazy val getRegistrationResponseSchema = "/schemas/get_registration_response.schema"
  lazy val countryCodeSchema = "/schemas/country_code"
  lazy val businessSubscriptionRequestSchema = "/schemas/business_subscription_request.schema"
  lazy val businessSubscriptionResponseSchema = "/schemas/business_subscription_response.schema"
  lazy val propertySubscriptionRequestSchema = "/schemas/property_subscription_request.schema"
  lazy val propertySubscriptionResponseSchema = "/schemas/property_subscription_response.schema"
}
