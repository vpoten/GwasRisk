#!/usr/bin/env groovy

/**
 * check .cgou vs .arff files 
 */

// Constants

def FOLDER_TRAINING = 'training/'
def FOLDER_HOLD = 'holdout/'
def FOLDER_TEST = 'test/'

def CHRS = (1..22).collect{it as String}.sort()


// Configuration
def arffTpl = 'haplotypes_phased_pv0_{size}.{set}.arff'
def cgouTpl = 'msCEUFebruary2009_IndependentlyPhasedUsingBeagle_{set}{chr}Size{size}ThAllMeamTDT2G.cgou'
def cgouSets = ['TestForHaploRisk','Test']
def arffSets = ['dat','tes']


def sizes = ['1', '2', '5', '10', '20']
def inputDir = '/home/clados/GeneRisk/MS_IndependentlyPhasedUsingBeagle/results/Size{size}_thAll/'
def outputDir = '/home/victor/out_trios_MS_indepBeagle/size_{size}/'


// TODO

sizes.each{ size->
    println "=====> Size ${size}"
    
    ['Training','Test'].eachWithIndex{ set, idx->
        println "===> Set ${set}"
        
        def arffFile = arffTpl.replace('{size}',size).replace('{set}',arffSets[idx])
        def cgouFile = cgouTpl.replace('{size}',size).replace('{set}',cgouSets[idx])
        
        def inputPath = inputDir.replace('{size}',size)
        def outputPath = outputDir.replace('{size}',size)
        
        
        CHRS.each{ chr->
             println "=> Chr ${chr}"
             
            // read chr attributes and extract them from arff patterns
            def reader = new File(outputPath+arffFile).newReader()
            boolean patternSect = false
            int start = -1
            int end = 0
            int attIdx = 0
            
            def arffPatterns = []
            
            reader.eachLine{ line->
                if( patternSect ) {
                    if( !line.startsWith('%') ){
                        // extract chr attributes
                        def chrAttributes = line.substring(start*2, end*2+1)
                        arffPatterns << (chrAttributes.split(',')).collect{ it=='0' ? '1' : '2' }//change codification
                    }
                }
                else if( line.startsWith('@ATTRIBUTE') && !line.contains('phenotype') ) {
                    def toks = line.split("\\s")
                    def attChr = toks[1].substring(1, toks[1].indexOf(':'))
                    if( attChr==chr ) {
                        if( start<0 ){ start = attIdx }//get index for the first chr attribute
                        end = attIdx
                    }
                    attIdx++
                }
                else if( line.startsWith('@DATA')) {
                    patternSect = true
                }

            }
        
            reader.close()
            
            // read .cgou and compare it with arff (skip children in training set)
            reader = new File(inputPath+cgouFile.replace('{chr}',chr)).newReader()
            int pattIdx = 0
            int failed = 0
            
            reader.eachLine{ line->
                def toks = line.split("\\s",8)
                
                if( set=='Training' && toks[2]!='0' ){
                    // skip children
                }
                else{
                    //compare patterns
                    def arff = arffPatterns[pattIdx]
                    int mismatch = 0
                    int cgouLen = ((int)toks[7].length())/2 + 1
                    
                    if( cgouLen!=arff.size() ){
                        println "Error: pattern size doesn't match ${cgouLen}!=${arff.size()}"
                        failed++
                    }
                    else{
                        arff.eachWithIndex{ att, i->
                            if( att!=toks[7][i*2] ){
                                mismatch++
                            }
                        }
                        
                        if(mismatch>0){
                            println "Error: pattern mismatch (${mismatch}/${arff.size()})"
                            failed++
                        }
                    }   
                    
                    pattIdx++
                }
            }
         
            reader.close()
            
            if( failed>0 ){
                println "${failed} of ${arffPatterns.size()} failed"
            }
            
        }//end CHRS
        
    }//end set {training,test}
}


return 0