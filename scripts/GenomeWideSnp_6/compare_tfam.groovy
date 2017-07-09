#!/usr/bin/env groovy

//get and check args
if( !this.args || this.args.length!=2 ){
    println 'Compare tfam files'
    println 'usage: compare_tfam.groovy <tfam1> <tfam2>'
    return 1
}

String tfam1 = this.args[0]
String tfam2 = this.args[1]


def tfamMapList = []

[tfam1,tfam2].each{ tfam->
    def tfamMap = [:] as TreeMap //map: key=familyID value=list of ids
    
    new File(tfam).eachLine{ line->
        def toks = line.split("\\s")

        def fId = toks[0]
        def id = toks[1]
        def list = tfamMap[fId]

        if( list==null ){
            list = []
            tfamMap[fId] = list
        }

        list << id
    }
    
    tfamMapList << tfamMap
}

tfamMapList.eachWithIndex{ tfamMap, i->
    println "tfam file ${i} has ${tfamMap.size()} families"
}

if( tfamMapList[0].size()!=tfamMapList[1].size() ){
    def min = tfamMapList.min{it.size()}
    def max = tfamMapList.max{it.size()}
    
    def missing = max.keySet().findAll{ !(it in min.keySet()) }
    
    println "Removed families:\n${missing}"
}

return 0