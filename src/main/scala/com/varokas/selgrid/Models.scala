package com.varokas.selgrid

import java.io.StringWriter

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import scala.collection.immutable.HashMap

object Mapper {
  private val writer = new StringWriter

  val DEPLOYMENT_RESPONSE_TYPE = new TypeReference[DeploymentResponse] {}

  private val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)
  mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  def getJSON(request: MarathonRequest): String = {
    mapper.writeValue(writer, request)
    return writer.toString()
  }

  def getIndentedJSON(request: MarathonRequest): String = {
    return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(request)
  }

  def parseJSON[T](json: String, typeRef: TypeReference[T]): T = {
    return mapper.readValue(json, typeRef)
  }
}

trait MarathonRequest
trait MarathonResponse

case class Group(
  val id:String,
  val apps:List[App] = List(),
  var groups:List[Group] = List()
) extends MarathonRequest

case class App(
  val id:String,
  val cmd:String = null,
  val cpus:Double = 1.0,
  val mem:Int = 32,
  val disk:Int = 0,
  val instances:Int = 1,
  val container:Container = null,
  val portDefinitions:List[PortDefinition],
  val env:Map[String, String] = new HashMap(),
  val healthChecks:List[HealthCheck] = List(),
  val dependencies:List[String] = List()
)
extends MarathonRequest

case class Container(
  @JsonProperty("type") val containerType:String,
  val docker: Docker
)

case class Docker(
  val image: String,
  val network: String
)

case class PortDefinition(
  val port: Int,
  val protocol: String,
  val name: String = null,
  val labels: String = null
)

case class HealthCheck(
  val protocol: String,
  val path: String,
  val portIndex: Int = 0,
  val gracePeriodSeconds: Int,
  val intervalSeconds: Int,
  val timeoutSeconds: Int,
  val maxConsecutiveFailures: Int
)

case class DeploymentsResponse(
  deploymentId:String = null
) extends MarathonResponse {
}

case class Deployment(
  id:String,
  version:String
)

case class DeploymentResponse(
  deploymentId:String = null,
  version:String = null,
  message:String = null,
  details:List[Detail] = null
) extends MarathonResponse {
  def isError() = (message != null)
}

case class Detail(
  path: String,
  errors: List[String]
)