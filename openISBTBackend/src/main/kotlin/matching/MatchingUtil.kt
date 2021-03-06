package matching

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import de.tuberlin.mcc.openapispecification.*
import org.slf4j.LoggerFactory

class MatchingUtil {

    val log = LoggerFactory.getLogger("ReferenceResolver")

    fun parseParameter(parameters : Array<ParameterObject>, spec: OpenAPISPecifcation) : ArrayList<JsonObject> {
        var result : ArrayList<JsonObject> = ArrayList()
        for (entry in parameters) {
            if (entry.`in` == "path" || entry.`in`=="query") {
                result.add(ReferenceResolver().resolveReferencesInJsonObject(GsonBuilder().create().toJsonTree(entry).asJsonObject, spec))
            }
        }
        return result
    }

    fun parseHeaderParameter(parameters : Array<ParameterObject>, spec: OpenAPISPecifcation) : ArrayList<JsonObject> {
        var result : ArrayList<JsonObject> = ArrayList()
        for (entry in parameters) {
            if (entry.`in` == "header") {
                result.add(ReferenceResolver().resolveReferencesInJsonObject(GsonBuilder().create().toJsonTree(entry).asJsonObject, spec))
            }
        }
        return result
    }

    fun parseApiKey(securityRequirement :JsonElement, spec: OpenAPISPecifcation) : Pair<String, JsonObject>? {
        if (securityRequirement != null) {
            if (securityRequirement.isJsonArray) {
                var secReq = securityRequirement.asJsonArray.get(0).asJsonObject
                for (name in secReq.entrySet()) {
                    var scheme = spec.components.securitySchemes.get(name.key)
                    if (scheme != null && scheme.name != null) {
                        var first = scheme.name
                        var second = GsonBuilder().create().toJsonTree(scheme)
                        return Pair(first, second.asJsonObject)
                    }
                }
            }
        }
        return null
    }

    fun parseRequestBody(requestBody: RequestBodyObject, spec: OpenAPISPecifcation) : JsonObject? {
        var requestBodyObject = requestBody
        if (requestBodyObject.`$ref` != null) {
            //There is a reference which has to be resolved
            requestBodyObject = ReferenceResolver().resolveReference(requestBodyObject.`$ref`, spec) as RequestBodyObject
        }
        if (requestBodyObject.content != null) {
            var contentObject = requestBodyObject.content
            //Currently, we only support json schemes
            var mediaTypeObject = contentObject.get("application/json")
            if (mediaTypeObject == null) {
                mediaTypeObject = contentObject.get("application/json;charset=UTF-8")
            }
            if (mediaTypeObject == null) {
                mediaTypeObject = contentObject.get("application/x-www-form-urlencoded")
            }
            if (mediaTypeObject == null) {
                mediaTypeObject = contentObject.get("multipart/form-data")
            }
            if (mediaTypeObject != null) {
                log.debug("    MediaTypeObject: " + GsonBuilder().create().toJson(mediaTypeObject))
                var schemaObject: SchemaObject? = null
                if (mediaTypeObject.schema != null) {
                    schemaObject = mediaTypeObject.schema
                    log.debug("    SchemaObjectX: " + GsonBuilder().create().toJson(schemaObject))

                    if (schemaObject.`$ref` != null) {
                        //There is a reference which has to be resolved
                        schemaObject = ReferenceResolver().resolveReference(schemaObject.`$ref`, spec) as SchemaObject
                    }
                }
                if (schemaObject != null) {
                    //Found a schema object
                    if ("object".equals(schemaObject.type.toLowerCase())) {
                        //Resolve References in schema object
                        schemaObject.properties = ReferenceResolver().resolveReferencesInJsonObject(schemaObject.properties, spec);
                        return GsonBuilder().create().toJsonTree(schemaObject).asJsonObject
                    }
                    if ("array".equals(schemaObject.type.toLowerCase())) {
                        //Resolve References in schema object
                        schemaObject.items = ReferenceResolver().resolveReferencesInJsonObject(schemaObject.items, spec);
                        return GsonBuilder().create().toJsonTree(schemaObject).asJsonObject
                    }

                }
            }
        }
        return null
    }


}