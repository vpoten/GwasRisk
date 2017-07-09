#!/usr/bin/env groovy

/**
 * Convert ped files to different arff datasets (using trend test thresholds)
 */

//// Constants section

//def dataDir = '/home/clados/bioinformaticsData/WTCCC1/{disease}/plink_formatQCWei_CC/{set}/'
def dataDir = '/home/victor/Escritorio/WTCCC1/pedToArff/{disease}/{set}/'
def regexPed = /Affx_gt_(\w+)_ChiamoQCWei_CC_(\d+)(\w+)\.(\w+)/
def regexModel = /Affx_gt_(\w+)_ChiamoQCWei_CC_(\d+)(\w+)\.ped\.model/
def regexAssoc = /Affx_gt_(\w+)_ChiamoQCWei_CC_(\d+)(\w+)\.ped\.assoc/

// training/test set labels
def TEST_LABEL = 'Test'
def TRAIN_LABEL = 'Training'

// limits
def thresholds = [/*0.8d, 0.6d, 0.4d, 0.2d,*/ 0.15d, 0.1d, 0.05d, 0.01d, 1e-3, 1e-4, 1e-5, 1e-6, 1e-7]
//def DISEASES = ["BD", "CAD", "HT", "IBD", "RA", "T1D", "T2D"]
def DISEASES = ["BD", "CAD", "HT", "IBD", "RA", "T1D", "T2D"]
//def DISEASES = ["T1D"]

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
def ATT_VALUES = '0,1,2'
def CLASS_VALUES = '1,2'
def PHENO_ATT_NAME = 'phenotype'

//ped file fields
def F_PHENO = 5
def F_FAM = 0
def F_SUBJ = 1

//plink constants
def PLINK = 'plink'
//def PLINK = '/home/victor/plink-1.07-x86_64/plink'
def trendTest =  '--model --hwe-all --maf 0.01 --hwe 0.05' //plink test
def assocTest =  '--assoc --hwe-all --maf 0.01 --hwe 0.05' //plink test

//////////////////////////////////////////
// end of Constants section
//////////////////////////////////////////


//// closures

def genArffGRS = { outFile, phenotypes, scoreLbl ->
    def writer = new File(outFile).newWriter()

    writer.writeLine("@RELATION ${outFile.substring(outFile.lastIndexOf('/')+1)}")
    writer.writeLine('')
    writer.writeLine("@ATTRIBUTE wGRS NUMERIC")
    writer.writeLine("@ATTRIBUTE ${PHENO_ATT_NAME} {${CLASS_VALUES}}")
    writer.writeLine('')
    writer.writeLine('@DATA')
    
    phenotypes.eachWithIndex{ pheno, i->
        writer.writeLine("%${pheno['id']}")
        writer.writeLine("${pheno[scoreLbl]},${pheno['value']}")
    }

    writer.close()
}//

def genArff = { outFile, chrSnps, mapGeno, phenotypes, type ->
    if( type!='rs' ){
        return genArffGRS(outFile, phenotypes, type)
    }
    
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
    
    phenotypes.eachWithIndex{ pheno, i->
        writer.writeLine("%${pheno['id']}")
    
        mapGeno.each{ chr, genotypes->
            assert (chrSnps[chr].size()==genotypes[i].size())
            genotypes[i].each{ writer.write("${(it==null) ? '?' : it},") }
        }
        
        writer.writeLine(pheno['value'])
    }
    
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
 * @param input : input directory where to find .dat.model files
 * @param cutPval
 * @return
 */
def getSNP = { input, cutPval ->
    def filterReg = /.+\sTREND\s.+/ //use lines of trend test
    def mapSNP = [:] as TreeMap

    def modelFiles = new File(input).list({d, f-> f ==~ regexModel } as FilenameFilter).toList()

    modelFiles.each{ file->
        def chr = (file =~ regexModel)[0][RFI_CHR]
        def currMap = [:] as TreeMap
        mapSNP[chr] = currMap

        def reader = new File(input+file).newReader()

        //get header and field indexes
        def header = reader.readLine().trim()
        def fields = getClearTokens(header)
        int snpIdx = fields.findIndexOf{it=='SNP'}
        int pIdx = fields.findIndexOf{it=='P'}

        reader.eachLine{ line ->
            if( line ==~ filterReg ){
                def toks = getClearTokens( line.trim() )
                try{
                    double pval = toks[pIdx] as Double
                    if( pval<cutPval )
                        currMap[toks[snpIdx]] = [:]
                } catch(NumberFormatException e){}
            }
        }

        reader.close()
    }
    
    def freqFiles = new File(input).list({d, f-> f ==~ regexAssoc } as FilenameFilter).toList()

    freqFiles.each{ file->
        def chr = (file =~ regexAssoc)[0][RFI_CHR]
        def currMap = mapSNP[chr]

        def reader = new File(input+file).newReader()

        //get header and field indexes
        def header = reader.readLine().trim()
        def fields = getClearTokens(header)
        int snpIdx = fields.findIndexOf{it=='SNP'}
        int a1Idx = fields.findIndexOf{it=='A1'}
        int a2Idx = fields.findIndexOf{it=='A2'}
        int orIdx = fields.findIndexOf{it=='OR'}

        reader.eachLine{ line ->
            def toks = getClearTokens( line.trim() )
            def snpData = currMap[toks[snpIdx]]
            if( snpData!=null ){
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
        }

        reader.close()
    }
    
    return mapSNP
}//


//
def encodeAlleles = { a1, a2, snpData->
    def alleles = snpData['alleles']
    
    if( (a1 in MISSING) || (a2 in MISSING) )
        return null
        
    int val = 0

    //compare to risk allele (maf)
    if( a1==alleles[0] ){val += 1}
    if( a2==alleles[0] ){val += 1}

    return val
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
 * @return a list of genotypes, each genotype is a list of Integer values {0,1,2 or null}.
 *  Also updates the GRS and wGRS of each subject in phenotypes list.
 */
def getGenotypes = { dir, chr, indices, phenotypes->
    def files = new File(dir).list({d, f-> f ==~ regexPed } as FilenameFilter).toList()
    def file = files.find{ (it=~regexPed)[0][RFI_CHR]==chr && (it=~regexPed)[0][RFI_EXT]=='ped' }
    
    def reader = new File(dir+file).newReader()
    
    def genotypes = []
    int subIdx = 0
    
    reader.eachLine{ line->
        def toks = line.split("\\s",7)
        def subjData = []
        
        double grs = 0.0
        double wgrs = 0.0
        
        indices.each{ snpData->
            int idx = snpData['index']
            def a1 = toks[6][idx*4]
            def a2 = toks[6][idx*4+2]
            def val = encodeAlleles(a1, a2, snpData)
            
            if( val!=null ){
                grs += val
                wgrs += val*snpData['logOr']
            }
            
            subjData << val
        }
        
        genotypes << subjData
        phenotypes[subIdx]['grs'] = phenotypes[subIdx]['grs']+grs
        phenotypes[subIdx]['wgrs'] = phenotypes[subIdx]['wgrs']+wgrs
        subIdx++
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
            'grs':0.0, 'wgrs':0.0, 'ewgrs':0.0 ]
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
    println 'Convert .ped files to arff datasets (using trend test thresholds).'
    println 'usage: ped_to_arff.groovy <output_dir>'
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

    // 1 - perform plink trend tests (--model)
    pedFiles.each{ ped->
        def chr = (ped=~regexPed)[0][RFI_CHR]
        def map = mapFiles.find{ (it=~regexPed)[0][RFI_CHR]==chr }
        def output = disOutput+ped
        def tests = [
            'model':plinkTestComm( trendTest, trainDir+ped, trainDir+map, output ),
            'assoc':plinkTestComm( assocTest, trainDir+ped, trainDir+map, output ) ]
        
        tests.each{name, comm->
            println "Performing plink ${name} test for: ${ped}"
        
            if( comm.execute().waitFor()!=0 ){
                println "Error performing plink ${name} test for: ${ped}"
            }
        }
    }
    
    // 2 - for each threshold get selected SNPs and generate arff files
    thresholds.each{ thr->
        println "*** generating data for threshold ${thr}:"
        def chrSnps = getSNP(disOutput, thr)
        
        int numSnps = chrSnps.values().sum{ it.size() }
        println "${numSnps} selected."
        
        if( numSnps>0 ){
            def genotypes = [(TRAIN_LABEL):[:] as TreeMap, (TEST_LABEL):[:] as TreeMap]
            
            def phenotypes = [:]
            [(TRAIN_LABEL), (TEST_LABEL)].each{ set->
                def dir = dataDir.replace('{disease}',dis).replace('{set}', set)
                phenotypes[set] = getPhenotypes(dir)
            }

            chrSnps.each{ chr, snpMap->
                //get snp indices
                def indices = getMapIndices(trainDir, chr, snpMap)

                //get genotypes for training and test set
                genotypes.each{ set, map->
                    def dir = dataDir.replace('{disease}',dis).replace('{set}', set)
                    map[chr] = getGenotypes(dir, chr, indices, phenotypes[set])
                    assert (map[chr].size()==phenotypes[set].size())
                }
            }
            
            //write arff files
            genotypes.each{ set, map->
                ['rs','grs','wgrs'].each{ type->
                    def outFile = disOutput+genOutFileName(dis, thr, set, type)
                    println "generating arff file: ${outFile}"
                    genArff(outFile, chrSnps, map, phenotypes[set], type)

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