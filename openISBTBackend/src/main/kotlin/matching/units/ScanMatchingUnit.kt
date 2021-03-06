package matching.units

import com.google.gson.GsonBuilder
import de.tuberlin.mcc.openapispecification.OpenAPISPecifcation
import de.tuberlin.mcc.openapispecification.PathItemObject
import de.tuberlin.mcc.openapispecification.SchemaObject
import patternconfiguration.AbstractOperation
import mapping.simplemapping.PatternOperation
import matching.MatchingUnit
import matching.MatchingUtil
import matching.ReferenceResolver
import org.slf4j.LoggerFactory
import patternconfiguration.AbstractPatternOperation

class ScanMatchingUnit : MatchingUnit{

    val log = LoggerFactory.getLogger("ScanMatchingUnit");

    override fun getSupportedOperation(): String {
        return AbstractPatternOperation.SCAN.name;
    }

    override fun match(pathItemObject: PathItemObject, abstractOperation: AbstractOperation, spec: OpenAPISPecifcation, path: String): PatternOperation? {
        if (pathItemObject.get != null) {
            //get could support READ or SCAN
            //If an array of items is returned, SCAN is supported
            var listReturned = false;
            for (response in pathItemObject.get.responses.responses.values) {
                if (response.content != null) {
                    for (content in response.content.values) {
                        if (content.schema != null) {
                            if (content.schema.`$ref` != null) {
                                var schema = ReferenceResolver().resolveReference(content.schema.`$ref`, spec) as SchemaObject
                                if (schema.properties != null) {
                                    for (p in schema.properties.entrySet()) {
                                        //todo: improve list detection
                                        if (path.contains("customers") && p.value.toString().contains("\"type\":\"array\"")) {
                                            listReturned = true;
                                        }
                                    }
                                }

                            }
                            if (content.schema.type == "array") {
                                listReturned = true;
                            }
                        }
                    }
                }
            }
            if (listReturned) {
                val operation = PatternOperation(abstractOperation, AbstractPatternOperation.SCAN)
                operation.path = spec.servers[0].url + path

                //Determine input and output values
                val scanObject = pathItemObject.get
                log.debug("    " + GsonBuilder().create().toJson(scanObject))
                if (scanObject.requestBody != null) {
                    //Operation requires some request body
                    val body = MatchingUtil().parseRequestBody(scanObject.requestBody, spec)
                    if (body != null) {
                        operation.requiredBody = body
                    }
                }
                if (scanObject.parameters != null) {
                    operation.parameters = MatchingUtil().parseParameter(scanObject.parameters, spec)
                    for (headerparam in MatchingUtil().parseHeaderParameter(scanObject.parameters, spec)) {
                        operation.headers.add(Pair(headerparam.get("name").asString, headerparam.getAsJsonObject("schema")))
                    }
                }
                if (scanObject.security != null) {
                    val header = MatchingUtil().parseApiKey(scanObject.security, spec)
                    if (header != null) {
                        operation.headers.add(header)
                    }
                }

                return operation
            }
        }
        return null
    }

}