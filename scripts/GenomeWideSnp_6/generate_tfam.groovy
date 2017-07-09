#!/usr/bin/env groovy

def regexGenoTxt = /(\w+)\.birdseed-v2.(\w+)\.txt\.gz/

//get and check args
if( !this.args || this.args.length!=2 ){
    println 'Generate actual tfam'
    println 'usage: generate_tfam.groovy <input_dir> <complete tfam>'
    return 1
}

String input = this.args[0]
String tfam = this.args[1]

// 1: get matching files
def genotypeFiles = new File(input).list({d, f-> f ==~ regexGenoTxt } as FilenameFilter).toList()
def subjects = genotypeFiles.collect{ (it=~regexGenoTxt)[0][1] } as TreeSet

// 2: read tfam file
def tfamMap = [:] as TreeMap //map: key=subject value=tfam line
boolean errors = false

new File(tfam).eachLine{ line->
    def toks = line.split("\t")
    
    if( tfamMap.containsKey(toks[1]) ){
        System.err.println("Duplicate subject ${toks[1]}")
        errors = true
    }
    
    tfamMap[toks[1]] = line
}

subjects.each{ //check subjects
    if( !tfamMap.containsKey(it) ){
        System.err.println("Missing ${it} subject in tfam")
        errors = true
    }
}

if( errors ){
    return 1
}

//print tfam lines to stdout
subjects.each{
    println tfamMap[it]
}

return 0