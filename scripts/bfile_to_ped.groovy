#!/usr/bin/env groovy

/**
 * recode binary ped into multiple ped files (one per chromosome)
 */


def plink = '/home/clados/bioinformaticsSoftware/plink/plink' //plink path
def outBaseName = '/home/clados/bioinformaticsData/dbGaP/tmp/ped/phg000028_IMSGC_filtered_Affy500K_p1_chr'
def bfile = '/home/clados/bioinformaticsData/dbGaP/tmp/merge2'
def options = '--hwe-all --maf 0.05 --hwe 0.05'
def markerFile = '/home/clados/bioinformaticsData/dbGaP/tmp/phg000028.IMSGC.genotype-calls.Affy500K.p1.MULTI.marker-info'

def chrs = (1..23).collect{it}

println "plink: ${plink}"
println "out ${outBaseName}"
println "input bfile ${bfile}"

//first: create a map for ids (affy_id->rs_id)
def affyIdsMap = [:] as TreeMap

new File(markerFile).splitEachLine(","){ toks->
    if( !toks[0].startsWith('#') ){
        affyIdsMap[toks[2]] = toks[4]
    }
}
    
println "read ${affyIdsMap.size()} markers"


chrs.each{
    println "recode bfile for chr ${it}"
    def fileBase = "${outBaseName}${it}"
    
    def comm = "${plink} --noweb --bfile ${bfile} --recode --out ${fileBase} ${options} --chr ${it}"
    
    if( comm.execute().waitFor()!=0 ){
        println "Error recoding bfile for chr ${it}"
    }
    else{
        // change affymetrix ids in .map file
        def writer = new File("${fileBase}.map.tmp").newWriter()
        
        new File("${fileBase}.map").splitEachLine("\t"){ toks->
            def id = affyIdsMap[toks[1]]
            id = (id) ?: toks[1]
            writer.println("${toks[0]}\t${id}\t${toks[2]}\t${toks[3]}")
        }
        
        writer.close()
        
        "mv ${fileBase}.map.tmp ${fileBase}.map".execute().waitFor()
    }
}

return 0