#!/usr/bin/env groovy

/**
 * Convert ped files to different arff datasets (using TDT test thresholds).
 * Generates allele patterns with class={1=T, 0=U}
 * 
 */

//// Constants section

def dataDir = '/home/victor/trios_{disease}_DBGAP_orig/{set}/' //for MS data
//def dataDir = '/home/clados/bioinformaticsData/WTCCC1/{disease}/plink_formatQCWei_CC/{set}/'
//def dataDir = '/home/victor/Escritorio/WTCCC1/pedToArff/{disease}/{set}/'
def regexPed = /(\w+)_DBGAP_orig(\d+)PhasedFromTrainingForHaploRiskUsingBeagle_(\w+)\.(\w+)/
def regexTdt = /(\w+)_DBGAP_orig(\d+)PhasedFromTrainingForHaploRiskUsingBeagle_(\w+)\.ped\.tdt/
// (not used) //def regexModel = /(\w+)_DBGAP_orig(\d+)PhasedFromTrainingForHaploRiskUsingBeagle_(\w+)\.ped\.model/
// (not used) //def regexAssoc = /(\w+)_DBGAP_orig(\d+)PhasedFromTrainingForHaploRiskUsingBeagle_(\w+)\.ped\.assoc/
// (not used) //def regexPed = /Affx_gt_(\w+)_ChiamoQCWei_CC_(\d+)(\w+)\.(\w+)/
// (not used) //def regexModel = /Affx_gt_(\w+)_ChiamoQCWei_CC_(\d+)(\w+)\.ped\.model/
// (not used) //def regexAssoc = /Affx_gt_(\w+)_ChiamoQCWei_CC_(\d+)(\w+)\.ped\.assoc/

// training/test set labels
def TEST_LABEL = 'Test'
def TRAIN_LABEL = 'Training'

// limits
def thresholds = [/*0.8d, 0.6d, 0.4d, 0.2d,*/ 0.15d, 0.1d, 0.05d, 0.01d, 1e-3, 1e-4, 1e-5, 1e-6, 1e-7]
//def DISEASES = ["BD", "CAD", "HT", "IBD", "RA", "T1D", "T2D"]
def DISEASES = ["MS"]

def COMPRESS = true

// regexPed matcher field indexes
int RFI_DISEASE = 1
int RFI_CHR = 2 
int RFI_SET = 3
int RFI_EXT = 4 //extension

// ped values
def PED_VALS = ['A','T','G','C','0','?','N']
def MISSING = ['0','?','N']

// arff attributes
def ATT_VALUES = '0,1'
def CLASS_VALUES = '1,2'
def PHENO_ATT_NAME = 'phenotype'

//ped file fields
def F_PHENO = 5
def F_FAM = 0
def F_SUBJ = 1
def F_PAT_ID= 2
def F_MAT_ID = 3

//plink constants
///def PLINK = 'plink'
def PLINK = '/home/victor/plink-1.07-x86_64/plink'
// (not used) //def trendTest =  '--model --hwe-all --maf 0.01 --hwe 0.05' //plink test
// (not used) //def assocTest =  '--assoc --hwe-all --maf 0.01 --hwe 0.05' //plink test
def tdtTest =  '--tdt --hwe-all --maf 0.01 --hwe 0.05' //plink test

//////////////////////////////////////////
// end of Constants section
//////////////////////////////////////////


//// closures


def isChildren = { pheno->
    (pheno['maternal']!='0' && pheno['paternal']!='0')
}//


def genArff = { outFile, chrSnps, mapGeno, phenotypes, type, set ->
    
    def writer = new File(outFile).newWriter()
        
    writer.writeLine("@RELATION ${outFile.substring(outFile.lastIndexOf('/')+1)}")
    writer.writeLine('')
    
    chrSnps.each{ chr, snpMap->
        //write attributes
        snpMap.each{id, data->  writer.writeLine("@ATTRIBUTE '${chr}:${id}' {${ATT_VALUES}}") }
    }
     
    writer.writeLine("@ATTRIBUTE ${PHENO_ATT_NAME} {${CLASS_VALUES}}")
    writer.writeLine('')
    writer.writeLine('@DATA')
    int skipped = 0
    
    phenotypes.eachWithIndex{ pheno, i->
        if( (set==TEST_LABEL) || (set==TRAIN_LABEL && !isChildren(pheno)) ){
            writer.writeLine("%${pheno['id']}")

            [2,1].each{ code->
                int module = code%2

                mapGeno.each{ chr, genotypes->
                    assert (chrSnps[chr].size()*2==(genotypes[i].size())), "Error [${pheno['id']}]: snps and genotype count don't match (chr ${chr} ${chrSnps[chr].size()}*2,${genotypes[i].size()})"

                    genotypes[i].eachWithIndex{ val, j->
                        if(j%2==module){ writer.write("${(val==null) ? '?' : val},")  }
                    }
                }

                if( isChildren(pheno) ){
                    writer.writeLine('2') // always T in children
                }
                else{ writer.writeLine("${code}") }// T/U
            }
        }
        else{
            println "skipping children ${pheno['id']}, ${pheno['maternal']}, ${pheno['paternal']}"
            skipped++
        }
    }
    
    println "${skipped} children skipped"
    writer.close()
}//


def genOutFileName = { disease, threshold, set, type ->
    "${disease}_size1_thr${threshold}_${set}.${type}.arff"
}//

def plinkTestComm = { test, ped, map, output->
    "${PLINK} --noweb --ped ${ped} --map ${map} ${test} --out ${output}"
}//



/**
 * splits the given string and returns the tokens which are not spaces/blank
 * characters (' ', '\t', ...)
 */
def getClearTokens = { string ->
    (string.split("\\s") as List).findAll{!it.isEmpty()}
}//


/**
 * get SNPs with pval < cutPval
 * 
 * @param input : input directory where to find .dat.tdt files
 * @param cutPval
 * @return
 */
def getSNP = { input, cutPval ->
    
    def tdtFiles = new File(input).list({d, f-> f ==~ regexTdt } as FilenameFilter).toList()
    def mapSNP = [:] as TreeMap
    
    tdtFiles.each{ file->
        def chr = (file =~ regexTdt)[0][RFI_CHR]
        def currMap = [:] as TreeMap
        mapSNP[chr] = currMap

        def reader = new File(input+file).newReader()

        //get header and field indexes
        def header = reader.readLine().trim()
        def fields = getClearTokens(header)
        int snpIdx = fields.findIndexOf{it=='SNP'}
        int a1Idx = fields.findIndexOf{it=='A1'}
        int a2Idx = fields.findIndexOf{it=='A2'}
        int orIdx = fields.findIndexOf{it=='OR'}
        int pIdx = fields.findIndexOf{it=='P'}

        reader.eachLine{ line ->
            def toks = getClearTokens( line.trim() )
            
            try{
                double pval = toks[pIdx] as Double
                
                if( pval<cutPval ){
                    def snpData =[:]
                    currMap[toks[snpIdx]] = snpData
                    
                    snpData['alleles'] = (toks[a1Idx]+toks[a2Idx])
                
                    try{
                        double or = toks[orIdx] as Double
                        if( or<1.0 ){
                            or = 1.0/or
                            snpData['alleles'] = (toks[a2Idx]+toks[a1Idx])
                        }
                        snpData['or'] = or
                        snpData['logOr'] = Math.log(or)
                    } catch(NumberFormatException e){
                        snpData['logOr'] = 0.0d
                    }
                }
            } catch(NumberFormatException e){}
        }

        reader.close()
        
        println "${currMap.size()} SNPs selected at chr ${chr}"
    }
    
    return mapSNP
}//


//
def encodeAllele = { a1, snpData->
    def alleles = snpData['alleles']
    
    if( (a1 in MISSING) )
        return null
        
    //compare to risk allele (maf)
    return (a1==alleles[0]) ? 1 : 0
}//


/**
 * @return a List with snp data sorted by index
 */
def getMapIndices = { trainDir, chr, snpMap->
    def files = new File(trainDir).list({d, f-> f ==~ regexPed } as FilenameFilter).toList()
    def file = files.find{ (it=~regexPed)[0][RFI_CHR]==chr && (it=~regexPed)[0][RFI_EXT]=='map' }
    def reader = new File(trainDir+file).newReader()
    int cont = 0
    def indices = []
    
    reader.splitEachLine("\\s"){ toks->
       def snpData = snpMap[toks[1]]
       if( snpData!=null ){ 
           snpData['index'] = cont
           indices << snpData
       }
       cont++
    }
    
    reader.close()
    
    return indices
}//

/**
 *
 * @return a list of genotypes, each genotype is a list of Integer values {0,1 or null}.
 * There are two values for each SNP (T,U)
 */
def getGenotypes = { dir, chr, indices, phenotypes->
    def files = new File(dir).list({d, f-> f ==~ regexPed } as FilenameFilter).toList()
    def file = files.find{ (it=~regexPed)[0][RFI_CHR]==chr && (it=~regexPed)[0][RFI_EXT]=='ped' }
    
    def reader = new File(dir+file).newReader()
    
    def genotypes = []
    
    reader.eachLine{ line->
        def toks = line.split("\\s",7)
        def subjData = []
        
        
        indices.each{ snpData->
            int idx = snpData['index']
            def a1 = toks[6][idx*4]
            def a2 = toks[6][idx*4+2]
            
            subjData << encodeAllele(a1, snpData)
            subjData << encodeAllele(a2, snpData)
        }
        
        genotypes << subjData
    }
    
    reader.close()
    
    return genotypes
}//


/**
 *
 */
def getPhenotypes = { dir->
    def files = new File(dir).list({d, f-> f ==~ regexPed } as FilenameFilter).toList()
    def file = files.find{ (it=~regexPed)[0][RFI_EXT]=='ped' }
    
    def reader = new File(dir+file).newReader()
    
    def phenotypes = []
    
    reader.eachLine{ line->
        def toks = line.split("\\s",7)
        def subjData = ['id':(toks[F_FAM]+':'+toks[F_SUBJ]), 'value':toks[F_PHENO],
            'paternal':toks[F_PAT_ID], 'maternal':toks[F_MAT_ID] ]
        phenotypes << subjData
    }
    
    reader.close()
    
    return phenotypes
}

////////////////////////////////////////////////
//// end of closures section
////////////////////////////////////////////////


//get and check args
if( !this.args || this.args.length!=1 ){
    println 'Convert .ped files to arff datasets (using TDT test thresholds).'
    println 'usage: ped_to_arff2.groovy <output_dir>'
    return 1
}

String outdir = this.args[0]

if( !outdir.endsWith('/') )
    outdir += '/'

println "Input dir: ${dataDir}"
println "Output dir: ${outdir}"

println "Start time: ${new Date()}"


DISEASES.each{ dis->
    println "\n===== Processing disease ${dis}\n"
    
    def trainDir = dataDir.replace('{disease}',dis).replace('{set}',TRAIN_LABEL)
    def files = new File(trainDir).list({d, f-> f ==~ regexPed } as FilenameFilter).toList()
    
    //mkdir disease output subfolder
    "mkdir ${outdir+dis}".execute().waitFor()
    
    def pedFiles = files.findAll{ (it=~regexPed)[0][RFI_EXT]=='ped' }
    def mapFiles = files.findAll{ (it=~regexPed)[0][RFI_EXT]=='map' }
    
    def disOutput = outdir+dis+'/' //disease output dir

    // 1 - perform plink TDT tests (--tdt)
    pedFiles.each{ ped->
        def chr = (ped=~regexPed)[0][RFI_CHR]
        def map = mapFiles.find{ (it=~regexPed)[0][RFI_CHR]==chr }
        def output = disOutput+ped
        def tests = [ 'tdt':plinkTestComm( tdtTest, trainDir+ped, trainDir+map, output ) ]
        
        tests.each{name, comm->
            println "Performing plink ${name} test for: ${ped}"
        
            if( comm.execute().waitFor()!=0 ){
                println "Error performing plink ${name} test for: ${ped}"
            }
        }
    }
    
    def phenotypes = [:] // get phenotypes
    [(TRAIN_LABEL), (TEST_LABEL)].each{ set->
        def dir = dataDir.replace('{disease}',dis).replace('{set}', set)
        phenotypes[set] = getPhenotypes(dir) 
    }
    
    // 2 - for each threshold get selected SNPs and generate arff files
    thresholds.each{ thr-> 
        println "*** generating data for threshold ${thr}:"
        def chrSnps = getSNP(disOutput, thr)
        
        int numSnps = chrSnps.values().sum{ it.size() }
        println "${numSnps} selected."
        
        if( numSnps>0 ){
            def genotypes = [(TRAIN_LABEL):[:] as TreeMap, (TEST_LABEL):[:] as TreeMap]

            chrSnps.each{ chr, snpMap->
                //get snp indices
                def indices = getMapIndices(trainDir, chr, snpMap)
                assert (indices.size()==snpMap.size())

                //get genotypes for training and test set
                genotypes.each{ set, map->
                    def dir = dataDir.replace('{disease}',dis).replace('{set}', set)
                    map[chr] = getGenotypes(dir, chr, indices, phenotypes[set])
                    assert (map[chr].size()==phenotypes[set].size())
                }
            }
            
            //write arff files
            genotypes.each{ set, map->
                ['allele'].each{ type->
                    def outFile = disOutput+genOutFileName(dis, thr, set, type)
                    println "generating arff file: ${outFile}"
                    genArff(outFile, chrSnps, map, phenotypes[set], type, set)

                    if( COMPRESS ){
                        "gzip -f ${outFile}".execute().waitFor()
                    }
                }
            }
        }
    }
}

println "End time: ${new Date()}"

return 0