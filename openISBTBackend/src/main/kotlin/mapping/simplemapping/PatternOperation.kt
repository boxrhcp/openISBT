package mapping.simplemapping

import com.google.gson.JsonObject
import patternconfiguration.AbstractOperation
import patternconfiguration.AbstractPatternOperation
//abstractOperation: name, input, output, selector, wait
//abstractPatternOperation: //CREATE; UPDATE; DELETE; ...
class PatternOperation(var abstractOperation: AbstractOperation, var abstractPatternOperation:AbstractPatternOperation) {
    var path:String = "" //Concrete Path which supports this operation
    var requests:Int = 0 //Number of requests to that path
    var parameters:ArrayList<JsonObject> = ArrayList() //List of required parameters
    var headers:ArrayList<Pair<String, JsonObject>> = ArrayList() //List of headers
    var requiredBody:JsonObject = JsonObject() //Schema required in body
    var produces:String ="" //Values which are produced by this operation
}