package it.unibo.example03

import it.unibo.fiware.BellHTTP
import it.unibo.fiware.Fiware
import it.unibo.fiware.MotionHTTP
import khttp.responses.Response
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test

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
            val service = "openiot"
            val servicepath = "/"

            // Check if IoT Agent is running
            var res = Fiware.checkIotAgent()
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
            val group = "4jggokgpepnvsb2uv4s40d59ov"
            res = Fiware.invokeProvision(group, service, servicepath)
            handleRes(res)

            /* In the example the IoT Agent is informed that the /iot/json endpoint will be used and that devices will
             * authenticate themselves by including the token $group. For a JSON IoT Agent this means
             * devices will be sending GET or POST requests to:
             * http://iot-agent:${HTTP_PORT}/iot/json?i=<device_id>&k=$group
             */

            /* Provisioning a Sensor */
            val motion = MotionHTTP("Motion:001", service, servicepath, "Motion")
            res = motion.register() as Response
            handleRes(res)

            /* Simulate a dummy IoT device measurement by making the following request */
            res = motion.simulate(group) as Response
            handleRes(res)

            /* You can see that a measurement has been recorded, by retrieving the entity data from the context broker.
             * Don't forget to add the fiware-service and fiware-service-path headers. */
            res = motion.sense() as Response
            handleRes(res)

            /* Provisioning an actuator is similar to provisioning a sensor. This time an endpoint attribute holds the
             * location where the IoT Agent needs to send the JSON command and the commands array includes a list of
             * each command that can be invoked. */
            val bell = BellHTTP("Bell:001", service, servicepath, "Bell")
            res = bell.register() as Response
            handleRes(res)

            /* The full list of provisioned devices can be obtained by making a GET request to the /iot/devices endpoint. */
            res = Fiware.getDevices(service, servicepath)
            handleRes(res)

            /* To invoke the ring command, the ring attribute must be updated in the context. */
            res = bell.doSomething() as Response
            handleRes(res)

            res = bell.checkCommand()
            handleRes(res)
        } catch (e: Exception) {
            e.printStackTrace()
            fail(e.message)
        }
    }
}