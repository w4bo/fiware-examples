package it.unibo.example03

import khttp.responses.Response
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test

val config = Config.init()
val IP = config.ip
val PORT_CONTEXTBROKER = config.portcontextbroker
val PORT_NORTH = config.portnorth
val PORT_HTTP = config.porthttp
val PORT_DUMMYDEVICE = config.portdummydevice

class TestExample03 {
    /**
     * Check weather the response is successful.
     * @param res response
     */
    fun handleRes(res: Response) {
        assertTrue(res.statusCode.toString().startsWith("2"), res.statusCode.toString() + " " + res.text)
        println(res.text)
    }

    @Test
    fun main() {
        try {
            // Check if IoT Agent is running
            var res = khttp.get(url = "http://$IP:${PORT_NORTH}/iot/about")
            handleRes(res)

            /* An IoT Agent is a component that lets a group of devices send their data to and be managed from a Context Broker
             * using their own native protocols. Every IoT Agent is defined for a single payload format, although they may be
             * able to use multiple disparate transports for that payload. The IoT Agent acts as a middleware between the IoT
             * devices and the context broker. It therefore needs to be able to create context data entities with unique IDs.
             * Once a service has been provisioned and an unknown device makes a measurement the IoT Agent add this to the
             * context using the supplied <device-id> (unless the device is recognized and can be mapped to a known ID).
             * There is no guarantee that every supplied IoT device <device-id> will always be unique, therefore all
             * provisioning requests to the IoT Agent require two mandatory headers:
             * - fiware-service: entities for a given service can be held in a separate mongoDB database.
             * - fiware-servicepath: used to differentiate between arrays of devices.
             */

            /* Invoking group provision is always the first step in connecting devices since it is always necessary to supply an
             * authentication key with each measurement and the IoT Agent will not initially know which URL the context broker
             * is responding on. */
            val data = JSONObject()
            val payload = JSONObject()
            val group = "4jggokgpepnvsb2uv4s40d59ov" // UUID.randomUUID().toString() //
            payload.put("apikey", group)
            payload.put("cbroker", "http://orion:${PORT_CONTEXTBROKER}")
            payload.put("entity_type", "Thing")
            payload.put("resource", "/iot/json")
            data.append("services", payload)
            res = khttp.post(
                    url = "http://$IP:${PORT_NORTH}/iot/services",
                    headers = mapOf(
                            "Content-Type" to "application/json",
                            "fiware-service" to "openiot",
                            "fiware-servicepath" to "/",
                    ),
                    data = data.toString()
            )
            handleRes(res)

            /* In the example the IoT Agent is informed that the /iot/json endpoint will be used and that devices will
             * authenticate themselves by including the token $group. For a JSON IoT Agent this means
             * devices will be sending GET or POST requests to:
             * http://iot-agent:${HTTP_PORT}/iot/json?i=<device_id>&k=$group
             */

            /* Provisioning a Sensor */
            res = khttp.post(
                    url = "http://$IP:${PORT_NORTH}/iot/devices",
                    headers = mapOf(
                            "Content-Type" to "application/json",
                            "fiware-service" to "openiot",
                            "fiware-servicepath" to "/",
                    ),
                    data =
                    """{
                        "devices": [
                            {
                                "device_id":   "motion001",
                                "entity_name": "urn:ngsi-ld:Motion:001",
                                "entity_type": "Motion",
                                "transport": "HTTP",
                                "attributes": [ { "object_id": "c", "name": "count", "type": "Integer" } ],
                                "static_attributes": [{ "name": "refStore", "type": "Relationship", "value": "urn:ngsi-ld:Store:001"}]
                            }
                        ]
                    }""".trimIndent()
            )
            handleRes(res)

            /* Simulate a dummy IoT device measurement by making the following request */
            res = khttp.post(
                    url = "http://$IP:${PORT_HTTP}/iot/json?k=$group&i=motion001",
                    headers = mapOf("Content-Type" to "application/json"),
                    data = """{"c": "1"}""".trimIndent()
            )
            handleRes(res)

            /* You can see that a measurement has been recorded, by retrieving the entity data from the context broker.
             * Don't forget to add the fiware-service and fiware-service-path headers. */
            res = khttp.get(
                    url = "http://$IP:${PORT_CONTEXTBROKER}/v2/entities/urn:ngsi-ld:Motion:001?type=Motion",
                    headers = mapOf("fiware-service" to "openiot", "fiware-servicepath" to "/")
            )
            handleRes(res)

            /* Provisioning an actuator is similar to provisioning a sensor. This time an endpoint attribute holds the
             * location where the IoT Agent needs to send the JSON command and the commands array includes a list of
             * each command that can be invoked. */
            res = khttp.post(
                    url = "http://$IP:${PORT_NORTH}/iot/devices",
                    headers = mapOf(
                            "Content-Type" to "application/json",
                            "fiware-service" to "openiot",
                            "fiware-servicepath" to "/",
                    ),
                    data =
                    """{
                        "devices": [
                            {
                                "device_id":   "bell001",
                                "entity_name": "urn:ngsi-ld:Bell:001",
                                "entity_type": "Bell",
                                "transport": "HTTP",
                                "endpoint": "http://iot-sensors:${PORT_DUMMYDEVICE}/iot/bell001",
                                "commands": [{ "name": "ring", "type": "command" }],
                                "static_attributes": [{"name": "refStore", "type": "Relationship", "value": "urn:ngsi-ld:Store:001"}]
                            }
                        ]
                    }""".trimIndent()
            )
            handleRes(res)

            /* The full list of provisioned devices can be obtained by making a GET request to the /iot/devices endpoint. */
            res = khttp.get(
                    url = "http://$IP:${PORT_NORTH}/iot/devices",
                    headers = mapOf("fiware-service" to "openiot", "fiware-servicepath" to "/"),
            )
            handleRes(res)

            /* Before we wire-up the context broker, we can test that a command can be send to a device by making a REST request
             * directly to the IoT Agent's North Port using the /v2/op/update endpoint. It is this endpoint that will eventually
             * be invoked by the context broker once we have connected it up. To test the configuration you can run the
             * command directly as shown below. */
            //  res = khttp.post(
            //          url = "http://$IP:${PORT_NORTH}/v2/op/update",
            //          headers = mapOf(
            //                  "Content-Type" to "application/json",
            //                  "fiware-service" to "openiot",
            //                  "fiware-servicepath" to "/",
            //          ),
            //          data =
            //          """{
            //              "actionType": "update",
            //              "entities": [
            //                  {
            //                      "type": "Bell",
            //                      "id": "urn:ngsi-ld:Bell:001",
            //                      "ring" : {
            //                          "type": "command",
            //                          "value": ""
            //                      }
            //                  }
            //              ]
            //          }""".trimIndent()
            //  )
            //  handleRes(res)

            /* To invoke the ring command, the ring attribute must be updated in the context. */
            res = khttp.post(
                    url = "http://$IP:${PORT_CONTEXTBROKER}/v2/entities/urn:ngsi-ld:Bell:001/attrs",
                    headers = mapOf(
                            "Content-Type" to "application/json",
                            "fiware-service" to "openiot",
                            "fiware-servicepath" to "/",
                    ),
                    data =
                    """{
                        "ring": {
                            "type" : "command",
                            "value" : ""
                        }
                    }""".trimIndent())
            handleRes(res)

            res = khttp.get(
                    url = "http://$IP:${PORT_CONTEXTBROKER}/v2/entities/urn:ngsi-ld:Bell:001?type=Bell&options=keyValues",
                    headers = mapOf("fiware-service" to "openiot", "fiware-servicepath" to "/")
            )
            handleRes(res)
        } catch (e: Exception) {
            e.printStackTrace()
            fail(e.message)
        }
    }
}