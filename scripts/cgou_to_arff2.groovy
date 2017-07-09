#!/usr/bin/env groovy

/**
 * Convert .cgou haplotype files to arff, for trios .cgou files
 */

//// Constants section

def regexCgou = /msCEUFebruary2009_IndependentlyPhasedUsingBeagle_([a-zA-Z]+)(\d+)Size(\d+)Th([\w\.]+)MeamTDT2G\.([a-zA-Z]+)/
def regexFolder = /Size(\d+)_th([\w\.]+)/

def CHRS = (1..22).collect{it as String}.sort()
// training/test set labels
def TEST_LABEL = 'Test'
def TRAIN_LABEL = 'TestForHaploRisk'
// limits
def SIZE_LIMIT = 10
def THR_LIMIT = null

def COMPRESS = true
def THRESHOLDS = ['All'] as Set

// regexCgou matcher field indexes
int RFI_SET = 1
int RFI_CHR = 2
int RFI_SIZE = 3 
int RFI_THR = 4 //threshold
int RFI_EXT = 5 //extension

// cgou values
def CGOU_VALS = ['1','2','?']

// arff attributes
def ATT_VALUES = '1,2'
def CLASS_VALUES = '1,2'
def PHENO_ATT_NAME = 'phenotype'

//gou file fields
def F_PHENO = 5
def F_FAM = 0
def F_SUBJ = 1

def DISEASE = 'MS_IndependentlyPhasedUsingBeagle'

// end of Constants section

//// closures

def writeArff = { patterns, subjects, phenotypes, outFile->
    def writer = new File(outFile).newWriter()
        
    writer.writeLine("@RELATION ${outFile.substring(outFile.lastIndexOf('/')+1)}")
    writer.writeLine('')
    
    //write attributes
    CHRS.each{ chr->
        int len = patterns[chr][0].size()/2
        (1..len).each{ writer.writeLine("@ATTRIBUTE '${chr}:${it}_L' {${ATT_VALUES}}\n@ATTRIBUTE '${chr}:${it}_R' {${ATT_VALUES}}") }
    }
     
    writer.writeLine("@ATTRIBUTE ${PHENO_ATT_NAME} {${CLASS_VALUES}}")
    writer.writeLine('')
    writer.writeLine('@DATA')
    
    subjects.eachWithIndex{ subj, i->
        writer.writeLine('%'+subj)
        CHRS.each{ chr->
            def pattern = patterns[chr][i]
            pattern.each{ writer.write(it+',') }
        }
        writer.writeLine(phenotypes[i] as String)
    }
    
    writer.close()
}//


// end of closures section

//get and check args
if( !this.args || this.args.length!=2 ){
    println 'Convert .cgou haplotype files to arff'
    println 'usage: cgou_to_arff2.groovy <input_dir> <output_dir>'
    return 1
}

String inputdir = this.args[0]
String outdir = this.args[1]

if( !inputdir.endsWith('/') )
    inputdir += '/'

if( !outdir.endsWith('/') )
    outdir += '/'

println "Input dir: ${inputdir}"
println "Output dir: ${outdir}"
println "Size limit: ${SIZE_LIMIT}"
println "Threshold limit: ${THR_LIMIT}"

println "Start time: ${new Date()}"

//mkdir disease output subfolder
"mkdir ${outdir+DISEASE}".execute().waitFor()

//get folders that match regexFolder
def folders = new File(inputdir).list({d, f-> f ==~ regexFolder } as FilenameFilter).toList()
folders = folders.findAll{ (new File(inputdir+it)).isDirectory() }

// process folders
folders.each{ dir->
    def mat = (dir =~ regexFolder)[0]
    def dSize = mat[1] as Integer
    def dThreshold = mat[2]
    
    if( dSize <= SIZE_LIMIT && (dThreshold in THRESHOLDS) ){
        println "Processing folder ${dir}, size=${dSize}, thr=${dThreshold}"
        def dirF = new File(inputdir+dir)
        
        // get .cgou/.map files
        def files = dirF.list( {d, f-> f ==~ regexCgou} as FilenameFilter ).toList()
        def cgouFiles = files.findAll{ mat = (it =~ regexCgou)[0]; (mat[RFI_EXT]=='cgou') }
    
        [TRAIN_LABEL,TEST_LABEL].each{ set->
            def patterns = [:] as TreeMap
            def subjects = []
            def phenotypes = []
            
            CHRS.each{ chr->
                def cgou = cgouFiles.find{mat=(it=~regexCgou)[0]; (mat[RFI_SET]==set && mat[RFI_CHR]==chr)}
                mat = (cgou =~ regexCgou)[0]
                def size = mat[RFI_SIZE]
                def threshold = mat[RFI_THR]
                def ext = mat[RFI_EXT]
                
                // count attributes and store patterns of each subject
                patterns[chr] = []
                
                def reader = new File(inputdir+dir+'/'+cgou).newReader()
                
                reader.eachLine{ line->
                    def toks = line.split("\\s",8)

                    if( set==TRAIN_LABEL && toks[2]!='0' ){
                        // skip children
                    }
                    else{
                        if( chr==CHRS[0] ){
                            // store subjects in the first chromosome reading
                            subjects << toks[F_FAM]+','+toks[F_SUBJ]
                            phenotypes << (toks[2]!='0' ? 2 : 1)//children <-> affected
                        }
                        
                        int cgouLen = ((int)toks[7].length())/2 + 1
                        patterns[chr] << (0..cgouLen-1).collect{(toks[7][it*2]) as Integer}
                    }
                }
            
                reader.close()
                
                //check pattern length
                int len = patterns[chr][0].size()
                
                if( !(patterns[chr].every{it.size()==len}) ) {
                    println "Error: ${set}, pattern lengths don't match."
                }
                
            }//end CHRs
            
            def outFile = outdir + "${DISEASE}/${DISEASE}_size${dSize}_thr${dThreshold}_${set}.arff"
            
            println "Converting .cgou of ${set} to arff:  ${outFile}"
                
            try{
                //write arff
                writeArff(patterns, subjects, phenotypes, outFile)
                
                if( COMPRESS ){
                    "gzip -f ${outFile}".execute().waitFor()
                }
            } catch(e){
                println "Error generating ${outFile}: ${e.message}"
                "rm -f ${outFile}".execute().waitFor()
            }
        }//end sets
    }
}

println "End time: ${new Date()}"

return 0