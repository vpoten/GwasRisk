#!/usr/bin/env groovy

/**
 * rename attributes (replace [:,',-] symbols
 */

def symbols = [':','\'','-']

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
            symbols.each{ line=line.replace(it,'_') }
        }
        
        println line
    }
    else{
        println line
    }
}

reader.close()

return 0