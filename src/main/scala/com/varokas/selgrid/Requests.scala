package com.varokas.selgrid

import scalaj.http.Http

class Requests(val marathonUrl:String, val marathonUsername:String, val marathonPassword:String) {
  private val credentials = s"$marathonUsername:$marathonPassword";
  private val GROUP_NAME = "selgrid-"
  private val GROUP_PREFIX = s"/$GROUP_NAME"
  private val GROUPS_API = s"$marathonUrl/v2/groups"

  private def dnsNameHub(name:String, dnsSuffix: String) = s"$GROUP_NAME$name.$dnsSuffix";
  private def groupName(name:String) = GROUP_PREFIX + name;
  private def appNameHub(name:String) = groupName(name) + "/hub"
  private def appNameNode(name:String) = groupName(name) + "/node"

  private val HUB_CPU = 0.1
  private val NODE_CPU = 0.4
  private val HUB_MEM = 128
  private val RAM_PER_BROWSER = 128

  def createGrid(name:String, nodes:Int, browsers: Int, dnsSuffix: String): DeploymentResponse = {
    val response = Http(GROUPS_API)
      .auth(marathonUsername, marathonPassword)
      .method("POST")
      .param("force", "true")
      .postData( Mapper.getJSON(createGroupRequest(name, nodes, browsers, dnsSuffix)) )
      .asString

    return Mapper.parseJSON(response.body, Mapper.DEPLOYMENT_RESPONSE_TYPE)
  }

  def deleteGrid(name:String): DeploymentResponse = {
    val response = Http(GROUPS_API + "/" + groupName(name))
      .auth(marathonUsername, marathonPassword)
      .method("DELETE")
      .param("force", "true")
      .asString

    return Mapper.parseJSON(response.body, Mapper.DEPLOYMENT_RESPONSE_TYPE)
  }

  private def createGroupRequest(name:String, nodes:Int, browsers: Int, dnsSuffix: String):Group = {
    return Group(
      id=groupName(name),
      apps=List(
        App(
          id = appNameHub(name),
          cpus = HUB_CPU,
          mem = HUB_MEM,
          instances =  1,
          container = new Container("DOCKER", new Docker("varokas/selenium-hub", "HOST")),
          portDefinitions = List(new PortDefinition(0, "tcp")),
          env = Map(),
          healthChecks = List(
            new HealthCheck("HTTP", "/", 0, 120, 10, 20, 10)
          ),
          labels = Map(
            "HAPROXY_GROUP" -> "external",
            "HAPROXY_0_VHOST" -> dnsNameHub(name, dnsSuffix)
          )
        ),
        App(
          id = appNameNode(name),
          cpus = NODE_CPU,
          mem = RAM_PER_BROWSER * browsers,
          instances =  nodes,
          container = new Container("DOCKER", new Docker("varokas/selenium-node", "HOST")),
          portDefinitions = List(new PortDefinition(0, "tcp")),
          env = Map(
            "MARATHON_URL" -> marathonUrl,
            "MARATHON_CREDENTIALS" -> credentials,
            "HUB_APPID" -> appNameHub(name),
            "MAX_INSTANCES" -> s"$browsers"
          ),
          healthChecks = List(
            new HealthCheck("HTTP", "/wd/hub/static/resource/hub.html", 0, 120, 10, 20, 10)
          ),
          dependencies = List(appNameHub(name))
        )
      )
    )
  }
}
