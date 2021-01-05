package it.unibo.fiware

import khttp.responses.Response
import org.json.JSONObject

val config = Config.init()
val IP = config.ip
val PORT_CONTEXTBROKER = config.portcontextbroker
val PORT_NORTH = config.portnorth
val PORT_HTTP = config.porthttp
val PORT_DUMMYDEVICE = config.portdummydevice

class Fiware {
    companion object {
        /**
         * Show the list of devices
         */
        fun getDevices(service: String, servicepath: String): Response {
            return khttp.get(
                    url = "http://$IP:${PORT_NORTH}/iot/devices",
                    headers = mapOf("fiware-service" to "$service", "fiware-servicepath" to "$servicepath"),
            )
        }

        fun invokeProvision(group: String, service: String, servicepath: String): Response {
            val data = JSONObject()
            val payload = JSONObject()
            payload.put("apikey", group)
            payload.put("cbroker", "http://orion:${PORT_CONTEXTBROKER}")
            payload.put("entity_type", "Thing")
            payload.put("resource", "/iot/json")
            data.append("services", payload)
            return khttp.post(
                    url = "http://$IP:${PORT_NORTH}/iot/services",
                    headers = mapOf(
                            "Content-Type" to "application/json",
                            "fiware-service" to service,
                            "fiware-servicepath" to servicepath,
                    ),
                    data = data.toString()
            )
        }

        fun checkIotAgent(): Response {
            return khttp.get(url = "http://$IP:${PORT_NORTH}/iot/about")
        }
    }
}

interface IThing {
    val id: String
    val service: String
    val servicepath: String
    val type: String
    fun register(): Any
}

class MotionHTTP(override val id: String, override val service: String, override val servicepath: String, override val type: String) : ISensor {
    val shortenedId = id.replace(":", "").toLowerCase()

    override fun register(): Any {
        return khttp.post(
                url = "http://$IP:${PORT_NORTH}/iot/devices",
                headers = mapOf(
                        "Content-Type" to "application/json",
                        "fiware-service" to "$service",
                        "fiware-servicepath" to "$servicepath",
                ),
                data =
                """{
                        "devices": [
                            {
                                "device_id":   "$shortenedId",
                                "entity_name": "urn:ngsi-ld:$id",
                                "entity_type": "$type",
                                "transport": "HTTP",
                                "attributes": [ { "object_id": "c", "name": "count", "type": "Integer" } ],
                                "static_attributes": [{ "name": "refStore", "type": "Relationship", "value": "urn:ngsi-ld:Store:001"}]
                            }
                        ]
                    }""".trimIndent()
        )
    }

    override fun sense(): Any {
        return khttp.get(
                url = "http://$IP:${PORT_CONTEXTBROKER}/v2/entities/urn:ngsi-ld:$id?type=$type",
                headers = mapOf("fiware-service" to "$service", "$servicepath" to "/")
        )
    }

    fun simulate(group: String): Response {
        return khttp.post(
                url = "http://$IP:${PORT_HTTP}/iot/json?k=$group&i=$shortenedId",
                headers = mapOf("Content-Type" to "application/json"),
                data = """{"c": "1"}""".trimIndent()
        )
    }
}

class BellHTTP(override val id: String, override val service: String, override val servicepath: String, override val type: String) : IActuator {
    override fun register(): Any {
        val shortenedId = id.replace(":", "").toLowerCase()
        return khttp.post(
                url = "http://$IP:${PORT_NORTH}/iot/devices",
                headers = mapOf(
                        "Content-Type" to "application/json",
                        "fiware-service" to "$service",
                        "fiware-servicepath" to "$servicepath",
                ),
                data = """{
                        "devices": [
                            {
                                "device_id":   "$shortenedId",
                                "entity_name": "urn:ngsi-ld:$id",
                                "entity_type": "$type",
                                "transport": "HTTP",
                                "endpoint": "http://iot-sensors:${PORT_DUMMYDEVICE}/iot/$shortenedId",
                                "commands": [{ "name": "ring", "type": "command" }],
                                "static_attributes": [{"name": "refStore", "type": "Relationship", "value": "urn:ngsi-ld:Store:001"}]
                            }
                        ]
                }""".trimIndent()
        )
    }

    override fun doSomething(): Any {
        return khttp.post(
                url = "http://$IP:${PORT_CONTEXTBROKER}/v2/entities/urn:ngsi-ld:$id/attrs",
                headers = mapOf(
                        "Content-Type" to "application/json",
                        "fiware-service" to "$service",
                        "fiware-servicepath" to "$servicepath",
                ),
                data =
                """{
                        "ring": {
                            "type" : "command",
                            "value" : ""
                        }
                }""".trimIndent())
    }

    fun checkCommand(): Response {
        return khttp.get(
                url = "http://$IP:${PORT_CONTEXTBROKER}/v2/entities/urn:ngsi-ld:$id?type=$type&options=keyValues",
                headers = mapOf("fiware-service" to service, "fiware-servicepath" to servicepath)
        )
    }
}


interface IThingMQTT : IThing

interface ISensor : IThing {
    fun sense(): Any
    fun senseNumber(): Any = sense().toString().toDouble()
}

interface IMotion : ISensor

interface IActuator : IThing {
    fun doSomething(): Any
}

interface IDoor : IActuator
interface IBell : IActuator