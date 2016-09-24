package com.varokas.selgrid

import com.fasterxml.jackson.core.`type`.TypeReference
import org.junit.Assert._
import org.junit.Test
import org.scalatest.junit.AssertionsForJUnit
import org.scalatest.{FunSpec, Matchers}

class ModelsTest {
  @Test def testAppPostRequest(): Unit = {
    val req = App(
      id = "/selgrid01/node",
      cpus = 0.4,
      mem = 384,
      disk = 100,
      instances = 1,
      container = new Container("DOCKER", new Docker("varokas/selenium-node", "HOST")),
      portDefinitions = List(new PortDefinition(0, "tcp")),
      env = Map(
        "MARATHON_URL" -> "http://ip:8080",
        "MARATHON_CREDENTIALS" -> "user:pass",
        "HUB_APPID" -> "/selgrid01/hub",
        "MAX_INSTANCES" -> "2"
      ),
      healthChecks = List(
        new HealthCheck("HTTP", "/wd/hub/static/resource/hub.html", 0, 300, 60, 20, 3)
      )
    )

    val is = getClass.getResourceAsStream("/json/app_post_request.json")
    val expected = scala.io.Source.fromInputStream(is).mkString

    assertEquals(expected, Mapper.getIndentedJSON(req))
  }

  @Test def testDeploymentResponse():Unit = {
    val str =
      """
        {
          "deploymentId" : "5ed4c0c5-9ff8-4a6f-a0cd-f57f59a34b43",
          "version" : "2015-09-29T15:59:51.164Z"
        }
      """.stripMargin

    val response:DeploymentResponse = Mapper.parseJSON(str, new TypeReference[DeploymentResponse] {})

    assertEquals("5ed4c0c5-9ff8-4a6f-a0cd-f57f59a34b43", response.deploymentId)
    assertEquals("2015-09-29T15:59:51.164Z", response.version)
  }

  @Test def testDeploymentResponseError() = {
    val str = """ { "message": "Invalid username or password." } """

    val response:DeploymentResponse = Mapper.parseJSON(str, new TypeReference[DeploymentResponse] {})

    assertEquals("Invalid username or password.", response.message)
  }

  @Test def testDeploymentsResponse():Unit = {
    val is = getClass.getResourceAsStream("/json/deployments_response.json")
    val str = scala.io.Source.fromInputStream(is).mkString

    val response:List[Deployment] = Mapper.parseJSON(str, new TypeReference[List[Deployment]] {})

    assertEquals(1, response.size)
    assertEquals("97c136bf-5a28-4821-9d94-480d9fbb01c8", response(0).id)
    assertEquals("2015-09-30T09:09:17.614Z", response(0).version)
  }
}