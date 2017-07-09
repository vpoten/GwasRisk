/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.clados.gwasrisk

import org.clados.gwasrisk.weka.DiscreteEstimatorSimple

/**
 *
 * @author victor
 */

class GwasPattern {
    String id
    String phenotype
    double GRS = 0.0
    double wGRS = 0.0
    double estWGRS = 0.0
}


class SNPData {
    String id
    String chr
    double or = 0.0
    double logOr = 0.0
    double pvalue = 0.0
    String rAllele //risk allele or haplotype
    def estAff
    def estUnaff
    private Double estLogOR = null
    
    public createEst(){
        estAff = new DiscreteEstimatorSimple()
        estUnaff = new DiscreteEstimatorSimple()
    }
    
    /**
     * update probabilities
     */
    public void updateHi(String pheno, byte x1, byte x2){
        if( pheno==Main.affCode){
            estAff.addValue(x1, 1.0)
            estAff.addValue(x2, 1.0)
        }
        else if( pheno==Main.unaffCode ){
            estUnaff.addValue(x1, 1.0)
            estUnaff.addValue(x2, 1.0)
        }
    }
    
    /**
     * calculates OR using internal estimators
     */
    public double calcLogOR(){
        if( estLogOR==null )
            estLogOR = Math.log( (estAff.getProbability(1)*estUnaff.getProbability(0)) /
                (estUnaff.getProbability(1)*estAff.getProbability(0)) )
            
        return estLogOR
    }
}


class GrsOps {
    
    private def mapSNP = [:] //map with key=chr and value=map of selected SNPdata, keys=snp ids
    private def mapIdxs = [:] //map with key=chr and value=map of selected SNPData, keys=indexes
    private def selectedSNPs
    private Matrix rAlleles
    
    double alpha0 = 0.0 // NBC alpha0
    private def estPattAff = new DiscreteEstimatorSimple()
    
    static def regexDatHPed = /(\w+_)(\d+)(\.dat\.haped)/ //*_(chr).dat.haped
    static def regexTesHPed = /(\w+_)(\d+)(\.tes\.haped)/ //*_(chr).tes.haped
    
    private int numDat = 0
    private int numTes = 0
    
    /**
     *
     */
    public def generatePatterns(String input, double cutPval){
        countInstances(input)
        getSNP(input, cutPval, /.+\sTREND\s.+/)
        filterMissing(input)
        calcGrsTerms(input)
        buildSNPbyIdxList(input)
        
        if( getNumSnps()==0 )
            return null
            
        def patterns = generatePatterns(input)
        alpha0 = calcNBCalpha0()
        estimateWGRS(patterns)
        return patterns
    }
    
    /**
     *
     */
    public def generatePatternsUnfilter(String input, List snpList){
        countInstances(input)
        calcGrsTerms(input, snpList)
        buildSNPbyIdxList(input)
        def patterns = generatePatterns(input)
        alpha0 = calcNBCalpha0()
        estimateWGRS(patterns)
        return patterns
    }
    
    /**
     *
     */
    public def generateHapPatterns(String input, double cutPval){
        countInstances(input)
        getHaplotypes(input, cutPval)
        
        if( getNumSnps()==0 )
            return null
            
        def patterns = generatePatterns(input, true, 2)
        alpha0 = calcNBCalpha0()
        estimateWGRS(patterns)
        return patterns
    }
    
    /**
     *
     */
    protected def countInstances(dir){
        numDat = Utils.countLines( dir + new File(dir).list({d, f-> f ==~ Main.regexDatPed } as FilenameFilter)[0] )
        numTes = Utils.countLines( dir + new File(dir).list({d, f-> f ==~ Main.regexTesPed } as FilenameFilter)[0] )
    }
    
    
    /**
     * generates phase files (.haped) for each .ped file
     */
    static public def genPhaseFiles(String input, String plink, double cutPval){
        // generate list of selected haplotypes for each chromosome
        generateHList(input, cutPval)
        // build (.dat/.tes).haped files using (.dat/.tes).ped.phase-WIN*
        phasePedFiles(input, plink)
    }
    
    
    /**
     * splits the given string and returns the tokens which are not spaces/blank
     * characters (' ', '\t', ...)
     */
    private static getClearTokens(String string){
        return (string.split("\\s") as List).findAll{!it.isEmpty()}
    }
    
    /**
     *
     */
    private void updateProb(String pheno){
        if( pheno==Main.affCode)
            estPattAff.addValue(1.0, 1.0)
        else if( pheno==Main.unaffCode )
            estPattAff.addValue(0.0, 1.0)
    }
    
    /**
     * get SNPs with pval < cutPval
     * 
     * @param input : input directory where to find .dat.model files
     * @param cutPval
     * @param filterReg : example:/.+\sTREND\s.+/ use lines of trend test
     * @return
     */
    private def getSNP(String input, double cutPval, filterReg){
        def regexModel = /\w+_(\d+)\.dat\.model/
        
        def modelFiles = new File(input).list({d, f-> f ==~ regexModel } as FilenameFilter).toList()
        
        modelFiles.each{ file->
            def mat = (file =~ regexModel)
            def chr = mat[0][1]
            mapSNP[chr] = [:] as TreeMap
                
            def reader = Utils.createReader(new File(input+file))
            
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
                            mapSNP[chr][toks[snpIdx]] = new SNPData(id:toks[snpIdx], chr:chr)
                    } catch(NumberFormatException e){}
                }
            }
            
            reader.close()
        }
    }
    
    /**
     * remove SNPs with missingness differ between cases and controls
     */
    private def filterMissing(String input, double cutPval = 0.05){
        def regexMissing = /\w+_(\d+)\.dat\.missing/
        
        def missFiles = new File(input).list({d, f-> f ==~ regexMissing } as FilenameFilter).toList()
        
        missFiles.each{ file->
            def mat = (file =~ regexMissing)
            def chr = mat[0][1]
                
            def reader = Utils.createReader(new File(input+file))
            
            //get header and field indexes
            def header = reader.readLine().trim()
            def fields = getClearTokens(header)
            int snpIdx = fields.findIndexOf{it=='SNP'}
            int pIdx = fields.findIndexOf{it=='P'}
            
            reader.eachLine{ line ->
                def toks = getClearTokens( line.trim() )
                try{
                    double pval = toks[pIdx] as Double
                    if( pval<cutPval )
                        mapSNP[chr].remove(toks[snpIdx])
                } catch(NumberFormatException e){}
            }
            
            reader.close()
        }
    }
    
    /**
     * calculates wi = ln(ORi) and risk allele
     * 
     * @param input : input directory where to find .dat.assoc files
     * @param count : if true uses count method (logOR=1) else uses logOR
     * @param snpList : optional snp lists, calculates OR for snps in list only
     * @return the given mapSNP with the new calculated data added
     */
    private def calcGrsTerms(String input, snpList = null){
        def regexAssoc = /\w+_(\d+)\.dat\.assoc/
        
        def assocFiles = new File(input).list({d, f-> f ==~ regexAssoc } as FilenameFilter).toList()
        
        assocFiles.each{ file->
            def mat = (file =~ regexAssoc)
            def chr = mat[0][1]
            
            if( snpList && !(mapSNP[chr]) )
                mapSNP[chr] = [:] as TreeMap
                
            def reader = Utils.createReader(new File(input+file))
            
            //get header and field indexes
            def header = reader.readLine().trim()
            def fields = getClearTokens(header)
            int snpIdx = fields.findIndexOf{it=='SNP'}
            int orIdx = fields.findIndexOf{it=='OR'}
            int a1Idx = fields.findIndexOf{it=='A1'}
            int a2Idx = fields.findIndexOf{it=='A2'}
            
            reader.eachLine{ line ->
                def toks = getClearTokens( line.trim() )
                SNPData snpData = null
                
                if( snpList ){
                    if(toks[snpIdx] in snpList){
                        snpData = new SNPData(id:toks[snpIdx], chr:chr)
                        mapSNP[chr][toks[snpIdx]] = snpData
                    }
                }
                else{
                    snpData = mapSNP[chr][toks[snpIdx]]
                }
                
                if( snpData!=null ){
                    //complete SNP data
                    snpData.rAllele = toks[a1Idx]
                    
                    try{
                        double or = toks[orIdx] as Double
                        if( or > Main.orCutoff ){
                            if( or<1.0 ){
                                or = 1.0/or
                                snpData.rAllele = toks[a2Idx]
                            }
                            snpData.or = or
                            snpData.logOr = Math.log(or)
                        }
                        else{//unselect snp
                            mapSNP[chr].remove(toks[snpIdx])
                        }
                    } catch(NumberFormatException e){}
                }
            }
            
            reader.close()
        }
    }
    
    /**
     * 
     * @param input : input directory where to find .map files
     */
    private def buildSNPbyIdxList(String input){
        //get sorted .list of map files
        def mapFiles = new File(input).list({d, f-> f ==~ Main.regexMap } as FilenameFilter).toList().sort()
        
        mapFiles.each{ map->
            def mat = (map =~ Main.regexMap)
            def chr = mat[0][1]
            
            mapIdxs[chr] = [:] as TreeMap //keep keys sorted
            
            if( !mapSNP[chr] )
                return//if is a spurious .map
                
            def reader = Utils.createReader(new File(input+map))
            int i=0
            reader.eachLine{ line ->
                def toks = line.split("\\s")
                SNPData snpData = mapSNP[chr][toks[1]]
        
                ////exclude snps with negative coordinates
                if( snpData && !toks[3].startsWith('-') ){
                    mapIdxs[chr][i] = snpData
                    snpData.createEst()
                }
                    
                i++
            }

            reader.close()
        }
    }
    
    /**
     * builds a pattern ID inserting ',' between the given values
     */
    private static String patternId(familyId, indivId){
        return "${familyId},${indivId}"
    }
    
    /**
     * gets the start row in rAlleles matrix. file could contain '.tes.' or '.dat.'
     */
    private int getStartRow(String file){
        if( file.contains('.tes.') ){
            return numDat+1
        }
        return 0
    }
    
    /**
     *
     */
    protected int getNumSnps(){
        return mapIdxs.values().sum{ it.size() }
    }
    
    /**
     * 
     * @param input : input directory where to find .dat.ped and .tes.ped files
     * @return a map with two entries: for training and test set; each set is a
     * list of GwasPattern
     */
    protected def Map generatePatterns(String input, boolean haplotype=false, int hapLen=0 ){
        Map patterns = [ '.dat':new ArrayList(Main.PAT_SIZE), 
                        '.tes':new ArrayList((Main.PAT_SIZE*0.1) as Integer)]
        
        int numSNPs = getNumSnps()
        boolean first = true
        selectedSNPs = new ArrayList(numSNPs)
        
        if( rAlleles.columnDimension<numSNPs )// create matrix array the first time that is used
            rAlleles.recreate(numDat+numTes+1, numSNPs)
            
        rAlleles.set(0 as Byte)// clean matrix
        
        def pedFilesRegex = (haplotype) ? [regexDatHPed,regexTesHPed] : [Main.regexDatPed,Main.regexTesPed]
        
        pedFilesRegex.each{ regex->
            def filter = {d, f-> f ==~ regex } as FilenameFilter
            
            // get ped files (order is important for rAlleles)
            def pedFiles = new File(input).list(filter).toList().sort()
            int colStart = 0
            
            pedFiles.each{ pedName->
                def mat = (pedName =~ regex)
                def chr = mat[0][2]
                def suff = mat[0][3]
                
                def patList = patterns.find{ suff.startsWith(it.key) }.value //get pattern set (train or test)
                
                int start = getStartRow(pedName)
                def reader = Utils.createReader(new File(input+pedName))
                int i=0
                reader.eachLine{ line ->
                    def tokens = line.split("\\s",7)
                    GwasPattern pattern = null
                    
                    if( (i+1) > patList.size() ){//if the pattern not exists yet
                        pattern = new GwasPattern( id:patternId(tokens[0], tokens[1]), 
                            phenotype:tokens[5])
                        patList << pattern
                    }
                    else{
                        pattern = patList[i]
                    }
                    
                    if( first ){
                        // execute only for the first .dat file
                        updateProb(tokens[5])
                    }            
                    
                    String genotype = tokens[6]//remainder of the line
                    int step = hapLen*2+2

                    mapIdxs[chr].eachWithIndex{ idx, snpData, j->
                        //calc wGRS or GRS if count==true
                        byte x1, x2

                        if( haplotype ){
                            int base = idx*step
                            x1 = (genotype.substring(base,base+hapLen)==snpData.rAllele) ? 1 : 0
                            x2 = (genotype.substring(base+hapLen+1,base+step-1)==snpData.rAllele) ? 1 : 0
                        }
                        else{
                            x1 = (genotype.charAt(idx*4)==snpData.rAllele) ? 1 : 0
                            x2 = (genotype.charAt(idx*4+2)==snpData.rAllele) ? 1 : 0
                        }

                        if( regex==Main.regexDatPed || regex==regexDatHPed ){// do for training set only
                            snpData.updateHi( tokens[5], x1, x2)
                            if( i==0 ) //keep snpData
                                selectedSNPs << snpData
                        }

                        byte x = x1+x2
                        pattern.wGRS += ( snpData.logOr*x )
                        pattern.GRS += x
                        rAlleles.set( i+start, colStart+j, x)
                    }
                    
                    i++//increment pattern counter
                }
                
                reader.close()
                colStart += ((mapIdxs[chr]) ? mapIdxs[chr].size() : 0)
                first = false
            }
        }
        
        return patterns
    }
    
    /**
     *
     * @param patterns : a collection of GwasPattern
     * @param relation
     * @param arffFile
     * @param valGrs : GRS value to use Classify.{P_wGRS,P_GRS,P_eWGRS,P_rScore}
     */
    def genArff(Collection patterns, relation, arffFile, String valGrs){
        def value = { val, pat->
            //return the appropiate GRS value
            if( val==Classify.P_GRS )
                return pat.GRS
            else if( val==Classify.P_eWGRS )
                return pat.estWGRS
            else
                return pat.wGRS
        }
        
        if( valGrs==Classify.P_rScore )
            return genArffRiskScores(patterns, relation, arffFile)
        
        def writer = new BufferedWriter(new FileWriter(arffFile))
        
        writer.writeLine("@RELATION ${relation}")
        writer.writeLine('')
        writer.writeLine("@ATTRIBUTE wGRS NUMERIC")
        writer.writeLine("@ATTRIBUTE phenotype {1,2}")
        writer.writeLine('')
        writer.writeLine('@DATA')
        
        patterns.each{ pat-> 
            writer.writeLine("%${pat.id}")
            writer.writeLine("${value(valGrs,pat)},${pat.phenotype}")
        }
                  
        writer.close()
    }
    
    
    /**
     *
     */
    def genArffRiskScores(Collection patterns, relation, arffFile){
        def writer = new BufferedWriter(new FileWriter(arffFile))
        
        writer.writeLine("@RELATION ${relation}")
        writer.writeLine('')
        selectedSNPs.each{ writer.writeLine("@ATTRIBUTE '${it.chr}:${it.id}' {0,1,2}") }
        writer.writeLine("@ATTRIBUTE phenotype {1,2}")
        writer.writeLine('')
        writer.writeLine('@DATA')
        
        int start = getStartRow(arffFile)
        int cols = selectedSNPs.size()
        
        patterns.eachWithIndex{ pat, i -> 
            writer.writeLine("%${pat.id}")
            def scores = new StringBuilder()
            (0..(cols-1)).each{ scores << rAlleles.get(i+start, it)+',' }
            scores << pat.phenotype
            writer.writeLine( scores.toString() )
        }
                  
        writer.close()
    }
    
    /**
     *
     */
    private double calcNBCalpha0(){
        double sum = 0.0
        
        mapIdxs.values().each{ map->
                map.values().each{ snpData->
                    // p(hi=0/D)/p(hi=0/!D)
                    sum += Math.log(snpData.estAff.getProbability(0)/snpData.estUnaff.getProbability(0))
                }
            }
        
        return Math.log(estPattAff.getProbability(1)/estPattAff.getProbability(0)) + 2*sum
    }
    
    /**
     *
     */
    private void estimateWGRS(Map patterns){
        
        patterns.each{ setName, patList->
            int start = getStartRow(setName+'.')
            
            patList.eachWithIndex{ pat, i->
                selectedSNPs.eachWithIndex{ snpData, j->
                    // xi*lnORi
                    pat.estWGRS += (rAlleles.get(start+i, j)*snpData.calcLogOR())
                }
            }
        }
    }
    
    
    /**
     * 
     * @param input dir
     * @param plink : plink path option
     */
    static private phasePedFiles(input, plink){
       plink = plink.substring( plink.indexOf('=')+1 )
                
       def regexPhase = /.+\.phase-HAP(\d+)/
       def mapFiles = new File(input).list({d, f-> f ==~ Main.regexMap } as FilenameFilter).toList()
       def hlistFiles = new File(input).list({d, f-> f ==~ /\w+_(\d+)\.hlist/ } as FilenameFilter).toList()
       
       [Main.regexDatPed, Main.regexTesPed].each{ regex->
            def filter = {d, f-> f ==~ regex } as FilenameFilter
            def pedFiles = new File(input).list(filter).toList()
            
            pedFiles.each{ pedName->
                def mat = (pedName =~ regex)
                def chr = mat[0][2]
                def suff = mat[0][3]
                def prefix = mat[0][1]
                def mapName = mapFiles.find{ it.contains('_'+chr+'.map') }
                def outName = prefix+chr+suff
                def hapedName = prefix+chr+suff.replace('.ped','.haped')
                def hlist = hlistFiles.find{ it.contains('_'+chr+'.hlist') }
                
                
                def test = "--hap-phase --hap ${input+hlist}"
                
                println "Performing ${test} test for ${pedName}, ${mapName}"
            
                //generate phase files
                def command = Main.plinkTest(plink, test, input+pedName, input+mapName, input+outName)
                def proc = command.execute()
                proc.consumeProcessOutput()

                if( proc.waitFor()!=0 ){
                    System.err.println("Error while performing ${test} test for ${pedName}, ${mapName}")
                    System.exit(1)
                }
                
                //get .ped first 6 fields
                def lines = []
                new File(input+pedName).eachLine{ line->
                    def toks = line.split("\\s",7)
                    lines << new StringBuilder("${toks[0]}\t${toks[1]}\t${toks[2]}\t${toks[3]}\t${toks[4]}\t${toks[5]}")
                }
                
                //add haplotypes to .haped
                def phaseFiles = new File(input).list({d, f-> f ==~ regexPhase } as FilenameFilter).toList()
                //process phase files ordered by HAP number
                phaseFiles.sort{ (it =~ regexPhase)[0][1] }.each{ phase->
                    def reader = Utils.createReader(new File(input+phase))
                    
                    //get header and field indexes
                    def header = reader.readLine().trim()
                    def fields = getClearTokens(header)
                    int faIdx = fields.findIndexOf{it=='FID'}
                    int inIdx = fields.findIndexOf{it=='IID'}
                    int hap1Idx = fields.findIndexOf{it=='HAP1'}
                    int hap2Idx = fields.findIndexOf{it=='HAP2'}
                    int bestIdx = fields.findIndexOf{it=='BEST'}
                    
                    int i=0
                    reader.eachLine{ line->
                        def toks = getClearTokens( line.trim() )
                        if( toks[bestIdx]=='1' )
                            lines[i++] << "\t${toks[hap1Idx]}\t${toks[hap2Idx]}"
                    }
                    
                    reader.close()
                }
                
                //write lines to .haped file
                def writer = new BufferedWriter(new FileWriter(input+hapedName))
                lines.each{ writer.writeLine( it.toString() ) }
                writer.close()
                
                //delete *.ped.phase-HAP*, *.mishap and current .ped files
                def tmpFiles = new File(input).list({d, f-> f ==~ /.+\.mishap/ } as FilenameFilter).toList()
                (phaseFiles+tmpFiles+[input+pedName]).each{ "rm -f ${input+it}".execute().waitFor() }
            }
       }
       
    }
    
    /**
     *
     * @param input dir
     */
    static private generateHList(input, double cutPval){
        def regex = /(\w+_)(\d+)\.dat\.assoc\.hap\.logistic/
        def hapFiles = new File(input).list({d, f-> f ==~ regex } as FilenameFilter).toList()
        
        hapFiles.each{ hapFile->
            def mat = (hapFile =~ regex)
            def chr = mat[0][2]
            def pref = mat[0][1]
            def mapHaplo = [:] as TreeMap
            
            //read and select haplotypes
            def reader = Utils.createReader(new File(input+hapFile))
            //get header and field indexes
            def header = reader.readLine().trim()
            def fields = getClearTokens(header)
            int snp1Idx = fields.findIndexOf{it=='SNP1'}
            int snp2Idx = fields.findIndexOf{it=='SNP2'}
            int hapIdx = fields.findIndexOf{it=='HAPLOTYPE'}
            int orIdx = fields.findIndexOf{it=='OR'}
            int pIdx = fields.findIndexOf{it=='P'}
            
            reader.eachLine{ line ->
                def toks = getClearTokens( line.trim() )
                try{
                    double pval = toks[pIdx] as Double
                    double or = toks[orIdx] as Double
                    
                    //keep significant haplotypes with OR>1
                    if( pval<cutPval && or>1.0 ){
                        def hapId = patternId(toks[snp1Idx], toks[snp2Idx])
                        def haplo = toks[hapIdx]
                        def snpData = mapHaplo[hapId]
                        
                        if( !snpData || snpData.or<or )
                            mapHaplo[hapId] = new SNPData(id:hapId, chr:chr, or:or, 
                                pvalue:pval, rAllele:haplo)
                    }
                } catch(NumberFormatException e){}
            }
            
            reader.close()
            
            //write selected haplotypes
            def writer = new BufferedWriter(new FileWriter(input+"${pref}${chr}.hlist"))
            def writer2 = new BufferedWriter(new FileWriter(input+"${pref}${chr}.hlistext"))
            
            mapHaplo.eachWithIndex { hapId, snpData, i->
                def toks = hapId.split(',')
                writer.writeLine("**\tHAP${i}\t${toks[0]}\t${toks[1]}")
                //.hlistext fields: chr hapId risk_seq pvalue or
                writer2.writeLine("${chr}\t${snpData.id}\t${snpData.rAllele}\t${snpData.pvalue}\t${snpData.or}")
            }
            
            writer2.close()
            writer.close()
        }
        
    }
    
    /**
     *
     */
    private def getHaplotypes(String input, double cutPval){
        def regexModel = /\w+_(\d+)\.hlistext/
        
        def modelFiles = new File(input).list({d, f-> f ==~ regexModel } as FilenameFilter).toList()
        
        modelFiles.each{ file->
            def mat = (file =~ regexModel)
            def chr = mat[0][1]
            mapIdxs[chr] = [:] as TreeMap
                
            def reader = Utils.createReader(new File(input+file))
            
            int i = 0
            reader.splitEachLine("\\s"){ toks ->
                //.hlistext fields: chr hapId risk_seq pvalue or
                double pval = toks[3] as Double
                double or = toks[4] as Double
                
                if( pval<cutPval ){
                    def snpData = new SNPData(id:toks[1], chr:chr, or:or, 
                                pvalue:pval, rAllele:toks[2], 
                                logOr:Math.log(or) )
                    snpData.createEst()
                    mapIdxs[chr][i] = snpData
                }
                
                i++
            }
            
            reader.close()
        }
    }
    
}

