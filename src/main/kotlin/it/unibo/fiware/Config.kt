package it.unibo.fiware

import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.Constructor


class Config {
    companion object {
        init {
            init()
        }

        fun init(): Config {
            val inputStream = this.javaClass.classLoader.getResourceAsStream("config.yml")
            return Yaml(Constructor(Config::class.java)).load(inputStream)
        }
    }

    var ip: Any? = null
    var portcontextbroker: Any? = null
    var portnorth: Any? = null
    var porthttp: Any? = null
    var portdummydevice: Any? = null
    var portmosquito: Any? = null
}