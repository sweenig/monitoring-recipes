/*******************************************************************************
 *  © 2007-2019 - LogicMonitor, Inc. All rights reserved.
 ******************************************************************************/
import com.santaba.agent.groovyapi.http.HTTP
import groovy.json.JsonSlurper
hostname = hostProps.get("system.hostname")

user = hostProps.get("api.user")
pass = hostProps.get("api.pass")
resourcePath = "/platform/3/storagepool/storagepools"

port = api.port ?: 8080
try {
    httpClient = HTTP.open(hostname, 443)
    headers = ["Authorization": "Basic ${"${user}:${pass}".bytes.encodeBase64().toString()}"]
    url = "https://" + hostname + ":" + port + resourcePath
    getResponse = httpClient.get(url, headers)
    body = httpClient.getResponseBody()
    response_obj = new JsonSlurper().parseText(body)
    println(response_obj)
    return 0
}
catch (Exception e) {println e;return 1}
finally {httpClient.close()}
