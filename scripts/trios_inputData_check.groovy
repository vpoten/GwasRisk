#!/usr/bin/env groovy

/**
 * check .gou/.ped files
 */

// Constants

def FOLDER_TRAINING = 'training/'
def FOLDER_HOLD = 'holdout/'
def FOLDER_TEST = 'test/'

def regexInputData = /\w+_[a-zA-Z]*(\d+)\w+\.([a-z]+)/
        
def regexTrainHPed = /(\w+_)(\d+)_(\d+)(\.dat\.haped)/ //*_(chr).dat.haped
def regexTestHPed = /(\w+_)(\d+)_(\d+)(\.tes\.haped)/ //*_(chr).tes.haped

def codes = ['A','T','G','C','0','?']

// Configuration
def inputDir = '/home/victor/trios_MS_indepBeagle/'


// Closures
def checkGenotype = { genotype, numSnps ->
    int expected = numSnps*4-1
    
    if(genotype.length()!=expected){
        println "genotype length error: actual ${genotype.length()}/expected ${expected}"
        return false
    }
    
    for(int i=0; i<genotype.length(); i+=4){
        if( !(genotype[i] in codes) || !(genotype[i+2] in codes) ){
            println "genotype coding error: ${genotype[i]} ${genotype[i+2]}"
            return false
        }
    }
    
    return true
}//end checkGenotype

//end closures

[FOLDER_TRAINING, FOLDER_TEST].each{ set->
    println "=====> Processing ${set} dataset."
    
    def dir = inputDir + set
    def inputFiles = new File(dir).list({d, f-> f ==~ regexInputData } as FilenameFilter).toList()
    def mapFiles = inputFiles.findAll{def mat = (it=~regexInputData)[0]; mat[2]=='map'}
    
    mapFiles.each{ mapFile->
        def chr = (mapFile=~regexInputData)[0][1]
        
        println "===>Check files of chr${chr}"
        
        // 1 - read map file and check it
        def reader = new File(inputDir+set+mapFile).newReader()
        int numSnps = 0
        
        reader.splitEachLine("\t"){ toks->
            if( toks[0]!=chr || !(toks[1].startsWith('rs') || toks[1].startsWith('SNP')) ) {
                println "Map file error chr${chr}, line ${numSnps}: ${toks[0]} ${toks[1]} ${toks[2]} ${toks[3]}"
            }
            numSnps++
        }
        
        reader.close()
        
        println "${numSnps} snps read"
        
        // 2 - check .ped/.gou (numSnps per line and codification)
        ['ped','gou'].each{ ext->
            def inFile = inputFiles.find{ def mat = (it=~regexInputData)[0]; (mat[2]==ext && mat[1]==chr)}

            println "=>Check .${ext} file of chr${chr}"

            reader = new File(inputDir+set+inFile).newReader()

            reader.eachLine{ line->
                def toks = line.split("\\s", (ext=='gou' ? 8 : 7))
                
                if( toks.size() != (ext=='gou' ? 8 : 7) ){
                    println "${ext} file error: wrong field count"
                    println line
                }

                checkGenotype(toks[(ext=='gou' ? 7 : 6)], numSnps)
            }

            reader.close()
        }
        
        // 3 - check that .ped and .gou files are identical
        def pedFile = inputFiles.find{ def mat = (it=~regexInputData)[0]; (mat[2]=='ped' && mat[1]==chr)}
        def gouFile = inputFiles.find{ def mat = (it=~regexInputData)[0]; (mat[2]=='gou' && mat[1]==chr)}
        
        println "=>Check ped vs gou file of chr${chr}"
        
        def reader1 = new File(inputDir+set+pedFile).newReader()
        def reader2 = new File(inputDir+set+gouFile).newReader()
        
        def line1 = reader1.readLine()
        def line2 = reader2.readLine()
        
        while( line1!=null && line2!=null ){
            def toks1 = line1.split("\\s", 7)
            def toks2 = line2.split("\\s", 8)
            
            if( toks1[6] != toks2[7] ){
                println "ped vs gou error: different genotypes"
            }
            
            if( (0..3).any{toks1[it]!=toks2[it]} ){
                println "ped vs gou error: different subjects"
            }
    
            line1 = reader1.readLine()
            line2 = reader2.readLine()
        }
        
        if( line1!=null || line2!=null ){
            println "ped vs gou error: different line count"
        }
        
        reader1.close()
        reader2.close()
    }
}


return 0