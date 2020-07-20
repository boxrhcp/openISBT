package workload

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import kotlinx.coroutines.runBlocking
import patternconfiguration.AbstractPattern
import mapping.simplemapping.PatternOperation
import org.slf4j.LoggerFactory
import patternconfiguration.AbstractPatternOperation

val log = LoggerFactory.getLogger("PatternRequest")!!

class PatternRequest(var id: Int, var resource: String, var abstractPattern: AbstractPattern) {

    var apiRequests: Array<ApiRequest> = arrayOf()

    fun generateApiRequests(operationSequence: List<List<PatternOperation>>) = runBlocking {

        val requestList = ArrayList<ApiRequest>()

        for (operationList in operationSequence) {
            //pick one of the possible operations (e.g., getUserByID or getUserByName)
            var idx = (0 until operationList.size).shuffled().first()
            var operation = operationList[idx]
            //pick next operation if all required requests are already generated
            while (operation.requests <= 0) {
                idx = (0 until operationList.size).shuffled().first()
                operation = operationList[idx]
            }

            log.debug("Generate request for operation " + operation.abstractOperation.operation + " (" + operation.path + ")...")

            val req = ApiRequest()
            req.path = operation.path
            log.debug("Path is " + operation.path)

            //Fill req method
            when (operation.abstractPatternOperation) {

                AbstractPatternOperation.READ -> req.method = "GET"
                AbstractPatternOperation.SCAN -> req.method = "GET"
                AbstractPatternOperation.CREATE -> req.method = "POST"
                AbstractPatternOperation.UPDATE -> req.method = "PUT"
                AbstractPatternOperation.PATCH -> req.method = "PATCH"
                AbstractPatternOperation.DELETE -> req.method = "DELETE"
                else -> {
                    req.method = "undefined"
                    log.error("Method for operation " + operation.abstractOperation.operation + " is undefined.")
                }
            }
            log.debug("Method is " + req.method)

            //Fill parameter
            log.debug("Parameter:")
            val parameterList = ArrayList<Pair<String, String>>()
            for (p in operation.parameters) {
                val schema = p.get("schema").toString()
                log.trace("Fill parameter " + p.get("name").toString() + " with " + schema)
                var value: String = ""
                value = SchemaFaker.fakeSchema(schema)

                log.trace("Faked schema: $value")
                if (value.startsWith("[")) {
                    log.warn("VALUE ARRAY!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
                    value = value.substring(2, value.length - 2)
                    log.debug("Minimized: $value")
                    var tmp = ""
                    for (e in value.split("\",\"")) {
                        log.debug("One value: $e")
                        tmp = "$tmp$e,"
                    }
                    tmp = tmp.substring(0, tmp.length - 1)
                    log.debug("Result: $tmp")
                    value = GsonBuilder().create().toJson("\"" + tmp + "\"")
                }
                parameterList.add(Pair(p.get("name").asString, value))
                log.debug(p.get("name").asString + "=" + value)
            }
            req.parameter = parameterList.toTypedArray()

            //Fill header
            if (operation.headers.size > 0) {
                log.debug("Fill header...")
                val headerList = ArrayList<Pair<String, String>>()
                for (p in operation.headers) {
                    val schema = p.second.toString()
                    log.trace("Header schema: $schema")
                    var fakeValue = ""
                    fakeValue = SchemaFaker.fakeSchema(schema)

                    log.debug("Added header: $fakeValue")
                    headerList.add(Pair(p.first, fakeValue))
                }
                req.headers = headerList.toTypedArray()
            }

            //Fill Body
            val body: String = operation.requiredBody.toString()
            log.trace("Body schema: $body")
            var v = ""

            v = SchemaFaker.fakeSchema(body)

            log.debug("Added body: $v")
            req.body = JsonParser().parse(v)
            requestList.add(req)
            operation.requests--
        }
        apiRequests = requestList.toTypedArray()
    }
}
