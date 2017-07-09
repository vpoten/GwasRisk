#!/usr/bin/env groovy

/**
 * traverse a directory recursively and gzip all files (not already gzipped).
 */

import groovy.io.*

def SUFF_GZIPPED = '.gzipped'

String dir = this.args[0]
boolean compress = true

if( this.args.length>1) {
    if( this.args[1].startsWith('un') ){
        compress = false
    }
}

if( !dir.endsWith('/') )
    dir += '/'

println "Input dir: ${dir}"

int countGz = 0
int countSk = 0

println "Start time: ${new Date()}"

(new File(dir)).eachFileRecurse(FileType.FILES){ file->
    if( file.isFile() ){
        if( compress ){
            if( file.name.endsWith('.gz') ){
                "mv ${file.absolutePath} ${file.absolutePath}${SUFF_GZIPPED}".execute().waitFor()
                println "skip ${file.absolutePath}"
                countSk++
            }
            else{
                "gzip ${file.absolutePath}".execute().waitFor()
                println "gzip ${file.absolutePath}"
                countGz++
            }
        }
        else{
            if( file.name.endsWith('.gz') ){
                "gzip -d ${file.absolutePath}".execute().waitFor()
                println "ungzip ${file.absolutePath}"
                countGz++
            }
            else if( file.name.endsWith('.gz'+SUFF_GZIPPED) ){
                "mv ${file.absolutePath} ${file.absolutePath.substring(0,file.absolutePath.length()-SUFF_GZIPPED.length())}".execute().waitFor()
                println "skip ${file.absolutePath}"
                countSk++
            }
        }
    }
    
}

println "${countGz} gzipped and ${countSk} skipped"

println "End time: ${new Date()}"

return 0