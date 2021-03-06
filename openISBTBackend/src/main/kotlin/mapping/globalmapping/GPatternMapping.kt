package mapping.globalmapping

import patternconfiguration.AbstractPattern
//AbstractPattern like its given in the pattern config: name, sequence, weight, ...
class GPatternMapping(var abstractPattern: AbstractPattern, var gMappingList:ArrayList<GMapping>) {
    //true, if the abstract pattern is supported, i.e. there is at least one global mapping)
    var supported = false
    // number of requests which should be executed
    var requests = 0

    init {
        if (gMappingList.isNotEmpty()) {
            supported = true
        }
    }
}