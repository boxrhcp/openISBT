package workload

import kotlin.js.Json

class ApiRequest() {
    var path:String = ""
    var parameter: Array<Pair<String, Json>> = arrayOf()
    var headers:Array<Pair<String, String>> = arrayOf()
    var method: String = ""
    var body: Json? = null
    var contentType: String = ""
}
