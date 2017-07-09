#!/usr/bin/env groovy

/**
 * transform nominal {0,1,2} attributes into numeric (simple)
 */

import java.util.zip.GZIPInputStream

def createReader = { file->
    (file.name.endsWith('.gz')) ? 
        new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file)))) : file.newReader()
}//


def arff = this.args[0]


def reader = createReader(new File(arff))
    
reader.eachLine{ line->
    if( line.toUpperCase().startsWith('@ATTRIBUTE') ){
        if( line.contains('{0,1,2}') ){
            line = line.replace('{0,1,2}','NUMERIC')
        }
        
        println line
    }
    else{
        println line
    }
}

reader.close()

return 0