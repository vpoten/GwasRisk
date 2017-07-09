#!/usr/bin/env groovy

/**
 * gets the intersection of SNP attributes from 2 arff files
 */ 

import java.util.zip.GZIPInputStream

def createReader = { file->
    (file.name.endsWith('.gz')) ? 
        new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file)))) : file.newReader()
}//


def arff1 = this.args[0]
def arff2 = this.args[1]

def snps = [arff1,arff2].collect{ file->
    def reader = createReader(new File(file))
    def set = [] as TreeSet
    reader.eachLine{ line->
        if( line.startsWith('@ATTRIBUTE') ){
            def toks = line.split("\\s")
            if( toks[1].contains('SNP') ){
                set << toks[1].substring(1, toks[1].length()-1)
            }
        }
        
    }
    
    reader.close()
    return set
}


//print intersection
snps[0].intersect(snps[1]).each{ println it }

return 0