package workload

import dataobjects.PatternOperation
import kotlin.js.Json

//@JsModule("")
//external class jsf() {
//    fun generate(schema:String) : String
//}

//@file:JsModule("json-schema-faker")
//package ext.jspackage.name
//external fun foo()

//@JsModule("json-schema-faker")
//@JsNonModule
//@JsName("JSONSchemaFaker")
//external fun generate(schema : Json) : Json

//@JsModule("fakewrapper")
//@JsNonModule
//@JsName("FakeWrapper")
//external fun sayHello(name: String)

@JsModule("fakewrapper")
@JsNonModule
external fun fakeSchema(schema: Json) : Json

class PatternRequest(var id: Int, var abstractPatternName: String) {

    var apiRequests : ArrayList<ApiRequest> = ArrayList()

    fun generateApiRequests(operationSequence: Array<Array<PatternOperation>>) {

        for (operationList in operationSequence) {
            val idx = (0 .. operationList.size-1).shuffled().first()
            var operation = operationList.get(idx)

            var req = ApiRequest()
            req.path = operation.path
            when (operation.aPatternOperation) {

                "READ" -> req.method = "GET"
                "SCAN" -> req.method = "GET"
                "CREATE" -> req.method = "POST"
                "UPDATE" -> req.method = "PUT"
                "DELETE" -> req.method = "DELETE"
                else -> {
                    req.method = "undefined"
                }
            }

            //Call json-schema-faker lib
            req.parameter.clear()
            for (p in operation.parameters) {
                //println("PARAMETER: " +  JSON.stringify(p))
                //println("SCHEMA: " + JSON.stringify(p.schema))

                var value =  fakeSchema(p.schema)
                //println("VALUE: " + value)
                req.parameter.add(Pair(p.name, value))
                //println("Added " + p.name + " " + value)
            }
            if (operation.requiredBody != null) {
                //println("Body Schema: " + JSON.stringify(operation.requiredBody))
                var v = fakeSchema( operation.requiredBody)
                //println("Body Value: " + JSON.stringify(v))
                req.body = v
            }
            apiRequests.add(req)
        }
    }
}
