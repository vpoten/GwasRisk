#!/usr/bin/env groovy

/**
 * Convert .cgou haplotype files to arff
 */

//// Constants section

def regexCgou = /Affx_gt_(\w+)PhasedPhasedFromTrainingForHaploRiskUsingBeagle_([a-zA-Z]+)Size(\d+)Th([\w\.]+)MeamTDT2G\.([a-zA-Z]+)/
def regexFolder = /Size(\d+)_th([\w\.]+)/

// training/test set labels
def TEST_LABEL = 'Test'
def TRAIN_LABEL = 'TestForHaploRisk'
// limits
def SIZE_LIMIT = 5
def THR_LIMIT = null
//def DISEASES = ["BD", "CAD", "HT", "IBD", "RA", "T1D", "T2D"]
def DISEASES = ["BD", "CAD", "HT", "IBD", "RA", "T1D", "T2D"]

def COMPRESS = true

// regexCgou matcher field indexes
int RFI_DISEASE = 1
int RFI_SET = 2
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

// end of Constants section

//// closures

def getMapSnpsAsAttribNames = { mapFile ->
    def atts = []
    new File(mapFile).splitEachLine("\\s"){ toks->
        atts << "${toks[0]}:${toks[1]}"
    }
    return atts
}//

def cgouToArff = { cgouFile, atts, outFile->
    def reader = new File(cgouFile).newReader()
    def writer = new File(outFile).newWriter()
        
    writer.writeLine("@RELATION ${outFile.substring(outFile.lastIndexOf('/')+1)}")
    writer.writeLine('')
    
    //write attributes
    atts.each{ writer.writeLine("@ATTRIBUTE '${it}_L' {${ATT_VALUES}}\n@ATTRIBUTE '${it}_R' {${ATT_VALUES}}") }
     
    writer.writeLine("@ATTRIBUTE ${PHENO_ATT_NAME} {${CLASS_VALUES}}")
    writer.writeLine('')
    writer.writeLine('@DATA')
    
    reader.eachLine{ line->
        def toks = line.split("\\s",8)
        def subjectId = "%${toks[F_FAM]},${toks[F_SUBJ]}"
        writer.writeLine(subjectId)
        
        String haplos = toks[7]
        int count = 0
        
        for(int i=0; i<haplos.length(); i+=4){
            if( !(haplos[i] in CGOU_VALS) || !(haplos[i+2] in CGOU_VALS) ){
                throw new RuntimeException("Bad codification (${subjectId}): ${haplos[i]},${haplos[i+2]}")
            }
            
            writer.write("${haplos[i]},${haplos[i+2]},")
            count++
        }
        
        if( count!=atts.size() ){
            throw new RuntimeException("Different haplotype count (${subjectId}): ${count}, must be ${atts.size()}")
        }
        
        writer.writeLine(toks[F_PHENO])
    }
        
    writer.close()
    reader.close()
}//

def genOutFileName = { cgouMatch->
    def size = cgouMatch[RFI_SIZE]
    def disease = cgouMatch[RFI_DISEASE]
    def set = cgouMatch[RFI_SET]
    def threshold = cgouMatch[RFI_THR]
    
    "${disease}/${disease}_size${size}_thr${threshold}_${set}.arff"
}//

// end of closures section

//get and check args
if( !this.args || this.args.length!=2 ){
    println 'Convert .cgou haplotype files to arff'
    println 'usage: cgou_to_arff.groovy <input_dir> <output_dir>'
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

//get folders that match regexFolder
def folders = new File(inputdir).list({d, f-> f ==~ regexFolder } as FilenameFilter).toList()
folders = folders.findAll{ (new File(inputdir+it)).isDirectory() }

// process folders
folders.each{ dir->
    def mat = (dir =~ regexFolder)[0]
    def dSize = mat[1] as Integer
    def dThreshold = mat[2]
    
    if( dSize <= SIZE_LIMIT ){
        println "Processing folder ${dir}, size=${dSize}, thr=${dThreshold}"
        def dirF = new File(inputdir+dir)
        
        // get .cgou/.map files
        def files = dirF.list({d, f-> f ==~ regexCgou } as FilenameFilter).toList()
        def cgouFiles = files.findAll{ (it =~ regexCgou)[0][RFI_EXT]=='cgou' }
    
        cgouFiles.each{ cgou->
            mat = (cgou =~ regexCgou)[0]
            def size = mat[RFI_SIZE]
            def disease = mat[RFI_DISEASE]
            def set = mat[RFI_SET]
            def threshold = mat[RFI_THR]
            def ext = mat[RFI_EXT]
            
            if( disease in DISEASES ){
                def mapFile = files.find{ 
                    def mat2 = (it =~ regexCgou)[0]
                    (mat2[RFI_EXT]=='map' && mat2[RFI_SIZE]==size && mat2[RFI_DISEASE]==disease &&
                        mat2[RFI_SET]==set && mat2[RFI_THR]==threshold)
                }

                def outFile = outdir + genOutFileName(mat)

                //mkdir disease output subfolder
                "mkdir ${outdir+disease}".execute().waitFor()

                println "Converting ${cgou} and ${mapFile} to arff."
                println "Output arff file ${outFile}."

                try{
                    if( !mapFile ){
                        println "Error: map file for ${cgou} not found"
                    }

                    def atts = getMapSnpsAsAttribNames(dirF.absolutePath+'/'+mapFile)
                    cgouToArff(dirF.absolutePath+'/'+cgou, atts, outFile)

                    if( COMPRESS ){
                        "gzip -f ${outFile}".execute().waitFor()
                    }
                } catch(e){
                    println "Error generating ${outFile}: ${e.message}"
                    "rm -f ${outFile}".execute().waitFor()
                }
            }
        }
    }
}

println "End time: ${new Date()}"

return 0