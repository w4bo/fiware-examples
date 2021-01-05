package it.unibo.example03

import org.yaml.snakeyaml.Yaml


class Config {
    companion object {
        init {
            init()
        }

        fun init(): Config {
            val inputStream = this.javaClass.classLoader.getResourceAsStream("config.yml")
            return Yaml().load(inputStream)
        }
    }

    var ip: Any? = null
    var portcontextbroker: Any? = null
    var portnorth: Any? = null
    var porthttp: Any? = null
}