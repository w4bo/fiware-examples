package it.unibo.example03

interface IThing {
    val id: String
    fun register(): Unit
    fun unregister(): Unit
}

interface IThingHTTP : IThing {
    val url: String
    val params: MutableMap<String, String>
}

interface ISensorHTTP : ISensor, IThingHTTP {
    override fun sense(): Number {
        return khttp.get(url = url, params = params).text.toDouble()
    }
}

interface IActuatorHTTP : IActuator, IThingHTTP {
    override fun doSomething() {
        khttp.post(url = url, params = params)
    }
}


interface IThingMQTT : IThing

interface ISensor : IThing {
    fun sense(): Number
    fun senseAny(): Any = sense()
}

interface IMotion : ISensor

interface IActuator : IThing {
    fun doSomething(): Unit
}

interface IDoor : IActuator
interface IBell : IActuator