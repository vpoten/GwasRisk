#!/usr/bin/env groovy

def CHR_LIST = (1..22).collect{it as String}
def regexPed = /_(\d+)\.(\w+)/ // MS_DBGAP_orig(\d+)Unphased_All\.(\w+)/


//get and check args
if( !this.args || this.args.length!=2 ){
    println 'missing stats for plink missingness tests'
    println 'usage: missing_stats.groovy <input_dir> <output_dir>'
    return 1
}

String input = this.args[0]
String output = this.args[1]

if( !input.endsWith('/') ){ input+='/'}
if( !output.endsWith('/') ){ output+='/'}


def pedFiles = new File(input).list({d, f-> f ==~ regexPed } as FilenameFilter).toList().findAll{ (it=~regexPed)[0][2]=='ped' }
def imissFiles = new File(output).list({d, f-> f ==~ regexPed } as FilenameFilter).toList().findAll{ (it=~regexPed)[0][2]=='imiss' }

// read first .PED to get parents and children
def ped1 = pedFiles.find{ (it=~regexPed)[0][1]=='1' }
def reader = new File(input+ped1).newReader()

def parents = [] as TreeSet
int nchildren = 0

reader.eachLine{ line->
    def toks = line.split("\\s",7)
    def fid = toks[0]
    def iid = toks[1]
    def pid = toks[2]
    def mid = toks[3]
    
    if( pid=='0' && mid=='0' ){
        parents << fid+'-'+iid
    }
    else{
        nchildren++
    }
}

reader.close()

/**
 * splits the given string and returns the tokens which are not spaces/blank
 * characters (' ', '\t', ...)
 */
def getClearTokens = { string->
    (string.split("\\s") as List).findAll{!it.isEmpty()}
}

//print header
println "dataset\tchr\tnparents\tnchildren\tavgMissPar\tavgMissChild"

CHR_LIST.each{ chr->
    def file = imissFiles.find{ (it=~regexPed)[0][1]==chr }
    
    double sumParents = 0.0d
    double sumChild = 0.0d
    
    reader = new File(output+file).newReader()
    reader.readLine()//skip header  
    
    int readPar = 0
    int readChild = 0
    
    reader.eachLine{ line->
        def toks = getClearTokens(line)
        def fid = toks[0]
        def iid = toks[1]
        
        double perc = toks[5] as Double
        
        if( (fid+'-'+iid) in parents){
            sumParents += perc
            readPar++
        }
        else {
            sumChild += perc
            readChild++
        }
    }
    
    reader.close()
    
    assert (readPar==parents.size() && readChild==nchildren)
    
    double avgParents = sumParents/((double)parents.size())
    double avgChild = sumChild/((double)nchildren)
    
    println "${ped1}\t${chr}\t${parents.size()}\t${nchildren}\t${avgParents}\t${avgChild}"
}



return 0